package com.workspace.service;

import com.workspace.config.ReservationProperties;
import com.workspace.dto.report.response.SpaceOccupancyResponse;
import com.workspace.entity.ReservationStatus;
import com.workspace.exception.BusinessRuleException;
import com.workspace.repository.ReservationRepository;
import com.workspace.repository.SpaceOccupiedHoursProjection;
import com.workspace.repository.SpaceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final List<ReservationStatus> OCCUPYING_STATUSES = List.of(
            ReservationStatus.PENDING, ReservationStatus.PENDING_PAYMENT,
            ReservationStatus.CONFIRMED, ReservationStatus.COMPLETED);

    private final SpaceRepository spaceRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationProperties reservationProperties;

    public ReportService(SpaceRepository spaceRepository, ReservationRepository reservationRepository,
                          ReservationProperties reservationProperties) {
        this.spaceRepository = spaceRepository;
        this.reservationRepository = reservationRepository;
        this.reservationProperties = reservationProperties;
    }

    @Cacheable(value = "occupancyReport", key = "#startDate + '_' + #endDate")
    public List<SpaceOccupancyResponse> getOccupancyReport(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleException("La fecha final debe ser mayor o igual a la fecha inicial");
        }

        OffsetDateTime rangeStart = startDate.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime rangeEnd = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();

        List<String> statusNames = OCCUPYING_STATUSES.stream().map(Enum::name).toList();
        Map<Long, Double> reservedHoursBySpace = reservationRepository
                .sumReservedHoursBySpace(statusNames, rangeStart, rangeEnd).stream()
                .collect(Collectors.toMap(SpaceOccupiedHoursProjection::getSpaceId,
                        SpaceOccupiedHoursProjection::getReservedHours));

        double totalAvailableHours = calculateAvailableHours(startDate, endDate);

        return spaceRepository.findAll().stream()
                .map(space -> {
                    double reservedHours = reservedHoursBySpace.getOrDefault(space.getId(), 0.0);
                    BigDecimal occupancyPercentage = BigDecimal.valueOf(reservedHours / totalAvailableHours * 100)
                            .setScale(2, RoundingMode.HALF_UP);
                    return new SpaceOccupancyResponse(space.getId(), space.getName(), occupancyPercentage);
                })
                .toList();
    }

    private double calculateAvailableHours(LocalDate startDate, LocalDate endDate) {
        double totalHours = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            ReservationProperties.Schedule schedule = date.getDayOfWeek() == DayOfWeek.SUNDAY
                    ? reservationProperties.businessHours().sunday()
                    : reservationProperties.businessHours().mondayToSaturday();
            totalHours += schedule.closeHour() - schedule.openHour();
        }
        return totalHours;
    }
}
