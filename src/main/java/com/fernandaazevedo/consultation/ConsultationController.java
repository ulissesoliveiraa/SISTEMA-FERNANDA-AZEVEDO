package com.fernandaazevedo.consultation;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {
  private final ConsultationAccessService accessService;
  private final String turnUrl;
  private final String turnUsername;
  private final String turnCredential;

  public ConsultationController(ConsultationAccessService accessService,
      @Value("${webrtc.turn.url:}") String turnUrl,
      @Value("${webrtc.turn.username:}") String turnUsername,
      @Value("${webrtc.turn.credential:}") String turnCredential) {
    this.accessService = accessService;
    this.turnUrl = turnUrl;
    this.turnUsername = turnUsername;
    this.turnCredential = turnCredential;
  }

  @GetMapping("/{id}/session")
  public ResponseEntity<?> session(@PathVariable Long id, HttpSession session) {
    Long userId = session.getAttribute("uid") instanceof Long uid ? uid : null;
    return accessService.authorize(id, userId)
      .<ResponseEntity<?>>map(access -> ResponseEntity.ok(Map.of(
        "consultationId", id,
        "reference", access.appointment().reference,
        "role", access.participantRole(),
        "iceServers", iceServers(),
        "turnConfigured", !turnUrl.isBlank()
      )))
      .orElseGet(() -> ResponseEntity.status(403).body(Map.of("message", "Você não participa desta consulta ou ela já foi encerrada.")));
  }

  private List<Map<String, Object>> iceServers() {
    List<Map<String, Object>> servers = new ArrayList<>();
    servers.add(Map.of("urls", List.of("stun:stun.l.google.com:19302", "stun:stun1.l.google.com:19302")));
    if (!turnUrl.isBlank() && !turnUsername.isBlank() && !turnCredential.isBlank()) {
      servers.add(Map.of("urls", turnUrl, "username", turnUsername, "credential", turnCredential));
    }
    return servers;
  }
}
