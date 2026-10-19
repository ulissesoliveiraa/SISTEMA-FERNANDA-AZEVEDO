package com.fernandaazevedo.support;
import com.fernandaazevedo.domain.*; import org.springframework.stereotype.Service; import java.util.*;
@Service public class SupportAccessService {
 private final AppUserRepository users; private final SupportTicketRepository tickets;
 public SupportAccessService(AppUserRepository users,SupportTicketRepository tickets){this.users=users;this.tickets=tickets;}
 public Optional<Access> authorize(Long ticketId,Long userId,boolean reply){if(userId==null)return Optional.empty();var u=users.findById(userId).orElse(null);var t=tickets.findById(ticketId).orElse(null);if(u==null||t==null||!"ACTIVE".equals(u.status))return Optional.empty();boolean staff="ADMIN".equals(u.role)||("SUPPORT".equals(u.role)&&u.permissions.contains(reply?"support.tickets.reply":"support.tickets.view"));return t.requesterId.equals(u.id)||staff?Optional.of(new Access(u,t,staff)):Optional.empty();}
 public record Access(AppUser user,SupportTicket ticket,boolean staff){}
}
