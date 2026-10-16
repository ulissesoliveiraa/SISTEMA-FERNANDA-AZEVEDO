package com.fernandaazevedo.consultation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConsultationSignalingHandler extends TextWebSocketHandler {
  private static final Set<String> RELAY_TYPES = Set.of("offer", "answer", "ice-candidate", "media-state", "leave");
  private final ConsultationAccessService accessService;
  private final ConsultationMessageRepository messages;
  private final ObjectMapper json;
  private final Map<Long, Map<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();

  public ConsultationSignalingHandler(ConsultationAccessService accessService, ConsultationMessageRepository messages, ObjectMapper json) {
    this.accessService = accessService;
    this.messages = messages;
    this.json = json;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    Long userId = session.getAttributes().get("uid") instanceof Long id ? id : null;
    Long consultationId = consultationId(session);
    var access = accessService.authorize(consultationId, userId);
    if (access.isEmpty()) {
      session.close(new CloseStatus(4403, "Acesso não autorizado à consulta"));
      return;
    }
    String role = access.get().participantRole();
    session.getAttributes().put("consultationId", consultationId);
    session.getAttributes().put("participantRole", role);
    session.getAttributes().put("userId", access.get().user().id);
    session.getAttributes().put("userName", access.get().user().name);
    Map<String, WebSocketSession> room = rooms.computeIfAbsent(consultationId, ignored -> new ConcurrentHashMap<>());
    WebSocketSession previous = room.putIfAbsent(role, session);
    if (previous != null && previous.isOpen()) {
      session.close(new CloseStatus(4409, "Este participante já está conectado"));
      return;
    }
    if (previous != null) room.put(role, session);
    send(session, Map.of("type", "connected", "role", role, "consultationId", consultationId, "at", Instant.now().toString()));
    broadcast(consultationId, Map.of("type", "participant-joined", "role", role), session);
    if (room.size() == 2) broadcast(consultationId, Map.of("type", "peer-ready"), null);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    if (message.getPayloadLength() > 65536) {
      session.close(CloseStatus.TOO_BIG_TO_PROCESS);
      return;
    }
    JsonNode body = json.readTree(message.getPayload());
    String type = body.path("type").asText("");
    if ("chat-message".equals(type)) { persistAndBroadcastMessage(session, body); return; }
    if ("chat-read".equals(type)) { markMessageRead(session, body); return; }
    if (!RELAY_TYPES.contains(type)) return;
    Long consultationId = (Long) session.getAttributes().get("consultationId");
    String role = String.valueOf(session.getAttributes().get("participantRole"));
    Map<String, Object> relay = new ConcurrentHashMap<>();
    relay.put("type", type);
    relay.put("from", role);
    if (body.has("description")) relay.put("description", json.convertValue(body.get("description"), Object.class));
    if (body.has("candidate")) relay.put("candidate", json.convertValue(body.get("candidate"), Object.class));
    if (body.has("audio")) relay.put("audio", body.get("audio").asBoolean());
    if (body.has("video")) relay.put("video", body.get("video").asBoolean());
    broadcast(consultationId, relay, session);
  }

  private void persistAndBroadcastMessage(WebSocketSession session, JsonNode body) throws IOException {
    String content = body.path("content").asText("").strip();
    if (content.isBlank() || content.length() > 4000) {
      send(session, Map.of("type", "chat-error", "message", "A mensagem deve ter entre 1 e 4000 caracteres."));
      return;
    }
    Long consultationId = (Long) session.getAttributes().get("consultationId");
    Map<String, WebSocketSession> room = rooms.get(consultationId);
    ConsultationMessage stored = new ConsultationMessage();
    stored.consultationId = consultationId;
    stored.senderUserId = (Long) session.getAttributes().get("userId");
    stored.senderName = String.valueOf(session.getAttributes().get("userName"));
    stored.senderRole = String.valueOf(session.getAttributes().get("participantRole"));
    stored.content = content;
    if (room != null && room.size() == 2) {
      stored.deliveryStatus = "DELIVERED";
      stored.deliveredAt = LocalDateTime.now();
    }
    stored = messages.save(stored);
    Map<String, Object> event = new LinkedHashMap<>();
    event.put("type", "chat-message");
    event.put("id", stored.id);
    event.put("senderUserId", stored.senderUserId);
    event.put("senderName", stored.senderName);
    event.put("senderRole", stored.senderRole);
    event.put("content", stored.content);
    event.put("deliveryStatus", stored.deliveryStatus);
    event.put("createdAt", stored.createdAt.toString());
    broadcast(consultationId, event, null);
  }

  private void markMessageRead(WebSocketSession session, JsonNode body) throws IOException {
    long messageId = body.path("messageId").asLong(-1);
    Long consultationId = (Long) session.getAttributes().get("consultationId");
    Long readerId = (Long) session.getAttributes().get("userId");
    var found = messages.findById(messageId);
    if (found.isEmpty()) return;
    ConsultationMessage stored = found.get();
    if (!stored.consultationId.equals(consultationId) || stored.senderUserId.equals(readerId)) return;
    stored.deliveryStatus = "READ";
    stored.readAt = LocalDateTime.now();
    messages.save(stored);
    broadcast(consultationId, Map.of("type", "chat-read", "messageId", stored.id, "readAt", stored.readAt.toString()), null);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    Long consultationId = (Long) session.getAttributes().get("consultationId");
    String role = String.valueOf(session.getAttributes().get("participantRole"));
    if (consultationId == null) return;
    Map<String, WebSocketSession> room = rooms.get(consultationId);
    if (room != null) {
      room.remove(role, session);
      if (room.isEmpty()) rooms.remove(consultationId, room);
    }
    broadcast(consultationId, Map.of("type", "participant-left", "role", role), session);
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    if (session.isOpen()) session.close(CloseStatus.SERVER_ERROR);
  }

  private Long consultationId(WebSocketSession session) {
    try {
      String path = session.getUri().getPath();
      return Long.valueOf(path.substring(path.lastIndexOf('/') + 1));
    } catch (Exception ignored) { return null; }
  }

  private void broadcast(Long consultationId, Map<String, ?> payload, WebSocketSession excluded) throws IOException {
    Map<String, WebSocketSession> room = rooms.get(consultationId);
    if (room == null) return;
    TextMessage message = new TextMessage(json.writeValueAsString(payload));
    for (WebSocketSession participant : room.values()) {
      if (participant != excluded && participant.isOpen()) participant.sendMessage(message);
    }
  }

  private void send(WebSocketSession session, Map<String, ?> payload) throws IOException {
    if (session.isOpen()) session.sendMessage(new TextMessage(json.writeValueAsString(payload)));
  }
}
