package com.flysoft.fretcorridor.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * EF-OPS-03 / ENF-SEC-02 — journal append-only des actions sensibles.
 */
@Entity
@Table(name = "journal_audit", indexes = {
        @Index(name = "idx_audit_tenant_ts", columnList = "tenantId, horodatage")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    private UUID acteurId;

    private String acteurRole;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(nullable = false, length = 64)
    private String ressourceType;

    private UUID ressourceId;

    @Column(columnDefinition = "TEXT")
    private String avant;

    @Column(columnDefinition = "TEXT")
    private String apres;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Instant horodatage = Instant.now();
}
