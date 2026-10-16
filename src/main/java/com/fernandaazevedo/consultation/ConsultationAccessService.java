package com.fernandaazevedo.consultation;

import com.fernandaazevedo.common.DemoService;
import com.fernandaazevedo.domain.AppUser;
import com.fernandaazevedo.domain.DomainRecord;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ConsultationAccessService {
  private static final Set<String> CLOSED = Set.of("COMPLETED", "CANCELLED", "NO_SHOW");
  private final DemoService demo;

  public ConsultationAccessService(DemoService demo) { this.demo = demo; }

  public Optional<RoomAccess> authorize(Long consultationId, Long userId) {
    if (consultationId == null || userId == null) return Optional.empty();
    Optional<AppUser> userResult = demo.users.findById(userId);
    Optional<DomainRecord> appointmentResult = demo.records.findById(consultationId);
    if (userResult.isEmpty() || appointmentResult.isEmpty()) return Optional.empty();
    AppUser user = userResult.get();
    DomainRecord appointment = appointmentResult.get();
    if (!"APPOINTMENT".equals(appointment.type) || CLOSED.contains(appointment.status)) return Optional.empty();

    Map<String, Object> payload = demo.payload(appointment);
    boolean patient = "PATIENT".equals(user.role) && appointment.owner.equalsIgnoreCase(user.email);
    boolean doctor = "DOCTOR".equals(user.role) && (
      user.email.equalsIgnoreCase(String.valueOf(payload.getOrDefault("doctorEmail", ""))) ||
      user.name.equalsIgnoreCase(String.valueOf(payload.getOrDefault("doctor", "")))
    );
    if (!patient && !doctor) return Optional.empty();
    return Optional.of(new RoomAccess(appointment, user, patient ? "PATIENT" : "DOCTOR"));
  }

  public record RoomAccess(DomainRecord appointment, AppUser user, String participantRole) {}
}
