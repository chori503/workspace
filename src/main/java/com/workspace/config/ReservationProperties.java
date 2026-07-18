package com.workspace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.reservation")
public record ReservationProperties(
        int maxAdvanceDays,
        BusinessHours businessHours
) {
    public record BusinessHours(Schedule mondayToSaturday, Schedule sunday) {
    }

    public record Schedule(int openHour, int closeHour) {
    }
}
