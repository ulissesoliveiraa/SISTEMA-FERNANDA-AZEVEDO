package com.fernandaazevedo.appointments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandaazevedo.common.DemoService;
import com.fernandaazevedo.domain.AppUser;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class AppointmentService {
  private static final Set<String> DOCTOR_BUSY = Set.of("READY", "IN_PROGRESS");
  private final MedicalAppointmentRepository appointments;
  private final DoctorAvailabilityRepository slots;
  private final DemoService demo;
  private final ObjectMapper json;

  public AppointmentService(MedicalAppointmentRepository appointments, DoctorAvailabilityRepository slots, DemoService demo, ObjectMapper json) {
    this.appointments = appointments; this.slots = slots; this.demo = demo; this.json = json;
  }

  @PostConstruct
  @Transactional
  public void seed() {
    if (appointments.findByReference("FA-LIVE-DEMO").isPresent()) return;
    AppUser patient = demo.users.findByEmailIgnoreCase("paciente@fernandaazevedo.demo").orElseThrow();
    AppUser doctor = demo.users.findByEmailIgnoreCase("medico@fernandaazevedo.demo").orElseThrow();
    doctor.online = true; demo.users.save(doctor);
    MedicalAppointment appointment = base(patient.id);
    appointment.reference = "FA-LIVE-DEMO";
    appointment.doctorUserId = doctor.id;
    appointment.status = "IN_PROGRESS";
    appointment.acceptedAt = LocalDateTime.now().minusMinutes(2);
    appointment.startedAt = LocalDateTime.now().minusMinutes(1);
    appointment.preAnamnesis = "{\"complaint\":\"Consulta demonstrativa em tempo real\"}";
    appointment.anamnesisVersion = 1;
    appointments.save(appointment);
  }

  public List<MedicalAppointment> visibleTo(AppUser user) {
    if ("PATIENT".equals(user.role)) return appointments.findByPatientUserIdOrderByCreatedAtDesc(user.id);
    if ("DOCTOR".equals(user.role)) {
      List<MedicalAppointment> result = new ArrayList<>(appointments.findByDoctorUserIdOrderByCreatedAtDesc(user.id));
      if (user.online) appointments.findByStatusOrderByQueueEnteredAtAsc("WAITING").stream().filter(item -> item.doctorUserId == null).forEach(result::add);
      return result;
    }
    if ("ADMIN".equals(user.role) || user.permissions.contains("appointments.view_operational")) return appointments.findAll();
    return List.of();
  }

  @Transactional
  public MedicalAppointment checkout(AppUser patient) { MedicalAppointment appointment = base(patient.id); appointment.status = "PAYMENT_PENDING"; return appointments.save(appointment); }

  @Transactional
  public MedicalAppointment approvePayment(Long id, AppUser patient) { MedicalAppointment appointment = ownedPatient(id, patient); requireStatus(appointment, "PAYMENT_PENDING"); appointment.status = "PAID"; touch(appointment); return appointment; }

  @Transactional
  public MedicalAppointment saveAnamnesis(Long id, AppUser patient, Map<String, Object> answers) { MedicalAppointment appointment = ownedPatient(id, patient); if (!Set.of("PAID", "ANAMNESIS_COMPLETED").contains(appointment.status)) throw new RuleException("A pré-anamnese só pode ser preenchida após o pagamento."); try { appointment.preAnamnesis = json.writeValueAsString(answers); } catch(Exception error) { throw new RuleException("Não foi possível salvar as respostas."); } appointment.anamnesisVersion++; appointment.status = "ANAMNESIS_COMPLETED"; touch(appointment); return appointment; }

  @Transactional
  public MedicalAppointment enterQueue(Long id, AppUser patient) { MedicalAppointment appointment = ownedPatient(id, patient); requireStatus(appointment, "ANAMNESIS_COMPLETED"); appointment.status = "WAITING"; appointment.queueEnteredAt = LocalDateTime.now(); touch(appointment); return appointment; }

  @Transactional
  public MedicalAppointment accept(Long id, AppUser doctor) { if (!"DOCTOR".equals(doctor.role) || !doctor.online) throw new RuleException("O médico precisa estar online para aceitar atendimentos."); if (appointments.existsByDoctorUserIdAndStatusIn(doctor.id, DOCTOR_BUSY)) throw new RuleException("Finalize ou devolva o atendimento atual antes de aceitar outro."); MedicalAppointment appointment = appointments.findLockedById(id).orElseThrow(() -> new RuleException("Consulta não encontrada.")); requireStatus(appointment, "WAITING"); if (appointment.doctorUserId != null) throw new RuleException("Esta consulta já foi atribuída."); appointment.doctorUserId = doctor.id; appointment.status = "READY"; appointment.acceptedAt = LocalDateTime.now(); touch(appointment); return appointment; }

  @Transactional
  public MedicalAppointment start(Long id, AppUser doctor) { MedicalAppointment appointment = assignedDoctor(id, doctor); requireStatus(appointment, "READY"); appointment.status = "IN_PROGRESS"; appointment.startedAt = LocalDateTime.now(); touch(appointment); return appointment; }

  @Transactional
  public MedicalAppointment finish(Long id, AppUser doctor) { MedicalAppointment appointment = assignedDoctor(id, doctor); requireStatus(appointment, "IN_PROGRESS"); appointment.status = "COMPLETED"; appointment.endedAt = LocalDateTime.now(); touch(appointment); return appointment; }

  @Transactional
  public AppUser presence(AppUser doctor, boolean online) { if (!"DOCTOR".equals(doctor.role)) throw new RuleException("Apenas médicos podem alterar disponibilidade operacional."); if (!online && appointments.existsByDoctorUserIdAndStatusIn(doctor.id, DOCTOR_BUSY)) throw new RuleException("Não é possível ficar offline durante um atendimento ativo."); doctor.online = online; return demo.users.save(doctor); }

  @Transactional
  public DoctorAvailabilitySlot createSlot(AppUser doctor, LocalDateTime startsAt, LocalDateTime endsAt) { if (!"DOCTOR".equals(doctor.role)) throw new RuleException("Apenas médicos podem criar horários."); if (startsAt == null || endsAt == null || !startsAt.isAfter(LocalDateTime.now()) || !endsAt.isAfter(startsAt) || Duration.between(startsAt, endsAt).toMinutes() < 15) throw new RuleException("Informe um horário futuro com duração mínima de 15 minutos."); if (slots.existsByDoctorUserIdAndStartsAtLessThanAndEndsAtGreaterThan(doctor.id, endsAt, startsAt)) throw new RuleException("O horário conflita com outra disponibilidade."); DoctorAvailabilitySlot slot = new DoctorAvailabilitySlot(); slot.doctorUserId = doctor.id; slot.startsAt = startsAt; slot.endsAt = endsAt; return slots.save(slot); }

  public List<DoctorAvailabilitySlot> availableSlots(AppUser user) { if ("DOCTOR".equals(user.role)) return slots.findByDoctorUserIdAndStartsAtAfterOrderByStartsAt(user.id, LocalDateTime.now().minusMinutes(1)); return slots.findByStatusAndStartsAtAfterOrderByStartsAt("AVAILABLE", LocalDateTime.now()); }

  @Transactional
  public MedicalAppointment bookSlot(AppUser patient, Long slotId) { DoctorAvailabilitySlot slot = slots.findLockedById(slotId).orElseThrow(() -> new RuleException("Horário não encontrado.")); if (!"AVAILABLE".equals(slot.status) || !slot.startsAt.isAfter(LocalDateTime.now())) throw new RuleException("Este horário não está mais disponível."); MedicalAppointment appointment = base(patient.id); appointment.doctorUserId = slot.doctorUserId; appointment.status = "PAYMENT_PENDING"; appointment.scheduledStart = slot.startsAt; appointment.scheduledEnd = slot.endsAt; appointment = appointments.save(appointment); slot.status = "BOOKED"; slot.appointmentId = appointment.id; slots.save(slot); return appointment; }

  @Transactional
  public DoctorAvailabilitySlot cancelSlot(AppUser doctor, Long slotId) { DoctorAvailabilitySlot slot = slots.findLockedById(slotId).orElseThrow(() -> new RuleException("Horário não encontrado.")); if (!slot.doctorUserId.equals(doctor.id)) throw new RuleException("Você não pode alterar este horário."); if (!"AVAILABLE".equals(slot.status)) throw new RuleException("Um horário reservado não pode ser cancelado por este fluxo."); slot.status = "CANCELLED"; return slot; }

  private MedicalAppointment base(Long patientId) { Map<String, Object> settings = demo.payload(demo.records.findByTypeOrderByCreatedAtDesc("SETTING").getFirst()); MedicalAppointment appointment = new MedicalAppointment(); appointment.reference = "FA-CONS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT); appointment.patientUserId = patientId; appointment.priceAtPurchase = decimal(settings.get("consultationPrice")); appointment.doctorPayoutAtPurchase = decimal(settings.get("doctorBasePayout")); return appointment; }
  private BigDecimal decimal(Object value) { return new BigDecimal(String.valueOf(value)); }
  private MedicalAppointment ownedPatient(Long id, AppUser patient) { MedicalAppointment appointment = appointments.findById(id).orElseThrow(() -> new RuleException("Consulta não encontrada.")); if (!"PATIENT".equals(patient.role) || !appointment.patientUserId.equals(patient.id)) throw new RuleException("Acesso não autorizado à consulta."); return appointment; }
  private MedicalAppointment assignedDoctor(Long id, AppUser doctor) { MedicalAppointment appointment = appointments.findLockedById(id).orElseThrow(() -> new RuleException("Consulta não encontrada.")); if (!"DOCTOR".equals(doctor.role) || !doctor.id.equals(appointment.doctorUserId)) throw new RuleException("Acesso não autorizado à consulta."); return appointment; }
  private void requireStatus(MedicalAppointment appointment, String expected) { if (!expected.equals(appointment.status)) throw new RuleException("A consulta não está no estado esperado: " + expected + "."); }
  private void touch(MedicalAppointment appointment) { appointment.updatedAt = LocalDateTime.now(); }
  public static class RuleException extends RuntimeException { public RuleException(String message) { super(message); } }
}
