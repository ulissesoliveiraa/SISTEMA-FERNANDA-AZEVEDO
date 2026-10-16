package com.fernandaazevedo.appointments;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.*;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailabilitySlot, Long> {
  List<DoctorAvailabilitySlot> findByDoctorUserIdAndStartsAtAfterOrderByStartsAt(Long doctorUserId, LocalDateTime after);
  List<DoctorAvailabilitySlot> findByStatusAndStartsAtAfterOrderByStartsAt(String status, LocalDateTime after);
  boolean existsByDoctorUserIdAndStartsAtLessThanAndEndsAtGreaterThan(Long doctorUserId, LocalDateTime endsAt, LocalDateTime startsAt);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from DoctorAvailabilitySlot s where s.id=:id") Optional<DoctorAvailabilitySlot> findLockedById(@Param("id") Long id);
}
