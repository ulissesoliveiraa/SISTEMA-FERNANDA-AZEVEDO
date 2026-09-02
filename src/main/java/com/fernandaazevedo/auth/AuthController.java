package com.fernandaazevedo.auth;

import com.fernandaazevedo.common.DemoService;
import com.fernandaazevedo.domain.AppUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final DemoService d; public AuthController(DemoService d){this.d=d;}
  @PostMapping("/login") public ResponseEntity<?> login(@RequestBody Map<String,String> b,HttpSession s){
    var found=d.users.findByEmailIgnoreCase(b.getOrDefault("email",""));
    if(found.isEmpty()||!d.encoder.matches(b.getOrDefault("password",""),found.get().passwordHash)){d.audit(b.getOrDefault("email","anonymous"),"LOGIN_FAILED","SESSION",null,null);return ResponseEntity.status(401).body(Map.of("message","E-mail ou senha inválidos."));}
    AppUser u=found.get(); if(!u.status.equals("ACTIVE")) return ResponseEntity.status(403).body(Map.of("message",u.status.equals("BLOCKED")?"Usuário bloqueado.":"Usuário desativado."));
    s.setAttribute("uid",u.id);u.lastAccess=LocalDateTime.now();d.users.save(u);d.audit(u.email,"LOGIN_SUCCESS","SESSION",null,u.role);return ResponseEntity.ok(view(u));
  }
  @PostMapping("/logout") public Map<String,Object> logout(HttpSession s){s.invalidate();return Map.of("ok",true);}
  @GetMapping("/me") public ResponseEntity<?> me(HttpSession s){Object id=s.getAttribute("uid");return id==null?ResponseEntity.status(401).body(Map.of("message","Sessão expirada.")):d.users.findById((Long)id).map(u->ResponseEntity.ok(view(u))).orElse(ResponseEntity.notFound().build());}
  @PostMapping("/forgot-password") public Map<String,Object> forgot(@RequestBody Map<String,String>b){return Map.of("message","Se o e-mail estiver cadastrado, enviaremos as instruções de redefinição.","demoToken","DEMO-RESET");}
  @PostMapping("/register") public ResponseEntity<?> register(@RequestBody Map<String,String>b){
    String email=b.getOrDefault("email","").trim().toLowerCase(),password=b.getOrDefault("password","");
    if(d.users.findByEmailIgnoreCase(email).isPresent()) return ResponseEntity.status(409).body(Map.of("message","Não foi possível concluir o cadastro com os dados informados."));
    if(!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) return ResponseEntity.badRequest().body(Map.of("message","A senha não atende aos requisitos."));
    AppUser u=new AppUser();u.name=b.getOrDefault("name","").trim();u.email=email;u.phone=b.get("phone");u.document=b.get("document");u.role="PATIENT";u.passwordHash=d.encoder.encode(password);d.users.save(u);
    d.save("CONSENT",email,"ACCEPTED","TERMS-2.1",Map.of("documentType","Termos de Uso e Política de Privacidade","version","2.1","acceptedAt",LocalDateTime.now().toString(),"source","WEB","ipMock","192.0.2.10"));d.audit(email,"USER_REGISTERED","USER",null,email);
    return ResponseEntity.ok(Map.of("message","Conta criada com sucesso."));
  }
  @PostMapping("/reset-password") public ResponseEntity<?> reset(@RequestBody Map<String,String>b){String p=b.getOrDefault("password","");if(!p.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"))return ResponseEntity.badRequest().body(Map.of("message","A senha não atende aos requisitos."));return d.users.findByEmailIgnoreCase(b.getOrDefault("email","" )).map(u->{u.passwordHash=d.encoder.encode(p);u.temporaryPassword=false;d.users.save(u);d.audit(u.email,"PASSWORD_RESET","USER",null,null);return ResponseEntity.ok(Map.of("message","Senha alterada com sucesso."));}).orElse(ResponseEntity.ok(Map.of("message","Senha alterada com sucesso.")));}
  private Map<String,Object> view(AppUser u){return Map.of("id",u.id,"name",u.name,"email",u.email,"role",u.role,"status",u.status,"permissions",u.permissions,"temporaryPassword",u.temporaryPassword);}
}
