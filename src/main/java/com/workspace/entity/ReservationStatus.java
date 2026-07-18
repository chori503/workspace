package com.workspace.entity;

import com.workspace.exception.BusinessRuleException;

public enum ReservationStatus {
    PENDING {
        @Override
        public ReservationStatus confirm() {
            return CONFIRMED;
        }

        @Override
        public ReservationStatus markPendingPayment() {
            return PENDING_PAYMENT;
        }

        @Override
        public ReservationStatus markPaymentDeclined() {
            return PAYMENT_DECLINED;
        }

        @Override
        public ReservationStatus cancel() {
            return CANCELLED;
        }
    },
    PENDING_PAYMENT {
        @Override
        public ReservationStatus confirm() {
            return CONFIRMED;
        }

        @Override
        public ReservationStatus cancel() {
            return CANCELLED;
        }
    },
    CONFIRMED {
        @Override
        public ReservationStatus cancel() {
            return CANCELLED;
        }

        @Override
        public ReservationStatus complete() {
            return COMPLETED;
        }
    },
    PAYMENT_DECLINED,
    CANCELLED,
    COMPLETED;

    public ReservationStatus confirm() {
        throw new BusinessRuleException("No se puede confirmar una reserva en estado " + this);
    }

    public ReservationStatus cancel() {
        throw new BusinessRuleException("No se puede cancelar una reserva en estado " + this);
    }

    public ReservationStatus complete() {
        throw new BusinessRuleException("No se puede completar una reserva en estado " + this);
    }

    public ReservationStatus markPendingPayment() {
        throw new BusinessRuleException("No se puede dejar en pago pendiente una reserva en estado " + this);
    }

    public ReservationStatus markPaymentDeclined() {
        throw new BusinessRuleException("No se puede marcar como pago rechazado una reserva en estado " + this);
    }
}
