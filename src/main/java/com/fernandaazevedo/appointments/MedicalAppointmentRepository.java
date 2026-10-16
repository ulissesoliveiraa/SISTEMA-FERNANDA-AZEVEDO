package com.fernandaazevedo.appointments;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface MedicalAppointmentRepository extends JpaRepository<MedicalAppointment, Long> {
  Optional<MedicalAppointment> findByReference(String reference);
  List<MedicalAppointment> findByPatientUserIdOrderByCreatedAtDesc(Long patientUserId);
  List<MedicalAppointment> findByDoctorUserIdOrderByCreatedAtDesc(Long doctorUserId);
  List<MedicalAppointment> findByStatusOrderByQueueEnteredAtAsc(String status);
  boolean existsByDoctorUserIdAndStatusIn(Long doctorUserId, Collection<String> statuses);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from MedicalAppointment a where a.id=:id") Optional<MedicalAppointment> findLockedById(@Param("id") Long id);
}
