package com.workspace.dto.reservation.response;

import com.workspace.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReservationResponse(
        Long id,
        Long userId,
        Long spaceId,
        OffsetDateTime startDatetime,
        OffsetDateTime endDatetime,
        ReservationStatus status,
        String paymentReference,
        BigDecimal totalPrice,
        OffsetDateTime createdAt
) {
}
