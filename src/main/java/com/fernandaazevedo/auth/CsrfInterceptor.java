package com.fernandaazevedo.auth;

import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Set;

@Component
public class CsrfInterceptor implements HandlerInterceptor {
  private static final Set<String> SAFE = Set.of("GET", "HEAD", "OPTIONS");
  @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
    if (SAFE.contains(request.getMethod())) return true;
    HttpSession session = request.getSession(false);
    String expected = session == null ? null : (String) session.getAttribute("csrfToken");
    String supplied = request.getHeader("X-CSRF-Token");
    if (expected != null && supplied != null && MessageDigest.isEqual(expected.getBytes(java.nio.charset.StandardCharsets.UTF_8), supplied.getBytes(java.nio.charset.StandardCharsets.UTF_8))) return true;
    response.setStatus(403); response.setContentType(MediaType.APPLICATION_JSON_VALUE); response.getWriter().write("{\"message\":\"A validação de segurança da sessão falhou. Atualize a página e tente novamente.\"}"); return false;
  }
}
