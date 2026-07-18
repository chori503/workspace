package com.workspace.mapper;

import com.workspace.dto.reservation.response.ReservationResponse;
import com.workspace.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getSpace().getId(),
                reservation.getStartDatetime(),
                reservation.getEndDatetime(),
                reservation.getStatus(),
                reservation.getPaymentReference(),
                reservation.getTotalPrice(),
                reservation.getCreatedAt()
        );
    }
}
