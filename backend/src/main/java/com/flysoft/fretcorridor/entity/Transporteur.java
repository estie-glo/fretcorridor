package com.flysoft.fretcorridor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transporteurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transporteur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private String nomEntreprise;

    @Column(nullable = false)
    private String nomResponsable;

    @Column(nullable = false)
    private String prenomResponsable;

    private String numeroRegistreCommerce; // optionnel, KYC niveau 2

    // KYC gradué (EF-IDA-02, appliqué à tous les acteurs)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private KycNiveau kycNiveau = KycNiveau.NIVEAU_0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutKyc statutKyc = StatutKyc.EN_ATTENTE;

    // Agent qui a enrôlé ce transporteur (EF-IDA-03)
    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Agent agent;

    @Column(nullable = false)
    private String tenantId;

    @Builder.Default
    private LocalDateTime dateEnrolement = LocalDateTime.now();

    private LocalDateTime dateValidationKyc;

    public enum KycNiveau { NIVEAU_0, NIVEAU_1, NIVEAU_2 }
    public enum StatutKyc { EN_ATTENTE, EN_COURS, VALIDE, REJETE }
}
