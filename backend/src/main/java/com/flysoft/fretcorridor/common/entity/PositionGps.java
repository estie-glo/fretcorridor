package com.flysoft.fretcorridor.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Point GPS horodaté capturé pour une mission (EF-TRK-01).
 */
@Entity
@Table(name = "positions_gps", indexes = {
    @Index(name = "idx_positions_mission", columnList = "mission_id"),
    @Index(name = "idx_positions_tenant", columnList = "tenant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionGps {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /** Instant de capture côté appareil. */
    @Column(nullable = false)
    private LocalDateTime recordedAt;

    /** Instant de réception côté serveur. */
    @Builder.Default
    private LocalDateTime receivedAt = LocalDateTime.now();

    private Double vitesseKmh;
    private Double precisionMetres;

    @Column(nullable = false)
    private String tenantId;
}
