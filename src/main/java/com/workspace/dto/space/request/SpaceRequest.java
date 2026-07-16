package com.workspace.dto.space.request;

import com.workspace.entity.SpaceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SpaceRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        SpaceType type,

        @NotNull
        @Min(1)
        @Max(50)
        Integer capacity,

        @Size(max = 255)
        String location,

        @Size(max = 50)
        String floor,

        @NotNull
        @DecimalMin(value = "0", inclusive = true)
        BigDecimal hourlyRate
) {
}
