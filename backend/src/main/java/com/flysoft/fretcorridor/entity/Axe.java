package com.flysoft.fretcorridor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "axes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Axe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nom;   // ex: "Douala-NDjamena"

    @ManyToOne
    @JoinColumn(name = "hub_depart_id", nullable = false)
    private Hub hubDepart;

    @ManyToOne
    @JoinColumn(name = "hub_arrivee_id", nullable = false)
    private Hub hubArrivee;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EtatActivation etatActivation = EtatActivation.INACTIF;

    // EF-GEO-04 : marquage zone sensible (restriction opérations, traçabilité renforcée)
    @Builder.Default
    private boolean zoneSensible = false;

    @Column(nullable = false)
    private String tenantId;

    // ── Énumérations ──────────────────────────────────────────
    public enum EtatActivation {
        ACTIF,       // visibilité + matching + financement possibles
        VERROUILLE,  // visible mais matching bloqué
        INACTIF      // non visible
    }
}
