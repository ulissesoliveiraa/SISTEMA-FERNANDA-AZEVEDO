package com.fernandaazevedo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="domain_records")
public class DomainRecord {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(nullable=false) public String type;
  @Column(nullable=false) public String owner;
  @Column(nullable=false) public String status;
  public String reference;
  @Lob @Column(columnDefinition="CLOB") public String payload;
  public LocalDateTime createdAt=LocalDateTime.now(); public LocalDateTime updatedAt=LocalDateTime.now();
}
