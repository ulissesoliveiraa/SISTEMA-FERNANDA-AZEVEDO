package com.fernandaazevedo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="app_users")
public class AppUser {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(nullable=false) public String name;
  @Column(nullable=false,unique=true) public String email;
  @Column(nullable=false) public String passwordHash;
  @Column(nullable=false) public String role;
  @Column(nullable=false) public String status="ACTIVE";
  @Column(length=4000) public String permissions="";
  public String phone; public String document; public boolean temporaryPassword;
  public LocalDateTime createdAt=LocalDateTime.now(); public LocalDateTime lastAccess;
}
