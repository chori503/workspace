package com.workspace.service;

import com.workspace.config.EmailProperties;
import com.workspace.config.ReservationProperties;
import com.workspace.dto.reservation.request.ReservationRequest;
import com.workspace.dto.reservation.response.ReservationResponse;
import com.workspace.entity.AppUser;
import com.workspace.entity.Reservation;
import com.workspace.entity.ReservationStatus;
import com.workspace.entity.Role;
import com.workspace.entity.Space;
import com.workspace.entity.SpaceStatus;
import com.workspace.exception.BusinessRuleException;
import com.workspace.exception.OverlappingReservationException;
import com.workspace.exception.ResourceNotFoundException;
import com.workspace.mapper.ReservationMapper;
import com.workspace.repository.ReservationRepository;
import com.workspace.repository.SpaceRepository;
import com.workspace.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final List<ReservationStatus> ACTIVE_STATUSES =
            List.of(ReservationStatus.PENDING, ReservationStatus.PENDING_PAYMENT, ReservationStatus.CONFIRMED);

    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;
    private final PaymentService paymentService;
    private final ReservationProperties reservationProperties;
    private final EmailService emailService;
    private final EmailProperties emailProperties;

    public ReservationService(ReservationRepository reservationRepository, SpaceRepository spaceRepository,
                               UserRepository userRepository, ReservationMapper reservationMapper,
                               PaymentService paymentService, ReservationProperties reservationProperties,
                               EmailService emailService, EmailProperties emailProperties) {
        this.reservationRepository = reservationRepository;
        this.spaceRepository = spaceRepository;
        this.userRepository = userRepository;
        this.reservationMapper = reservationMapper;
        this.paymentService = paymentService;
        this.reservationProperties = reservationProperties;
        this.emailService = emailService;
        this.emailProperties = emailProperties;
    }

    public List<ReservationResponse> findAll() {
        AppUser caller = currentUser();
        List<Reservation> reservations = caller.getRole() == Role.ADMIN
                ? reservationRepository.findAll()
                : reservationRepository.findByUserId(caller.getId());
        return reservations.stream().map(reservationMapper::toResponse).toList();
    }

    public ReservationResponse findById(Long id) {
        Reservation reservation = getOrThrow(id);
        requireOwnerOrAdmin(reservation);
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    @CacheEvict(value = "occupancyReport", allEntries = true)
    public ReservationResponse create(ReservationRequest request) {
        AppUser targetUser = resolveTargetUser(request.userId());
        Space space = spaceRepository.findById(request.spaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Espacio con id " + request.spaceId() + " no encontrado"));
        if (space.getStatus() != SpaceStatus.ACTIVE) {
            throw new BusinessRuleException("El espacio con id " + request.spaceId() + " no está activo");
        }

        OffsetDateTime start = validateSchedule(request);
        OffsetDateTime end = request.reservationDate().atTime(request.endTime(), 0)
                .atZone(ZoneId.systemDefault()).toOffsetDateTime();

        if (reservationRepository.existsOverlapping(space.getId(), ACTIVE_STATUSES, start, end)) {
            throw new OverlappingReservationException("El espacio ya tiene una reserva en ese horario");
        }

        BigDecimal totalPrice = space.getHourlyRate().multiply(BigDecimal.valueOf(request.endTime() - request.startTime()));

        Reservation reservation = new Reservation();
        reservation.setUser(targetUser);
        reservation.setSpace(space);
        reservation.setStartDatetime(start);
        reservation.setEndDatetime(end);
        reservation.setTotalPrice(totalPrice);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(OffsetDateTime.now());

        PaymentOutcome outcome = paymentService.charge(request.cardDetails());
        reservation.setPaymentReference(outcome.reference());

        switch (outcome.result()) {
            case APPROVED -> reservation.setStatus(reservation.getStatus().confirm());
            case DECLINED -> reservation.setStatus(reservation.getStatus().markPaymentDeclined());
            case GATEWAY_UNAVAILABLE -> reservation.setStatus(reservation.getStatus().markPendingPayment());
        }

        try {
            reservationRepository.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException ex) {
            throw new OverlappingReservationException("El espacio ya tiene una reserva en ese horario");
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            EmailProperties.Message confirmed = emailProperties.reservationConfirmed();
            emailService.sendEmail(targetUser.getEmail(), confirmed.subject(), confirmed.body());
        }

        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    @CacheEvict(value = "occupancyReport", allEntries = true)
    public ReservationResponse cancel(Long id) {
        Reservation reservation = getOrThrow(id);
        requireOwnerOrAdmin(reservation);
        reservation.setStatus(reservation.getStatus().cancel());
        reservationRepository.saveAndFlush(reservation);
        return reservationMapper.toResponse(reservation);
    }

    private OffsetDateTime validateSchedule(ReservationRequest request) {
        if (request.endTime() <= request.startTime()) {
            throw new BusinessRuleException("La hora de fin debe ser mayor a la hora de inicio");
        }

        LocalDate reservationDate = request.reservationDate();
        ReservationProperties.Schedule schedule = reservationDate.getDayOfWeek() == DayOfWeek.SUNDAY
                ? reservationProperties.businessHours().sunday()
                : reservationProperties.businessHours().mondayToSaturday();

        if (request.startTime() < schedule.openHour() || request.endTime() > schedule.closeHour()) {
            throw new BusinessRuleException("El horario debe estar entre las " + schedule.openHour()
                    + " y las " + schedule.closeHour() + " horas");
        }

        OffsetDateTime start = reservationDate.atTime(request.startTime(), 0)
                .atZone(ZoneId.systemDefault()).toOffsetDateTime();

        if (!start.isAfter(OffsetDateTime.now())) {
            throw new BusinessRuleException("La reserva debe ser en una fecha y hora futura");
        }
        if (reservationDate.isAfter(LocalDate.now().plusDays(reservationProperties.maxAdvanceDays()))) {
            throw new BusinessRuleException("No se pueden hacer reservas con más de "
                    + reservationProperties.maxAdvanceDays() + " días de anticipación");
        }

        return start;
    }

    private AppUser resolveTargetUser(Long requestedUserId) {
        AppUser caller = currentUser();

        if (caller.getRole() == Role.USER) {
            if (!caller.getId().equals(requestedUserId)) {
                throw new AccessDeniedException("Un usuario solo puede crear reservas para sí mismo");
            }
            return caller;
        }

        AppUser targetUser = userRepository.findById(requestedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con id " + requestedUserId + " no encontrado"));
        if (targetUser.getRole() != Role.USER) {
            throw new AccessDeniedException("Un administrador solo puede crear reservas para usuarios con rol USER");
        }
        return targetUser;
    }

    private void requireOwnerOrAdmin(Reservation reservation) {
        AppUser caller = currentUser();
        if (caller.getRole() != Role.ADMIN && !reservation.getUser().getId().equals(caller.getId())) {
            throw new AccessDeniedException("No tenés acceso a esta reserva");
        }
    }

    private AppUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }

    private Reservation getOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva con id " + id + " no encontrada"));
    }
}
