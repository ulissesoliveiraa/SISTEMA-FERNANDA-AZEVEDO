package com.fernandaazevedo.common;

import com.fernandaazevedo.domain.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
  "spring.datasource.url=jdbc:h2:mem:production-bootstrap;DB_CLOSE_DELAY=-1",
  "spring.jpa.hibernate.ddl-auto=create-drop",
  "app.seed-demo=false",
  "app.bootstrap-admin.email=admin@empresa.test",
  "app.bootstrap-admin.password=SenhaInicial@12345"
})
class ProductionBootstrapIntegrationTest {
  @Autowired AppUserRepository users;
  @Autowired DemoService service;

  @Test void createsOnlyTheConfiguredProductionAdministrator() {
    assertEquals(1, users.count());
    var admin = users.findByEmailIgnoreCase("admin@empresa.test").orElseThrow();
    assertEquals("ADMIN", admin.role);
    assertTrue(admin.temporaryPassword);
    assertTrue(service.encoder.matches("SenhaInicial@12345", admin.passwordHash));
    assertTrue(users.findByEmailIgnoreCase("paciente@fernandaazevedo.demo").isEmpty());
  }
}
