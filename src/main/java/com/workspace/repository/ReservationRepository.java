package com.workspace.repository;

import com.workspace.entity.Reservation;
import com.workspace.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.space.id = :spaceId
              AND r.status IN :activeStatuses
              AND r.startDatetime < :end
              AND r.endDatetime > :start
            """)
    boolean existsOverlapping(@Param("spaceId") Long spaceId,
                               @Param("activeStatuses") List<ReservationStatus> activeStatuses,
                               @Param("start") OffsetDateTime start,
                               @Param("end") OffsetDateTime end);
}
