package com.fernandaazevedo.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens", indexes = @Index(name = "idx_password_reset_hash", columnList = "tokenHash", unique = true))
public class PasswordResetToken {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
  @Column(nullable = false) public Long userId;
  @Column(nullable = false, length = 64) public String tokenHash;
  @Column(nullable = false) public LocalDateTime expiresAt;
  public LocalDateTime usedAt;
  @Column(nullable = false) public LocalDateTime createdAt = LocalDateTime.now();
}
