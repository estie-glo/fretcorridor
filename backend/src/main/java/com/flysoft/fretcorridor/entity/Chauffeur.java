package com.flysoft.fretcorridor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chauffeurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chauffeur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Lien vers l'utilisateur (authentification)
    @OneToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    // Informations personnelles
    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    private String numeroCNI;
    private String urlPhotoCNI;      // stocké dans MinIO
    private String urlPhotoPermis;   // stocké dans MinIO

    // KYC
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private KycNiveau kycNiveau = KycNiveau.NIVEAU_0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutKyc statutKyc = StatutKyc.EN_ATTENTE;

    // Agent qui a enrôlé ce chauffeur
    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Agent agent;

    // Tenant
    @Column(nullable = false)
    private String tenantId;

    @Builder.Default
    private LocalDateTime dateEnrolement = LocalDateTime.now();

    private LocalDateTime dateValidationKyc;

    // ── Énumérations ──────────────────────────────────────────

    public enum KycNiveau {
        NIVEAU_0,   // Pas de KYC
        NIVEAU_1,   // Identité + contact vérifiés
        NIVEAU_2    // Justificatifs complets (avant fonctions financières)
    }

    public enum StatutKyc {
        EN_ATTENTE,
        EN_COURS,
        VALIDE,
        REJETE
    }
}
