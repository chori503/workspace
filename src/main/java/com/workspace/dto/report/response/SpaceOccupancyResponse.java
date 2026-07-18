package com.workspace.dto.report.response;

import java.math.BigDecimal;

public record SpaceOccupancyResponse(
        Long spaceId,
        String spaceName,
        BigDecimal occupancyPercentage
) {
}
