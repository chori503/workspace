package com.workspace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "space")
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private SpaceType type;

    @NotNull
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Size(max = 255)
    @Column(name = "location")
    private String location;

    @Size(max = 50)
    @Column(name = "floor", length = 50)
    private String floor;

    @NotNull
    @Column(name = "hourly_rate", nullable = false, precision = 8, scale = 2)
    private BigDecimal hourlyRate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private SpaceStatus status;
}
