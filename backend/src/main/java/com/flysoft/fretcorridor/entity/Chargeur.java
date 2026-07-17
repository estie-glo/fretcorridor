package com.flysoft.fretcorridor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chargeurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chargeur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    private String entreprise; // optionnel (peut être un chargeur indépendant)

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private KycNiveau kycNiveau = KycNiveau.NIVEAU_0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutKyc statutKyc = StatutKyc.EN_ATTENTE;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Agent agent;

    @Column(nullable = false)
    private String tenantId;

    @Builder.Default
    private LocalDateTime dateEnrolement = LocalDateTime.now();

    public enum KycNiveau { NIVEAU_0, NIVEAU_1, NIVEAU_2 }
    public enum StatutKyc { EN_ATTENTE, EN_COURS, VALIDE, REJETE }
}
