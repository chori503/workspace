package com.workspace.dto.reservation.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationRequest(
        @NotNull
        Long userId,

        @NotNull
        Long spaceId,

        @NotNull
        @Valid
        CardDetailsRequest cardDetails,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate reservationDate,

        @NotNull
        @Min(0)
        @Max(23)
        Integer startTime,

        @NotNull
        @Min(0)
        @Max(23)
        Integer endTime
) {
}
