package com.workspace.dto.space.response;

import com.workspace.entity.SpaceStatus;
import com.workspace.entity.SpaceType;

import java.math.BigDecimal;

public record SpaceResponse(
        Long id,
        String name,
        SpaceType type,
        Integer capacity,
        String location,
        String floor,
        BigDecimal hourlyRate,
        SpaceStatus status
) {
}
