package com.fernandaazevedo.consultation;

import com.fernandaazevedo.appointments.*;
import com.fernandaazevedo.domain.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
  "spring.datasource.url=jdbc:h2:mem:consultation-test;DB_CLOSE_DELAY=-1",
  "spring.jpa.hibernate.ddl-auto=create-drop",
  "server.address=127.0.0.1",
  "storage.private.root=target/test-private-storage"
})
class ConsultationAccessIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired MedicalAppointmentRepository appointments;
  @Autowired AppUserRepository users;

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
  @Transactional
  void completedConsultationCannotBeRejoined() throws Exception {
    MedicalAppointment completed = new MedicalAppointment(); completed.reference = "FA-COMPLETED-TEST"; completed.patientUserId = users.findByEmailIgnoreCase("paciente@fernandaazevedo.demo").orElseThrow().id; completed.status = "COMPLETED"; completed.priceAtPurchase = BigDecimal.TEN; completed.doctorPayoutAtPurchase = BigDecimal.ONE; Long id = appointments.save(completed).id;
    mvc.perform(get("/api/consultations/{id}/session", id).session(login("paciente@fernandaazevedo.demo")))
      .andExpect(status().isForbidden());
  }

  @Test
  void clinicalUploadIsAuthorizedValidatedAndQuarantinedWithoutScanner() throws Exception {
    Long id = liveConsultationId();
    MockMultipartFile pdf = new MockMultipartFile("files", "exame.pdf", "application/pdf", "%PDF-1.7\nconteudo".getBytes());
    MockHttpSession patient = login("paciente@fernandaazevedo.demo");
    mvc.perform(multipart("/api/consultations/{id}/attachments", id).file(pdf).session(patient).header("X-CSRF-Token", patient.getAttribute("csrfToken")))
      .andExpect(status().isAccepted())
      .andExpect(jsonPath("$[0].scanStatus").value("QUARANTINED"))
      .andExpect(jsonPath("$[0].downloadUrl").doesNotExist());
    MockHttpSession support = login("suporte@fernandaazevedo.demo");
    mvc.perform(multipart("/api/consultations/{id}/attachments", id).file(pdf).session(support).header("X-CSRF-Token", support.getAttribute("csrfToken")))
      .andExpect(status().isForbidden());
    MockMultipartFile executable = new MockMultipartFile("files", "programa.exe", "application/octet-stream", "MZ".getBytes());
    mvc.perform(multipart("/api/consultations/{id}/attachments", id).file(executable).session(patient).header("X-CSRF-Token", patient.getAttribute("csrfToken")))
      .andExpect(status().isBadRequest());
  }

  private Long liveConsultationId() {
    return appointments.findByReference("FA-LIVE-DEMO").orElseThrow().id;
  }

  private MockHttpSession login(String email) throws Exception {
    return (MockHttpSession) mvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"" + email + "\",\"password\":\"Demo@123\"}"))
      .andExpect(status().isOk())
      .andReturn().getRequest().getSession(false);
  }
}
