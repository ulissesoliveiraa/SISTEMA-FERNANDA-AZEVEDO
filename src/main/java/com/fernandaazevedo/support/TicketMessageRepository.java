package com.fernandaazevedo.support; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketMessageRepository extends JpaRepository<TicketMessage,Long>{List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long id);}
