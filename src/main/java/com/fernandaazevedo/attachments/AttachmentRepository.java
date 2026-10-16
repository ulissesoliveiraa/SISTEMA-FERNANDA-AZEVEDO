package com.fernandaazevedo.attachments;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, String> {
  List<Attachment> findByConsultationIdOrderByCreatedAtAsc(Long consultationId);
}
