package com.workspace.dto.reservation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CardDetailsRequest(
        @NotBlank
        @Pattern(regexp = "\\d{16}", message = "El número de tarjeta debe tener 16 dígitos")
        String cardNumber,

        @NotBlank
        @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "La expiración debe tener formato MM/YY")
        String expiration,

        @NotBlank
        @Pattern(regexp = "\\d{3}", message = "El CVC debe tener 3 dígitos")
        String cvc
) {
}
