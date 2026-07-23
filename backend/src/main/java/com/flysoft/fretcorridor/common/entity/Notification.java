package com.flysoft.fretcorridor.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * EF-NOT — notification transactionnelle multicanal (IN_APP pour le web ; FCM/SMS/WA stub).
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_dest_ts", columnList = "destinataireId, dateCreation")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID destinataireId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Canal canal;

    @Column(nullable = false, length = 64)
    private String type;

    @Column(nullable = false, length = 200)
    private String titreFr;

    @Column(nullable = false, length = 200)
    private String titreEn;

    @Column(nullable = false, length = 1000)
    private String corpsFr;

    @Column(nullable = false, length = 1000)
    private String corpsEn;

    private String ressourceType;
    private UUID ressourceId;

    @Builder.Default
    private boolean lue = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 32)
    private StatutEnvoi statutEnvoi = StatutEnvoi.DELIVRE;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Instant dateCreation = Instant.now();

    public enum Canal {
        IN_APP,
        FCM,
        SMS,
        WHATSAPP,
        USSD
    }

    public enum StatutEnvoi {
        DELIVRE,
        STUB_QUEUED,
        ECHEC
    }
}
