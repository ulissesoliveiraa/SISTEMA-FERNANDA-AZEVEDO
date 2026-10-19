package com.fernandaazevedo.support;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="ticket_messages",indexes=@Index(name="idx_ticket_message",columnList="ticketId,createdAt")) public class TicketMessage {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(nullable=false) public Long ticketId; @Column(nullable=false) public Long senderId;
 public String senderName; public String senderRole; @Column(nullable=false,length=4000) public String content; public boolean internalNote; public String messageType="TEXT";
 public LocalDateTime createdAt=LocalDateTime.now();
}
