package com.workspace.entity;

import com.workspace.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationStatusTest {

    @Test
    void pendingSeConfirmaCuandoElPagoEsAprobado() {
        assertEquals(ReservationStatus.CONFIRMED, ReservationStatus.PENDING.confirm());
    }

    @Test
    void pendingPasaAPendingPaymentCuandoElGatewayFalla() {
        assertEquals(ReservationStatus.PENDING_PAYMENT, ReservationStatus.PENDING.markPendingPayment());
    }

    @Test
    void confirmedSePuedeCancelar() {
        assertEquals(ReservationStatus.CANCELLED, ReservationStatus.CONFIRMED.cancel());
    }

    @Test
    void cancelledNoSePuedeVolverACancelar() {
        assertThrows(BusinessRuleException.class, () -> ReservationStatus.CANCELLED.cancel());
    }
}
