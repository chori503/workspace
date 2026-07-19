package com.workspace.service;

import com.workspace.config.EmailProperties;
import com.workspace.config.ReservationProperties;
import com.workspace.dto.reservation.request.CardDetailsRequest;
import com.workspace.dto.reservation.request.ReservationRequest;
import com.workspace.entity.AppUser;
import com.workspace.entity.Reservation;
import com.workspace.entity.ReservationStatus;
import com.workspace.entity.Role;
import com.workspace.entity.Space;
import com.workspace.entity.SpaceStatus;
import com.workspace.exception.OverlappingReservationException;
import com.workspace.mapper.ReservationMapper;
import com.workspace.repository.ReservationRepository;
import com.workspace.repository.SpaceRepository;
import com.workspace.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private SpaceRepository spaceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private PaymentService paymentService;
    @Mock
    private EmailService emailService;
    @Mock
    private EmailProperties emailProperties;

    private ReservationService reservationService;

    private final AppUser normalUser = user(2L, "user@test.com", Role.USER);
    private final AppUser otherUser = user(3L, "other@test.com", Role.USER);
    private final Space space = space(10L, new BigDecimal("10.00"));

    private final ReservationProperties reservationProperties = new ReservationProperties(
            15,
            new ReservationProperties.BusinessHours(
                    new ReservationProperties.Schedule(8, 20),
                    new ReservationProperties.Schedule(9, 19)));

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, spaceRepository, userRepository,
                reservationMapper, paymentService, reservationProperties, emailService, emailProperties);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usuarioNoPuedeCrearReservaParaOtroUsuario() {
        authenticateAs(normalUser);
        ReservationRequest request = reservationRequest(otherUser.getId());

        assertThrows(AccessDeniedException.class, () -> reservationService.create(request));
    }

    @Test
    void tarjetaAprobadaConfirmaLaReserva() {
        authenticateAs(normalUser);
        when(spaceRepository.findById(space.getId())).thenReturn(Optional.of(space));
        when(reservationRepository.existsOverlapping(any(), any(), any(), any())).thenReturn(false);
        when(paymentService.charge(any())).thenReturn(new PaymentOutcome(PaymentResult.APPROVED, "ref-1", null));
        when(emailProperties.reservationConfirmed()).thenReturn(new EmailProperties.Message("asunto", "cuerpo"));

        reservationService.create(reservationRequest(normalUser.getId()));

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).saveAndFlush(captor.capture());
        assertEquals(ReservationStatus.CONFIRMED, captor.getValue().getStatus());
    }

    @Test
    void espacioSolapadoLanzaExcepcion() {
        authenticateAs(normalUser);
        when(spaceRepository.findById(space.getId())).thenReturn(Optional.of(space));
        when(reservationRepository.existsOverlapping(any(), any(), any(), any())).thenReturn(true);

        assertThrows(OverlappingReservationException.class,
                () -> reservationService.create(reservationRequest(normalUser.getId())));
    }

    @Test
    void dueñoPuedeCancelarSuReserva() {
        authenticateAs(normalUser);
        Reservation reservation = new Reservation();
        reservation.setId(100L);
        reservation.setUser(normalUser);
        reservation.setStatus(ReservationStatus.PENDING);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

        reservationService.cancel(100L);

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    private void authenticateAs(AppUser callerUser) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(callerUser.getEmail());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmailIgnoreCase(callerUser.getEmail())).thenReturn(Optional.of(callerUser));
    }

    private ReservationRequest reservationRequest(Long userId) {
        CardDetailsRequest cardDetails = new CardDetailsRequest("4111111111111111", "03/29", "029");
        return new ReservationRequest(userId, space.getId(), cardDetails, LocalDate.now().plusDays(2), 10, 12);
    }

    private static AppUser user(Long id, String email, Role role) {
        AppUser appUser = new AppUser();
        appUser.setId(id);
        appUser.setEmail(email);
        appUser.setFullName("Test User");
        appUser.setRole(role);
        return appUser;
    }

    private static Space space(Long id, BigDecimal hourlyRate) {
        Space space = new Space();
        space.setId(id);
        space.setName("Espacio de prueba");
        space.setHourlyRate(hourlyRate);
        space.setStatus(SpaceStatus.ACTIVE);
        space.setCapacity(1);
        return space;
    }
}
