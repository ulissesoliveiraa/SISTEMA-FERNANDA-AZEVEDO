package com.fernandaazevedo.appointments;

import com.fernandaazevedo.common.DemoService;
import com.fernandaazevedo.domain.AppUser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
  "spring.datasource.url=jdbc:h2:mem:scheduling-test;DB_CLOSE_DELAY=-1",
  "spring.jpa.hibernate.ddl-auto=create-drop",
  "storage.private.root=target/test-scheduling-storage"
})
@Transactional
class AppointmentServiceIntegrationTest {
  @Autowired AppointmentService service;
  @Autowired DemoService demo;

  @Test
  void patientFlowPersistsAndAssignedDoctorCanStartAndFinish() {
    AppUser patient = demo.users.findByEmailIgnoreCase("paciente@fernandaazevedo.demo").orElseThrow();
    AppUser doctor = newDoctor("medico2@fernandaazevedo.demo");
    MedicalAppointment appointment = service.checkout(patient);
    assertEquals("PAYMENT_PENDING", appointment.status);
    service.approvePayment(appointment.id, patient);
    service.saveAnamnesis(appointment.id, patient, Map.of("complaint", "Sintoma fictício", "started", "Hoje"));
    service.enterQueue(appointment.id, patient);
    service.accept(appointment.id, doctor);
    service.start(appointment.id, doctor);
    MedicalAppointment finished = service.finish(appointment.id, doctor);
    assertEquals("COMPLETED", finished.status);
    assertNotNull(finished.startedAt);
    assertNotNull(finished.endedAt);
    assertEquals(1, finished.anamnesisVersion);
  }

  @Test
  void doctorCannotAcceptTwoPatientsAtSameTime() {
    AppUser patient = demo.users.findByEmailIgnoreCase("paciente@fernandaazevedo.demo").orElseThrow();
    AppUser doctor = newDoctor("medico3@fernandaazevedo.demo");
    MedicalAppointment first = waiting(patient);
    MedicalAppointment second = waiting(patient);
    service.accept(first.id, doctor);
    assertThrows(AppointmentService.RuleException.class, () -> service.accept(second.id, doctor));
  }

  @Test
  void patientCanFinishOwnActiveConsultationAndActorIsAudited() {
    AppUser patient = demo.users.findByEmailIgnoreCase("paciente@fernandaazevedo.demo").orElseThrow();
    AppUser doctor = newDoctor("medico-patient-finish@fernandaazevedo.demo");
    MedicalAppointment appointment = waiting(patient);
    service.accept(appointment.id, doctor);
    service.start(appointment.id, doctor);

    MedicalAppointment finished = service.finish(appointment.id, patient, "Paciente solicitou encerramento");

    assertEquals("COMPLETED", finished.status);
    assertEquals(patient.id, finished.endedByUserId);
    assertEquals("PATIENT", finished.endedByRole);
    assertEquals("Paciente solicitou encerramento", finished.completionReason);
    assertNotNull(finished.endedAt);
    assertThrows(AppointmentService.RuleException.class, () -> service.finish(appointment.id, doctor));
  }

  @Test
  void slotCannotBeDoubleBookedOrOverlap() {
    AppUser patient = demo.users.findByEmailIgnoreCase("paciente@fernandaazevedo.demo").orElseThrow();
    AppUser doctor = newDoctor("medico4@fernandaazevedo.demo");
    LocalDateTime start = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
    DoctorAvailabilitySlot slot = service.createSlot(doctor, start, start.plusMinutes(30));
    assertThrows(AppointmentService.RuleException.class, () -> service.createSlot(doctor, start.plusMinutes(10), start.plusMinutes(40)));
    MedicalAppointment booked = service.bookSlot(patient, slot.id);
    assertEquals("PAYMENT_PENDING", booked.status);
    assertEquals(doctor.id, booked.doctorUserId);
    assertThrows(AppointmentService.RuleException.class, () -> service.bookSlot(patient, slot.id));
  }

  private MedicalAppointment waiting(AppUser patient) { MedicalAppointment appointment = service.checkout(patient); service.approvePayment(appointment.id, patient); service.saveAnamnesis(appointment.id, patient, Map.of("complaint", "Teste")); return service.enterQueue(appointment.id, patient); }
  private AppUser newDoctor(String email) { AppUser doctor = new AppUser(); doctor.name = "Médico Teste"; doctor.email = email; doctor.role = "DOCTOR"; doctor.passwordHash = demo.encoder.encode("Demo@123"); doctor.online = true; return demo.users.save(doctor); }
}
