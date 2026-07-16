package com.workspace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @NotNull
    @Column(name = "start_datetime", nullable = false)
    private OffsetDateTime startDatetime;

    @NotNull
    @Column(name = "end_datetime", nullable = false)
    private OffsetDateTime endDatetime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 30)
    private ReservationStatus status;

    @Size(max = 255)
    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
