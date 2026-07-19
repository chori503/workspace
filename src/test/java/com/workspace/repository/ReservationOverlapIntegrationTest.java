package com.workspace.repository;

import com.workspace.entity.AppUser;
import com.workspace.entity.Reservation;
import com.workspace.entity.ReservationStatus;
import com.workspace.entity.Role;
import com.workspace.entity.Space;
import com.workspace.entity.SpaceStatus;
import com.workspace.entity.SpaceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
class ReservationOverlapIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SpaceRepository spaceRepository;

    @Test
    void elExcludeConstraintRechazaReservasSolapadasParaElMismoEspacio() {
        AppUser user = new AppUser();
        user.setEmail("integracion@test.com");
        user.setPassword("hash-no-relevante");
        user.setFullName("Usuario de prueba");
        user.setRole(Role.USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);

        Space space = new Space();
        space.setName("Sala de prueba");
        space.setType(SpaceType.MEETING_ROOM);
        space.setCapacity(4);
        space.setHourlyRate(new BigDecimal("10.00"));
        space.setStatus(SpaceStatus.ACTIVE);
        spaceRepository.save(space);

        OffsetDateTime start = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        reservationRepository.saveAndFlush(reservation(user, space, start, start.plusHours(2)));

        Reservation overlapping = reservation(user, space, start.plusMinutes(30), start.plusHours(2).plusMinutes(30));
        assertThrows(DataIntegrityViolationException.class,
                () -> reservationRepository.saveAndFlush(overlapping));
    }

    private Reservation reservation(AppUser user, Space space, OffsetDateTime start, OffsetDateTime end) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setSpace(space);
        reservation.setStartDatetime(start);
        reservation.setEndDatetime(end);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(OffsetDateTime.now());
        return reservation;
    }
}
