package com.fernandaazevedo.consultation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_messages", indexes = @Index(name = "idx_consultation_message_room", columnList = "consultationId,createdAt"))
public class ConsultationMessage {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
  @Column(nullable = false) public Long consultationId;
  @Column(nullable = false) public Long senderUserId;
  @Column(nullable = false) public String senderName;
  @Column(nullable = false) public String senderRole;
  @Column(nullable = false) public String messageType = "TEXT";
  @Column(nullable = false, length = 4000) public String content;
  @Column(nullable = false) public String deliveryStatus = "SENT";
  @Column(nullable = false) public LocalDateTime createdAt = LocalDateTime.now();
  public LocalDateTime deliveredAt;
  public LocalDateTime readAt;
}
