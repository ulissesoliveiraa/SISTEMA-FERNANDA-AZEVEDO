package com.fernandaazevedo.consultation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandaazevedo.domain.AppUser;
import com.fernandaazevedo.domain.DomainRecord;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConsultationSignalingHandlerTest {
  @Test
  void twoAuthorizedParticipantsReceiveReadyAndRelaySignals() throws Exception {
    ConsultationAccessService access = mock(ConsultationAccessService.class);
    ConsultationMessageRepository messageRepository = mock(ConsultationMessageRepository.class);
    DomainRecord appointment = new DomainRecord(); appointment.id = 77L; appointment.reference = "FA-LIVE-TEST";
    AppUser patientUser = user(1L, "PATIENT");
    AppUser doctorUser = user(2L, "DOCTOR");
    when(access.authorize(77L, 1L)).thenReturn(Optional.of(new ConsultationAccessService.RoomAccess(appointment, patientUser, "PATIENT")));
    when(access.authorize(77L, 2L)).thenReturn(Optional.of(new ConsultationAccessService.RoomAccess(appointment, doctorUser, "DOCTOR")));

    Session patient = session(1L);
    Session doctor = session(2L);
    ConsultationSignalingHandler handler = new ConsultationSignalingHandler(access, messageRepository, new ObjectMapper());
    handler.afterConnectionEstablished(patient.socket);
    handler.afterConnectionEstablished(doctor.socket);

    assertTrue(patient.messages.stream().anyMatch(m -> m.contains("\"type\":\"peer-ready\"")));
    assertTrue(doctor.messages.stream().anyMatch(m -> m.contains("\"type\":\"peer-ready\"")));

    handler.handleTextMessage(doctor.socket, new TextMessage("{\"type\":\"media-state\",\"audio\":true,\"video\":false}"));
    assertTrue(patient.messages.stream().anyMatch(m -> m.contains("\"type\":\"media-state\"") && m.contains("\"from\":\"DOCTOR\"") && m.contains("\"audio\":true")));

    when(messageRepository.save(any(ConsultationMessage.class))).thenAnswer(invocation -> { ConsultationMessage message = invocation.getArgument(0); message.id = 9L; return message; });
    handler.handleTextMessage(patient.socket, new TextMessage("{\"type\":\"chat-message\",\"content\":\"Como você está?\"}"));
    verify(messageRepository).save(argThat(message -> message.consultationId.equals(77L) && message.senderUserId.equals(1L) && message.content.equals("Como você está?") && message.deliveryStatus.equals("DELIVERED")));
    assertTrue(doctor.messages.stream().anyMatch(m -> m.contains("\"type\":\"chat-message\"") && m.contains("Como você está?")));
  }

  @Test
  void unauthenticatedSocketIsClosedWithoutJoiningRoom() throws Exception {
    ConsultationAccessService access = mock(ConsultationAccessService.class);
    ConsultationMessageRepository messageRepository = mock(ConsultationMessageRepository.class);
    when(access.authorize(77L, null)).thenReturn(Optional.empty());
    Session anonymous = session(null);
    ConsultationSignalingHandler handler = new ConsultationSignalingHandler(access, messageRepository, new ObjectMapper());
    handler.afterConnectionEstablished(anonymous.socket);
    verify(anonymous.socket).close(argThat(status -> status.getCode() == 4403));
    assertTrue(anonymous.messages.isEmpty());
  }

  private static AppUser user(Long id, String role) { AppUser user = new AppUser(); user.id = id; user.role = role; return user; }

  private static Session session(Long uid) throws Exception {
    WebSocketSession socket = mock(WebSocketSession.class);
    Map<String, Object> attributes = new HashMap<>();
    if (uid != null) attributes.put("uid", uid);
    List<String> messages = new ArrayList<>();
    when(socket.getAttributes()).thenReturn(attributes);
    when(socket.getUri()).thenReturn(URI.create("ws://localhost/ws/consultations/77"));
    when(socket.isOpen()).thenReturn(true);
    doAnswer(invocation -> { messages.add(((TextMessage) invocation.getArgument(0)).getPayload()); return null; })
      .when(socket).sendMessage(any(TextMessage.class));
    return new Session(socket, messages);
  }

  private record Session(WebSocketSession socket, List<String> messages) {}
}
