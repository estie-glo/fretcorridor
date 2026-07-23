package com.flysoft.fretcorridor.common.service;

import com.flysoft.fretcorridor.common.entity.JournalAudit;
import com.flysoft.fretcorridor.common.repository.JournalAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JournalAuditService {

    private final JournalAuditRepository journalAuditRepository;

    @Transactional
    public void enregistrer(
            String tenantId,
            UUID acteurId,
            String acteurRole,
            String action,
            String ressourceType,
            UUID ressourceId,
            String avant,
            String apres) {
        journalAuditRepository.save(JournalAudit.builder()
                .tenantId(tenantId)
                .acteurId(acteurId)
                .acteurRole(acteurRole)
                .action(action)
                .ressourceType(ressourceType)
                .ressourceId(ressourceId)
                .avant(avant)
                .apres(apres)
                .build());
    }

    @Transactional(readOnly = true)
    public List<JournalAudit> lister(String tenantId) {
        return journalAuditRepository.findByTenantIdOrderByHorodatageDesc(tenantId);
    }
}
