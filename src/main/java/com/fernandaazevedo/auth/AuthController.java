package com.fernandaazevedo.auth;

import com.fernandaazevedo.common.DemoService;
import com.fernandaazevedo.domain.AppUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final DemoService d; private final LoginAttemptService attempts; private final PasswordResetService resets; private final boolean exposeResetToken;
  public AuthController(DemoService d, LoginAttemptService attempts, PasswordResetService resets, @Value("${app.demo.expose-reset-token:false}") boolean exposeResetToken){this.d=d;this.attempts=attempts;this.resets=resets;this.exposeResetToken=exposeResetToken;}
  @PostMapping("/login") public ResponseEntity<?> login(@RequestBody Map<String,String> b,HttpServletRequest request){
    String email=b.getOrDefault("email","").trim().toLowerCase(Locale.ROOT), address=request.getRemoteAddr();
    if(attempts.blocked(email,address)) return ResponseEntity.status(429).header("Retry-After","900").body(Map.of("message","Muitas tentativas. Aguarde alguns minutos antes de tentar novamente."));
    var found=d.users.findByEmailIgnoreCase(email);
    if(found.isEmpty()||!d.encoder.matches(b.getOrDefault("password",""),found.get().passwordHash)){attempts.failed(email,address);d.audit(email.isBlank()?"anonymous":email,"LOGIN_FAILED","SESSION",null,null);return ResponseEntity.status(401).body(Map.of("message","E-mail ou senha inválidos."));}
    AppUser u=found.get(); if(!u.status.equals("ACTIVE")) return ResponseEntity.status(403).body(Map.of("message",u.status.equals("BLOCKED")?"Usuário bloqueado.":"Usuário desativado."));
    attempts.succeeded(email,address); HttpSession old=request.getSession(false); if(old!=null)old.invalidate(); HttpSession session=request.getSession(true);
    session.setAttribute("uid",u.id);session.setAttribute("csrfToken",UUID.randomUUID().toString());session.setMaxInactiveInterval(b.containsKey("remember")?30*24*60*60:30*60);
    u.lastAccess=LocalDateTime.now();d.users.save(u);d.audit(u.email,"LOGIN_SUCCESS","SESSION",null,u.role);return ResponseEntity.ok(view(u,session));
  }
  @PostMapping("/logout") public Map<String,Object> logout(HttpSession s){Object id=s.getAttribute("uid");if(id!=null)d.users.findById((Long)id).ifPresent(u->d.audit(u.email,"LOGOUT","SESSION",null,null));s.invalidate();return Map.of("ok",true);}
  @GetMapping("/me") public ResponseEntity<?> me(HttpSession s){Object id=s.getAttribute("uid");if(id==null)return ResponseEntity.status(401).body(Map.of("message","Sessão expirada."));return d.users.findById((Long)id).map(u->{if(!"ACTIVE".equals(u.status)){s.invalidate();return ResponseEntity.status(403).body(Map.of("message","Acesso indisponível para esta conta."));}if(s.getAttribute("csrfToken")==null)s.setAttribute("csrfToken",UUID.randomUUID().toString());return ResponseEntity.ok(view(u,s));}).orElseGet(()->{s.invalidate();return ResponseEntity.status(401).body(Map.of("message","Sessão expirada."));});}
  @PostMapping("/forgot-password") public Map<String,Object> forgot(@RequestBody Map<String,String>b){Optional<String> token=resets.create(b.getOrDefault("email",""));Map<String,Object> response=new LinkedHashMap<>();response.put("message","Se o e-mail estiver cadastrado, enviaremos as instruções de redefinição.");if(exposeResetToken&&token.isPresent())response.put("demoResetUrl","/redefinir-senha?token="+token.get());return response;}
  @PostMapping("/register") public ResponseEntity<?> register(@RequestBody Map<String,String>b){
    String email=b.getOrDefault("email","").trim().toLowerCase(),password=b.getOrDefault("password","");
    if(d.users.findByEmailIgnoreCase(email).isPresent()) return ResponseEntity.status(409).body(Map.of("message","Não foi possível concluir o cadastro com os dados informados."));
    if(!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) return ResponseEntity.badRequest().body(Map.of("message","A senha não atende aos requisitos."));
    AppUser u=new AppUser();u.name=b.getOrDefault("name","").trim();u.email=email;u.phone=b.get("phone");u.document=b.get("document");u.role="PATIENT";u.passwordHash=d.encoder.encode(password);d.users.save(u);
    d.save("CONSENT",email,"ACCEPTED","TERMS-2.1",Map.of("documentType","Termos de Uso e Política de Privacidade","version","2.1","acceptedAt",LocalDateTime.now().toString(),"source","WEB","ipMock","192.0.2.10"));d.audit(email,"USER_REGISTERED","USER",null,email);
    return ResponseEntity.ok(Map.of("message","Conta criada com sucesso."));
  }
  @PostMapping("/reset-password") public ResponseEntity<?> reset(@RequestBody Map<String,String>b){String p=b.getOrDefault("password","");if(!p.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"))return ResponseEntity.badRequest().body(Map.of("message","A senha não atende aos requisitos."));if(!resets.reset(b.get("token"),p))return ResponseEntity.badRequest().body(Map.of("message","O link de redefinição é inválido, expirou ou já foi utilizado."));return ResponseEntity.ok(Map.of("message","Senha alterada com sucesso."));}
  private Map<String,Object> view(AppUser u,HttpSession session){Map<String,Object> result=new LinkedHashMap<>();result.put("id",u.id);result.put("name",u.name);result.put("email",u.email);result.put("role",u.role);result.put("status",u.status);result.put("permissions",u.permissions);result.put("temporaryPassword",u.temporaryPassword);result.put("online",u.online);result.put("csrfToken",session.getAttribute("csrfToken"));return result;}
}
