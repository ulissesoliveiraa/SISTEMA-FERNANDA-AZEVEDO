package com.fernandaazevedo.domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface DomainRecordRepository extends JpaRepository<DomainRecord,Long> {
  List<DomainRecord> findByTypeOrderByCreatedAtDesc(String type);
  List<DomainRecord> findByTypeAndOwnerOrderByCreatedAtDesc(String type,String owner);
  Optional<DomainRecord> findByTypeAndReference(String type,String reference);
}
