package com.fernandaazevedo.support; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface SupportTicketRepository extends JpaRepository<SupportTicket,Long>{List<SupportTicket> findByRequesterIdOrderByUpdatedAtDesc(Long id);}
