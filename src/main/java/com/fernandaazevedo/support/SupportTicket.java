package com.fernandaazevedo.support;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="support_tickets") public class SupportTicket {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(unique=true,nullable=false) public String ticketNumber;
 @Column(nullable=false) public Long requesterId; public String requesterName; public String requesterRole; @Column(nullable=false,length=300) public String subject;
 public String category; public String priority="NORMAL"; public String status="WAITING_SUPPORT"; public Long assignedSupportId; public String assignedSupportName;
 public LocalDateTime createdAt=LocalDateTime.now(); public LocalDateTime updatedAt=LocalDateTime.now(); public LocalDateTime resolvedAt;
 public String getReference(){return ticketNumber;} public String getRequester(){return requesterName;}
}
