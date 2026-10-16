package com.fernandaazevedo.appointments;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_availability_slots", uniqueConstraints = @UniqueConstraint(name = "uk_doctor_slot_start", columnNames = {"doctorUserId", "startsAt"}))
public class DoctorAvailabilitySlot {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
  @Column(nullable = false) public Long doctorUserId;
  @Column(nullable = false) public LocalDateTime startsAt;
  @Column(nullable = false) public LocalDateTime endsAt;
  @Column(nullable = false) public String status = "AVAILABLE";
  public Long appointmentId;
  @Column(nullable = false) public LocalDateTime createdAt = LocalDateTime.now();
  @Version public long version;
}
