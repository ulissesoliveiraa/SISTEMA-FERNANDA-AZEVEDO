package com.fernandaazevedo.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandaazevedo.domain.*;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DemoService {
  public final AppUserRepository users; public final DomainRecordRepository records;
  public final ObjectMapper json; public final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);
  public DemoService(AppUserRepository u,DomainRecordRepository r,ObjectMapper j){users=u;records=r;json=j;}
  @PostConstruct public void seed(){
    if(users.count()==0){
      createSeed("João Almeida","paciente@fernandaazevedo.demo","PATIENT","");
      createSeed("Dra. Fernanda Azevedo","medico@fernandaazevedo.demo","DOCTOR","");
      createSeed("Marina Costa","suporte@fernandaazevedo.demo","SUPPORT","support.tickets.view,support.tickets.reply,support.tickets.transfer,support.tickets.close,support.tickets.reopen,support.transcript.send,support.attachments.upload,users.view,users.create,users.edit,consents.view_status");
      createSeed("Administrador Demo","admin@fernandaazevedo.demo","ADMIN","*");
    }
    if(records.findByTypeOrderByCreatedAtDesc("SETTING").isEmpty()) save("SETTING","PUBLIC","ACTIVE","commercial",Map.of("consultationPrice",69.90,"doctorBasePayout",50.00,"currency","BRL","priceText","Consulta online com valor acessível","checkoutEnabled",true));
    if(records.findByTypeOrderByCreatedAtDesc("CAMPAIGN").isEmpty()) save("CAMPAIGN","PUBLIC","PUBLISHED","CAMP-001",Map.of("title","Seu cuidado em um só lugar","subtitle","Consulta, histórico e documentos com uma jornada simples.","ctaLabel","Conheça a plataforma","ctaUrl","#como-funciona","position","BANNER","audience","ALL"));
    if(records.findByTypeOrderByCreatedAtDesc("APPOINTMENT").isEmpty()) save("APPOINTMENT","paciente@fernandaazevedo.demo","COMPLETED","FA-CONS-2026-0042",Map.of("doctor","Dra. Fernanda Azevedo","patient","João Almeida","date","2026-08-20","duration","28 min","complaint","Dor de cabeça leve","priceAtPurchase",69.90,"doctorPayoutAtPurchase",50.00));
    if(records.findByTypeOrderByCreatedAtDesc("TICKET").isEmpty()) save("TICKET","paciente@fernandaazevedo.demo","WAITING_SUPPORT","FA-2026-001284",Map.of("subject","Dificuldade no teste de câmera","category","Câmera e microfone","priority","NORMAL","requester","João Almeida","messages",List.of(Map.of("sender","BOT","text","Conferimos as permissões do navegador."),Map.of("sender","USER","text","Ainda preciso de ajuda."))));
    if(records.findByTypeOrderByCreatedAtDesc("CONSENT").isEmpty()) save("CONSENT","paciente@fernandaazevedo.demo","ACCEPTED","TERMS-2.1",Map.of("documentType","Termos de Uso","version","2.1","acceptedAt","2026-08-20T09:15:00","source","WEB","ipMock","192.0.2.10"));
  }
  private void createSeed(String n,String e,String role,String p){AppUser u=new AppUser();u.name=n;u.email=e;u.role=role;u.permissions=p;u.passwordHash=encoder.encode("Demo@123");users.save(u);}
  public DomainRecord save(String t,String o,String s,String ref,Object payload){DomainRecord r=new DomainRecord();r.type=t;r.owner=o;r.status=s;r.reference=ref;try{r.payload=json.writeValueAsString(payload);}catch(Exception e){throw new IllegalArgumentException(e);}return records.save(r);}
  public Map<String,Object> payload(DomainRecord r){try{return json.readValue(r.payload,new TypeReference<>(){});}catch(Exception e){return new HashMap<>();}}
  public void audit(String actor,String action,String resource,Object before,Object after){save("AUDIT",actor,"RECORDED",resource,Map.of("action",action,"before",before==null?"":before,"after",after==null?"":after,"at",LocalDateTime.now().toString(),"ipMock","192.0.2.1"));}
}
