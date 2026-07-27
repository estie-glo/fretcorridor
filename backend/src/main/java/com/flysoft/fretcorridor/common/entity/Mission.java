package com.flysoft.fretcorridor.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "missions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"idempotency_key", "tenant_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // EF-MKT-05 : prévention de double affectation via idempotence
    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @ManyToOne
    @JoinColumn(name = "chauffeur_id", nullable = false)
    private Chauffeur chauffeur;

    @ManyToOne
    @JoinColumn(name = "axe_id", nullable = false)
    private Axe axe;

    // Position GPS au moment de la déclaration
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String typeCamion;      // ex: "Semi-remorque", "Porteur"

    @Column(nullable = false)
    private Double capaciteTonnes;

    // EF-MKT-01 : disponibilité déclarée par le chauffeur
    @Column
    private LocalDateTime disponibleDe;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutMission statut = StatutMission.CAMION_VIDE_DECLARE;

    @Column(nullable = false)
    private String tenantId;

    @Builder.Default
    private LocalDateTime dateDeclaration = LocalDateTime.now();

    public enum StatutMission {
        CAMION_VIDE_DECLARE,
        MATCH_PROPOSE,
        MISSION_ACCEPTEE,
        EN_COURS,
        TERMINEE,
        ANNULEE
    }
}
