package com.fernandaazevedo.attachments;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments", indexes = @Index(name = "idx_attachment_consultation", columnList = "consultationId,createdAt"))
public class Attachment {
  @Id public String id;
  @Column(nullable = false) public Long consultationId;
  @Column(nullable = false) public Long uploaderUserId;
  @Column(nullable = false) public String originalName;
  @Column(nullable = false, unique = true) public String storageName;
  @Column(nullable = false) public String extension;
  @Column(nullable = false) public String declaredMime;
  @Column(nullable = false) public String detectedType;
  @Column(nullable = false) public long size;
  @Column(nullable = false) public String sha256;
  @Column(nullable = false) public String scanStatus;
  @Column(length = 1000) public String scanDetail;
  @Column(nullable = false) public LocalDateTime createdAt = LocalDateTime.now();
}
