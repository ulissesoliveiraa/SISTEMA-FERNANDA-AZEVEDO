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

  public WebSocketConfig(ConsultationSignalingHandler signalingHandler) { this.signalingHandler = signalingHandler; }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(signalingHandler, "/ws/consultations/{consultationId}")
      .addInterceptors(new HttpSessionHandshakeInterceptor());
  }
}
