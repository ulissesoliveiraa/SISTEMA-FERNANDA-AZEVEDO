package com.fernandaazevedo.consultation;

import com.fernandaazevedo.domain.DomainRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
  "spring.datasource.url=jdbc:h2:mem:consultation-test;DB_CLOSE_DELAY=-1",
  "spring.jpa.hibernate.ddl-auto=create-drop",
  "server.address=127.0.0.1"
})
class ConsultationAccessIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired DomainRecordRepository records;

  @Test
  void patientAndAssignedDoctorCanOpenLiveRoom() throws Exception {
    Long id = liveConsultationId();
    mvc.perform(get("/api/consultations/{id}/session", id).session(login("paciente@fernandaazevedo.demo")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.role").value("PATIENT"))
      .andExpect(jsonPath("$.iceServers", hasSize(greaterThanOrEqualTo(1))));
    mvc.perform(get("/api/consultations/{id}/session", id).session(login("medico@fernandaazevedo.demo")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.role").value("DOCTOR"));
    mvc.perform(get("/api/consultations/{id}/messages", id).session(login("paciente@fernandaazevedo.demo")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void supportAndAdminCannotEnterClinicalRoom() throws Exception {
    Long id = liveConsultationId();
    mvc.perform(get("/api/consultations/{id}/session", id).session(login("suporte@fernandaazevedo.demo")))
      .andExpect(status().isForbidden());
    mvc.perform(get("/api/consultations/{id}/messages", id).session(login("suporte@fernandaazevedo.demo")))
      .andExpect(status().isForbidden());
    mvc.perform(get("/api/consultations/{id}/session", id).session(login("admin@fernandaazevedo.demo")))
      .andExpect(status().isForbidden());
  }

  @Test
  void completedConsultationCannotBeRejoined() throws Exception {
    Long id = records.findByTypeAndReference("APPOINTMENT", "FA-CONS-2026-0042").orElseThrow().id;
    mvc.perform(get("/api/consultations/{id}/session", id).session(login("paciente@fernandaazevedo.demo")))
      .andExpect(status().isForbidden());
  }

  private Long liveConsultationId() {
    return records.findByTypeAndReference("APPOINTMENT", "FA-LIVE-DEMO").orElseThrow().id;
  }

  private MockHttpSession login(String email) throws Exception {
    return (MockHttpSession) mvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"" + email + "\",\"password\":\"Demo@123\"}"))
      .andExpect(status().isOk())
      .andReturn().getRequest().getSession(false);
  }
}
