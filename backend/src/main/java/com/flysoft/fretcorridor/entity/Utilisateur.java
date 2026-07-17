package com.flysoft.fretcorridor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String telephone;

    @Column(nullable = false)
    private String codePin; // stocké hashé avec BCrypt

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String tenantId; // BGFT_CM, BNFT_TD, BARC_RCA...

    private String fcmToken; // Firebase Cloud Messaging token

    @Builder.Default
    private Boolean actif = true;

    @Builder.Default
    private Integer tentativesEchouees = 0;

    public enum Role {
        CHAUFFEUR, AGENT, CLIENT, OPERATEUR, TRANSPORTEUR, CHARGEUR
    }
}
