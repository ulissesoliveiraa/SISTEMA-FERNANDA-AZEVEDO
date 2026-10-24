package com.fernandaazevedo.appointments;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_appointments", indexes = {
  @Index(name = "idx_appointment_patient", columnList = "patientUserId,createdAt"),
  @Index(name = "idx_appointment_doctor", columnList = "doctorUserId,scheduledStart"),
  @Index(name = "idx_appointment_status", columnList = "status,queueEnteredAt")
})
public class MedicalAppointment {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
  @Column(nullable = false, unique = true) public String reference;
  @Column(nullable = false) public Long patientUserId;
  public Long doctorUserId;
  @Column(nullable = false) public String status;
  public LocalDateTime scheduledStart;
  public LocalDateTime scheduledEnd;
  public LocalDateTime queueEnteredAt;
  public LocalDateTime acceptedAt;
  public LocalDateTime startedAt;
  public LocalDateTime endedAt;
  public Long endedByUserId;
  public String endedByRole;
  public String completionReason;
  @Column(nullable = false, precision = 10, scale = 2) public BigDecimal priceAtPurchase;
  @Column(nullable = false, precision = 10, scale = 2) public BigDecimal doctorPayoutAtPurchase;
  @Column(columnDefinition = "TEXT") public String preAnamnesis;
  public int anamnesisVersion;
  public String cancellationReason;
  @Column(nullable = false) public LocalDateTime createdAt = LocalDateTime.now();
  @Column(nullable = false) public LocalDateTime updatedAt = LocalDateTime.now();
  @Version public long version;
}
