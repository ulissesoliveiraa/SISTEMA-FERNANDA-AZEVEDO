package com.fernandaazevedo.consultation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessage, Long> {
  List<ConsultationMessage> findByConsultationIdOrderByCreatedAtAsc(Long consultationId);
}
