package com.flysoft.fretcorridor.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "camions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "transporteur_id", nullable = false)
    private Transporteur transporteur;

    @Column(nullable = false, unique = true)
    private String immatriculation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCamion type;

    @Column(nullable = false)
    private Double capaciteTonnes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutOperationnel statut = StatutOperationnel.ACTIF;

    @Column(nullable = false)
    private String tenantId;

    public enum TypeCamion { SEMI_REMORQUE, PORTEUR, CITERNE, PLATEAU }
    public enum StatutOperationnel { ACTIF, EN_MAINTENANCE, HORS_SERVICE }
}
