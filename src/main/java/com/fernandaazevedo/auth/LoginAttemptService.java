package com.fernandaazevedo.auth;

import org.springframework.stereotype.Service;
import java.time.*;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
  private static final int MAX_FAILURES = 5;
  private static final Duration WINDOW = Duration.ofMinutes(15);
  private static final Duration BLOCK = Duration.ofMinutes(15);
  private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

  public boolean blocked(String email, String address) {
    String key = key(email, address); Attempt current = attempts.get(key); if (current == null) return false;
    Instant now = Instant.now();
    if (current.blockedUntil != null && current.blockedUntil.isAfter(now)) return true;
    if (current.windowStarted.plus(WINDOW).isBefore(now)) attempts.remove(key, current);
    return false;
  }

  public void failed(String email, String address) {
    String key = key(email, address); Instant now = Instant.now();
    attempts.compute(key, (ignored, current) -> {
      if (current == null || current.windowStarted.plus(WINDOW).isBefore(now)) current = new Attempt(0, now, null);
      int failures = current.failures + 1;
      return new Attempt(failures, current.windowStarted, failures >= MAX_FAILURES ? now.plus(BLOCK) : current.blockedUntil);
    });
  }

  public void succeeded(String email, String address) { attempts.remove(key(email, address)); }
  private String key(String email, String address) { return String.valueOf(email).strip().toLowerCase(Locale.ROOT) + "|" + String.valueOf(address); }
  private record Attempt(int failures, Instant windowStarted, Instant blockedUntil) {}
}
