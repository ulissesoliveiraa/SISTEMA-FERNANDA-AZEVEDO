package com.fernandaazevedo.domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DomainRecordRepository extends JpaRepository<DomainRecord,Long> {
  List<DomainRecord> findByTypeOrderByCreatedAtDesc(String type);
  List<DomainRecord> findByTypeAndOwnerOrderByCreatedAtDesc(String type,String owner);
}
