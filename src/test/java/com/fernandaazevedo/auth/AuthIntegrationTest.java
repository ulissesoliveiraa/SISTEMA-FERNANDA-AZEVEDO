package com.fernandaazevedo.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
  "spring.datasource.url=jdbc:h2:mem:auth-test;DB_CLOSE_DELAY=-1",
  "spring.jpa.hibernate.ddl-auto=create-drop",
  "app.demo.expose-reset-token=true"
})
@AutoConfigureMockMvc
class AuthIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  @Test void authenticatesAllFourProfilesAndIssuesCsrfToken() throws Exception {
    Map<String,String> expected = Map.of(
      "paciente@fernandaazevedo.demo", "PATIENT",
      "medico@fernandaazevedo.demo", "DOCTOR",
      "suporte@fernandaazevedo.demo", "SUPPORT",
      "admin@fernandaazevedo.demo", "ADMIN"
    );
    for (var account : expected.entrySet()) {
      mvc.perform(post("/api/auth/login").contentType("application/json")
          .content(json.writeValueAsString(Map.of("email", account.getKey(), "password", "Demo@123"))))
        .andExpect(status().isOk()).andExpect(jsonPath("$.role").value(account.getValue()))
        .andExpect(jsonPath("$.csrfToken", not(emptyString())));
    }
  }

  @Test void rejectsUnsafeRequestWithoutCsrfAndAcceptsMatchingToken() throws Exception {
    var login = mvc.perform(post("/api/auth/login").contentType("application/json")
        .content(json.writeValueAsString(Map.of("email", "paciente@fernandaazevedo.demo", "password", "Demo@123"))))
      .andExpect(status().isOk()).andReturn();
    MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
    String csrf = json.readTree(login.getResponse().getContentAsString()).get("csrfToken").asText();
    mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isForbidden());
    mvc.perform(post("/api/auth/logout").session(session).header("X-CSRF-Token", csrf)).andExpect(status().isOk());
  }

  @Test void resetTokenIsSingleUseAndChangesPassword() throws Exception {
    String email = "redefinicao@fernandaazevedo.demo";
    mvc.perform(post("/api/auth/register").contentType("application/json").content(json.writeValueAsString(Map.of(
      "name", "Conta Redefinição", "email", email, "password", "SenhaAntiga@123"
    )))).andExpect(status().isOk());
    var forgot = mvc.perform(post("/api/auth/forgot-password").contentType("application/json")
        .content(json.writeValueAsString(Map.of("email", email))))
      .andExpect(status().isOk()).andExpect(jsonPath("$.demoResetUrl", startsWith("/redefinir-senha?token="))).andReturn();
    String url = json.readTree(forgot.getResponse().getContentAsString()).get("demoResetUrl").asText();
    String token = url.substring(url.indexOf('=') + 1);
    String body = json.writeValueAsString(Map.of("token", token, "password", "NovaSenha@123"));
    mvc.perform(post("/api/auth/reset-password").contentType("application/json").content(body)).andExpect(status().isOk());
    mvc.perform(post("/api/auth/reset-password").contentType("application/json").content(body)).andExpect(status().isBadRequest());
    mvc.perform(post("/api/auth/login").contentType("application/json")
        .content(json.writeValueAsString(Map.of("email", email, "password", "NovaSenha@123"))))
      .andExpect(status().isOk());
  }

  @Test void temporarilyBlocksExcessiveLoginAttempts() throws Exception {
    String email = "limite@fernandaazevedo.demo";
    mvc.perform(post("/api/auth/register").contentType("application/json").content(json.writeValueAsString(Map.of(
      "name", "Conta Limite", "email", email, "password", "SenhaValida@123"
    )))).andExpect(status().isOk());
    for (int i = 0; i < 5; i++) {
      mvc.perform(post("/api/auth/login").with(request -> { request.setRemoteAddr("198.51.100.22"); return request; })
          .contentType("application/json").content(json.writeValueAsString(Map.of("email", email, "password", "incorreta"))))
        .andExpect(status().isUnauthorized());
    }
    mvc.perform(post("/api/auth/login").with(request -> { request.setRemoteAddr("198.51.100.22"); return request; })
        .contentType("application/json").content(json.writeValueAsString(Map.of("email", email, "password", "SenhaValida@123"))))
      .andExpect(status().isTooManyRequests()).andExpect(header().string("Retry-After", "900"));
  }
}
