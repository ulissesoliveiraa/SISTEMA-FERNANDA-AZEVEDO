package com.fernandaazevedo.consultation;

import com.fernandaazevedo.common.DemoService;
import com.fernandaazevedo.appointments.MedicalAppointment;
import com.fernandaazevedo.appointments.MedicalAppointmentRepository;
import com.fernandaazevedo.domain.AppUser;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class ConsultationAccessService {
  private static final Set<String> ROOM_STATES = Set.of("READY", "IN_PROGRESS");
  private final DemoService demo;
  private final MedicalAppointmentRepository appointments;

  public ConsultationAccessService(DemoService demo, MedicalAppointmentRepository appointments) { this.demo = demo; this.appointments = appointments; }

  public Optional<RoomAccess> authorize(Long consultationId, Long userId) {
    if (consultationId == null || userId == null) return Optional.empty();
    Optional<AppUser> userResult = demo.users.findById(userId);
    Optional<MedicalAppointment> appointmentResult = appointments.findById(consultationId);
    if (userResult.isEmpty() || appointmentResult.isEmpty()) return Optional.empty();
    AppUser user = userResult.get();
    MedicalAppointment appointment = appointmentResult.get();
    if (!ROOM_STATES.contains(appointment.status)) return Optional.empty();
    boolean patient = "PATIENT".equals(user.role) && appointment.patientUserId.equals(user.id);
    boolean doctor = "DOCTOR".equals(user.role) && user.id.equals(appointment.doctorUserId);
    if (!patient && !doctor) return Optional.empty();
    return Optional.of(new RoomAccess(appointment, user, patient ? "PATIENT" : "DOCTOR"));
  }

  public record RoomAccess(MedicalAppointment appointment, AppUser user, String participantRole) {}
}
