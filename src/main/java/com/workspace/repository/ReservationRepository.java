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

    @Query(value = """
            SELECT r.space_id AS spaceId,
                   SUM(EXTRACT(EPOCH FROM (LEAST(r.end_datetime, :end) - GREATEST(r.start_datetime, :start))) / 3600.0) AS reservedHours
            FROM reservation r
            WHERE r.status IN (:statuses)
              AND r.start_datetime < :end
              AND r.end_datetime > :start
            GROUP BY r.space_id
            """, nativeQuery = true)
    List<SpaceOccupiedHoursProjection> sumReservedHoursBySpace(@Param("statuses") List<String> statuses,
                                                                @Param("start") OffsetDateTime start,
                                                                @Param("end") OffsetDateTime end);
}
