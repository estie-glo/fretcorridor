package com.flysoft.fretcorridor.common.entity;

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

    /** EF-GEO-03 — allumage progressif : 3 dimensions indépendantes. */
    @Builder.Default
    private boolean visibiliteActive = false;

    @Builder.Default
    private boolean matchingActif = false;

    @Builder.Default
    private boolean financementActif = false;

    // EF-GEO-04 : marquage zone sensible (restriction opérations, traçabilité renforcée)
    @Builder.Default
    private boolean zoneSensible = false;

    @Column(nullable = false)
    private String tenantId;

    /** Dérivé pour compat API / carte (ACTIF | VERROUILLE | INACTIF). */
    public String deriveEtatActivation() {
        if (!visibiliteActive) {
            return "INACTIF";
        }
        if (!matchingActif) {
            return "VERROUILLE";
        }
        return "ACTIF";
    }

    public boolean peutMatcher() {
        return visibiliteActive && matchingActif;
    }
}
