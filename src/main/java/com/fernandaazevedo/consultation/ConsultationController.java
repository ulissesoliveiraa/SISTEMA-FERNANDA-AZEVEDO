package com.fernandaazevedo.consultation;

import com.fernandaazevedo.attachments.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;

import java.util.*;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {
  private final ConsultationAccessService accessService;
  private final ConsultationMessageRepository messages;
  private final AttachmentRepository attachments;
  private final PrivateAttachmentService attachmentService;
  private final String turnUrl;
  private final String turnUsername;
  private final String turnCredential;

  public ConsultationController(ConsultationAccessService accessService, ConsultationMessageRepository messages,
      AttachmentRepository attachments, PrivateAttachmentService attachmentService,
      @Value("${webrtc.turn.url:}") String turnUrl,
      @Value("${webrtc.turn.username:}") String turnUsername,
      @Value("${webrtc.turn.credential:}") String turnCredential) {
    this.accessService = accessService;
    this.messages = messages;
    this.attachments = attachments;
    this.attachmentService = attachmentService;
    this.turnUrl = turnUrl;
    this.turnUsername = turnUsername;
    this.turnCredential = turnCredential;
  }

  @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> upload(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files, HttpSession session) {
    Long userId = session.getAttribute("uid") instanceof Long uid ? uid : null;
    var access = accessService.authorize(id, userId);
    if (access.isEmpty()) return ResponseEntity.status(403).body(Map.of("message", "Acesso não autorizado aos anexos desta consulta."));
    if (files.isEmpty() || files.size() > 10) return ResponseEntity.badRequest().body(Map.of("message", "Envie entre 1 e 10 arquivos por vez."));
    try {
      List<Map<String, Object>> stored = new ArrayList<>();
      for (MultipartFile file : files) stored.add(attachmentView(attachmentService.store(id, userId, file)));
      HttpStatus status = stored.stream().allMatch(item -> "CLEAN".equals(item.get("scanStatus"))) ? HttpStatus.CREATED : HttpStatus.ACCEPTED;
      return ResponseEntity.status(status).body(stored);
    } catch (PrivateAttachmentService.InvalidAttachmentException error) {
      return ResponseEntity.badRequest().body(Map.of("message", error.getMessage()));
    } catch (Exception error) {
      return ResponseEntity.internalServerError().body(Map.of("message", "Não foi possível armazenar o arquivo com segurança."));
    }
  }

  @GetMapping("/{id}/attachments")
  public ResponseEntity<?> attachments(@PathVariable Long id, HttpSession session) {
    Long userId = session.getAttribute("uid") instanceof Long uid ? uid : null;
    if (accessService.authorize(id, userId).isEmpty()) return ResponseEntity.status(403).body(Map.of("message", "Acesso não autorizado aos anexos desta consulta."));
    return ResponseEntity.ok(attachments.findByConsultationIdOrderByCreatedAtAsc(id).stream().map(this::attachmentView).toList());
  }

  @GetMapping("/{id}/attachments/{attachmentId}/download")
  public ResponseEntity<?> download(@PathVariable Long id, @PathVariable String attachmentId, HttpSession session) {
    Long userId = session.getAttribute("uid") instanceof Long uid ? uid : null;
    if (accessService.authorize(id, userId).isEmpty()) return ResponseEntity.status(403).body(Map.of("message", "Acesso não autorizado ao arquivo."));
    var found = attachments.findById(attachmentId);
    if (found.isEmpty() || !found.get().consultationId.equals(id)) return ResponseEntity.notFound().build();
    try {
      Attachment attachment = found.get();
      return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(attachment.originalName, java.nio.charset.StandardCharsets.UTF_8).build().toString())
        .header("X-Content-Type-Options", "nosniff")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(attachment.size)
        .body(new FileSystemResource(attachmentService.resolveClean(attachment)));
    } catch (PrivateAttachmentService.InvalidAttachmentException error) {
      return ResponseEntity.status(423).body(Map.of("message", error.getMessage()));
    }
  }

  @GetMapping("/{id}/messages")
  public ResponseEntity<?> messages(@PathVariable Long id, HttpSession session) {
    Long userId = session.getAttribute("uid") instanceof Long uid ? uid : null;
    if (accessService.authorize(id, userId).isEmpty()) return ResponseEntity.status(403).body(Map.of("message", "Acesso não autorizado ao histórico clínico."));
    return ResponseEntity.ok(messages.findByConsultationIdOrderByCreatedAtAsc(id).stream().map(this::messageView).toList());
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

  private Map<String, Object> messageView(ConsultationMessage message) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("id", message.id);
    result.put("senderUserId", message.senderUserId);
    result.put("senderName", message.senderName);
    result.put("senderRole", message.senderRole);
    result.put("messageType", message.messageType);
    result.put("content", message.content);
    result.put("attachmentId", message.attachmentId);
    result.put("deliveryStatus", message.deliveryStatus);
    result.put("createdAt", message.createdAt);
    result.put("deliveredAt", message.deliveredAt);
    result.put("readAt", message.readAt);
    return result;
  }

  private Map<String, Object> attachmentView(Attachment attachment) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("id", attachment.id);
    result.put("originalName", attachment.originalName);
    result.put("size", attachment.size);
    result.put("detectedType", attachment.detectedType);
    result.put("scanStatus", attachment.scanStatus);
    result.put("scanDetail", attachment.scanDetail);
    result.put("createdAt", attachment.createdAt);
    if ("CLEAN".equals(attachment.scanStatus)) result.put("downloadUrl", "/api/consultations/" + attachment.consultationId + "/attachments/" + attachment.id + "/download");
    return result;
  }
}
