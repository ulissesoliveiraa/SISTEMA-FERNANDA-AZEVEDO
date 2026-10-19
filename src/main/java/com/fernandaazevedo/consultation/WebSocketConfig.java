package com.fernandaazevedo.consultation;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
  private final ConsultationSignalingHandler signalingHandler;
  private final com.fernandaazevedo.support.SupportWebSocketHandler supportHandler;

  public WebSocketConfig(ConsultationSignalingHandler signalingHandler, com.fernandaazevedo.support.SupportWebSocketHandler supportHandler) { this.signalingHandler = signalingHandler; this.supportHandler = supportHandler; }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(signalingHandler, "/ws/consultations/{consultationId}")
      .addInterceptors(new HttpSessionHandshakeInterceptor());
    registry.addHandler(supportHandler, "/ws/tickets/{ticketId}")
      .addInterceptors(new HttpSessionHandshakeInterceptor());
  }
}
