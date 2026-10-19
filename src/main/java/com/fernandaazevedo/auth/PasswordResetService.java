package com.fernandaazevedo.auth;

import com.fernandaazevedo.common.DemoService;
import com.fernandaazevedo.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PasswordResetService {
  private final PasswordResetTokenRepository tokens;
  private final DemoService demo;
  private final SecureRandom random = new SecureRandom();

  public PasswordResetService(PasswordResetTokenRepository tokens, DemoService demo) {
    this.tokens = tokens; this.demo = demo;
  }

  @Transactional
  public Optional<String> create(String email) {
    return demo.users.findByEmailIgnoreCase(String.valueOf(email).trim()).map(user -> {
      byte[] bytes = new byte[32]; random.nextBytes(bytes);
      String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
      PasswordResetToken token = new PasswordResetToken();
      token.userId = user.id; token.tokenHash = hash(raw); token.expiresAt = LocalDateTime.now().plusMinutes(30);
      tokens.save(token);
      demo.audit(user.email, "PASSWORD_RESET_REQUESTED", "USER", null, "token-expira-em-30-minutos");
      return raw;
    });
  }

  @Transactional
  public boolean reset(String rawToken, String password) {
    if (rawToken == null || rawToken.isBlank()) return false;
    Optional<PasswordResetToken> found = tokens.findByTokenHash(hash(rawToken));
    if (found.isEmpty()) return false;
    PasswordResetToken token = found.get();
    if (token.usedAt != null || token.expiresAt.isBefore(LocalDateTime.now())) return false;
    Optional<AppUser> user = demo.users.findById(token.userId);
    if (user.isEmpty() || !"ACTIVE".equals(user.get().status)) return false;
    AppUser account = user.get(); account.passwordHash = demo.encoder.encode(password); account.temporaryPassword = false;
    demo.users.save(account); token.usedAt = LocalDateTime.now(); tokens.save(token);
    demo.audit(account.email, "PASSWORD_RESET", "USER", null, null);
    return true;
  }

  private String hash(String value) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
  }
}
