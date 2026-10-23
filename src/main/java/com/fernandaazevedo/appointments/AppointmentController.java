package com.fernandaazevedo.appointments;

import com.fernandaazevedo.common.DemoService;
import com.fernandaazevedo.consultation.ConsultationSignalingHandler;
import com.fernandaazevedo.domain.AppUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
  private final AppointmentService service;
  private final DemoService demo;
  private final ConsultationSignalingHandler signaling;

  public AppointmentController(AppointmentService service, DemoService demo, ConsultationSignalingHandler signaling) { this.service = service; this.demo = demo; this.signaling = signaling; }

  @GetMapping public ResponseEntity<?> list(HttpSession session) { AppUser user = user(session); return user == null ? unauthorized() : ResponseEntity.ok(service.visibleTo(user).stream().map(this::view).toList()); }
  @PostMapping("/checkout") public ResponseEntity<?> checkout(HttpSession session) { AppUser user = user(session); if (user == null) return unauthorized(); if (!"PATIENT".equals(user.role)) return forbidden(); return ResponseEntity.status(201).body(view(service.checkout(user))); }
  @PostMapping("/{id}/payment/mock-approve") public ResponseEntity<?> approve(@PathVariable Long id, HttpSession session) { return withUser(session, user -> view(service.approvePayment(id, user))); }
  @PutMapping("/{id}/pre-anamnesis") public ResponseEntity<?> anamnesis(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) { return withUser(session, user -> view(service.saveAnamnesis(id, user, body))); }
  @PostMapping("/{id}/queue") public ResponseEntity<?> queue(@PathVariable Long id, HttpSession session) { return withUser(session, user -> view(service.enterQueue(id, user))); }
  @PostMapping("/{id}/accept") public ResponseEntity<?> accept(@PathVariable Long id, HttpSession session) { return withUser(session, user -> view(service.accept(id, user))); }
  @PostMapping("/{id}/start") public ResponseEntity<?> start(@PathVariable Long id, HttpSession session) { return withUser(session, user -> view(service.start(id, user))); }
  @PostMapping("/{id}/finish") public ResponseEntity<?> finish(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body, HttpSession session) {
    return withUser(session, user -> {
      MedicalAppointment appointment = service.finish(id, user, body == null ? null : String.valueOf(body.getOrDefault("reason", "")));
      signaling.consultationEnded(appointment.id, appointment.endedByRole, appointment.endedAt);
      return view(appointment);
    });
  }

  @PutMapping("/doctor/presence")
  public ResponseEntity<?> presence(@RequestBody Map<String, Object> body, HttpSession session) {
    return withUser(session, user -> Map.of("online", service.presence(user, Boolean.TRUE.equals(body.get("online"))).online));
  }

  @GetMapping("/availability")
  public ResponseEntity<?> availability(HttpSession session) { AppUser user = user(session); return user == null ? unauthorized() : ResponseEntity.ok(service.availableSlots(user).stream().map(this::slotView).toList()); }

  @PostMapping("/availability")
  public ResponseEntity<?> createAvailability(@RequestBody Map<String, String> body, HttpSession session) {
    return withUser(session, user -> slotView(service.createSlot(user, LocalDateTime.parse(body.getOrDefault("startsAt", "")), LocalDateTime.parse(body.getOrDefault("endsAt", "")))));
  }

  @DeleteMapping("/availability/{id}") public ResponseEntity<?> cancelAvailability(@PathVariable Long id, HttpSession session) { return withUser(session, user -> slotView(service.cancelSlot(user, id))); }
  @PostMapping("/schedule") public ResponseEntity<?> schedule(@RequestBody Map<String, Object> body, HttpSession session) { return withUser(session, user -> view(service.bookSlot(user, Long.valueOf(String.valueOf(body.get("slotId")))))); }

  @ExceptionHandler({AppointmentService.RuleException.class, java.time.format.DateTimeParseException.class, NumberFormatException.class})
  public ResponseEntity<?> ruleError(Exception error) { return ResponseEntity.badRequest().body(Map.of("message", error instanceof AppointmentService.RuleException ? error.getMessage() : "Dados de agenda inválidos.")); }

  private ResponseEntity<?> withUser(HttpSession session, java.util.function.Function<AppUser, Object> action) { AppUser user = user(session); if (user == null) return unauthorized(); return ResponseEntity.ok(action.apply(user)); }
  private AppUser user(HttpSession session) { Object id = session.getAttribute("uid"); return id instanceof Long value ? demo.users.findById(value).orElse(null) : null; }
  private ResponseEntity<?> unauthorized() { return ResponseEntity.status(401).body(Map.of("message", "Sessão expirada.")); }
  private ResponseEntity<?> forbidden() { return ResponseEntity.status(403).body(Map.of("message", "Você não tem permissão para esta ação.")); }

  private Map<String, Object> view(MedicalAppointment appointment) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("id", appointment.id); result.put("reference", appointment.reference); result.put("status", appointment.status);
    result.put("patientUserId", appointment.patientUserId); result.put("patient", name(appointment.patientUserId));
    result.put("doctorUserId", appointment.doctorUserId); result.put("doctor", name(appointment.doctorUserId));
    result.put("scheduledStart", appointment.scheduledStart); result.put("scheduledEnd", appointment.scheduledEnd);
    result.put("queueEnteredAt", appointment.queueEnteredAt); result.put("acceptedAt", appointment.acceptedAt); result.put("startedAt", appointment.startedAt); result.put("endedAt", appointment.endedAt);
    result.put("endedByUserId", appointment.endedByUserId); result.put("endedByRole", appointment.endedByRole); result.put("completionReason", appointment.completionReason);
    result.put("priceAtPurchase", appointment.priceAtPurchase); result.put("doctorPayoutAtPurchase", appointment.doctorPayoutAtPurchase);
    result.put("preAnamnesis", appointment.preAnamnesis); result.put("anamnesisVersion", appointment.anamnesisVersion); result.put("createdAt", appointment.createdAt);
    return result;
  }

  private Map<String, Object> slotView(DoctorAvailabilitySlot slot) { return Map.of("id", slot.id, "doctorUserId", slot.doctorUserId, "doctor", name(slot.doctorUserId), "startsAt", slot.startsAt, "endsAt", slot.endsAt, "status", slot.status); }
  private String name(Long userId) { return userId == null ? "Aguardando definição" : demo.users.findById(userId).map(user -> user.name).orElse("Usuário indisponível"); }
}
