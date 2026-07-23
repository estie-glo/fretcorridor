package com.flysoft.fretcorridor.common.service;

import com.flysoft.fretcorridor.common.dto.AxeDto;
import com.flysoft.fretcorridor.common.entity.Axe;
import com.flysoft.fretcorridor.common.repository.AxeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AxeService {

    private final AxeRepository axeRepository;
    private final JournalAuditService journalAuditService;

    public List<AxeDto.AxeResponse> getAxesDisponibles(String tenantId) {
        return axeRepository.findByTenantId(tenantId).stream()
                .map(AxeDto.AxeResponse::fromEntity)
                .toList();
    }

    public AxeDto.AxeResponse getStatut(UUID axeId, String tenantId) {
        Axe axe = axeRepository.findById(axeId)
                .orElseThrow(() -> new RuntimeException("AXE_INTROUVABLE"));
        if (!axe.getTenantId().equals(tenantId)) {
            throw new RuntimeException("AXE_INTROUVABLE");
        }
        return AxeDto.AxeResponse.fromEntity(axe);
    }

    @Transactional
    public AxeDto.AxeResponse updateActivation(
            UUID axeId,
            AxeDto.UpdateActivationRequest request,
            String tenantId,
            UUID acteurId,
            String acteurRole) {
        Axe axe = axeRepository.findById(axeId)
                .orElseThrow(() -> new RuntimeException("AXE_INTROUVABLE"));
        if (!axe.getTenantId().equals(tenantId)) {
            throw new RuntimeException("AXE_INTROUVABLE");
        }

        String avant = snapshotFlags(axe);

        if (request.getEtatActivation() != null && !request.getEtatActivation().isBlank()) {
            applyLegacyEtat(axe, request.getEtatActivation().trim().toUpperCase());
        } else {
            if (request.getVisibiliteActive() != null) {
                axe.setVisibiliteActive(request.getVisibiliteActive());
            }
            if (request.getMatchingActif() != null) {
                axe.setMatchingActif(request.getMatchingActif());
            }
            if (request.getFinancementActif() != null) {
                axe.setFinancementActif(request.getFinancementActif());
            }
        }

        // Cohérence CDC : matching / financement impliquent la visibilité
        if ((axe.isMatchingActif() || axe.isFinancementActif()) && !axe.isVisibiliteActive()) {
            axe.setVisibiliteActive(true);
        }
        // Financement Phase 3 : ne s'active pas sans matching
        if (axe.isFinancementActif() && !axe.isMatchingActif()) {
            axe.setMatchingActif(true);
        }

        Axe saved = axeRepository.save(axe);

        journalAuditService.enregistrer(
                tenantId, acteurId, acteurRole,
                "AXE_ACTIVATION", "AXE", axeId,
                avant, snapshotFlags(saved));

        return AxeDto.AxeResponse.fromEntity(saved);
    }

    private void applyLegacyEtat(Axe axe, String etat) {
        switch (etat) {
            case "ACTIF" -> {
                axe.setVisibiliteActive(true);
                axe.setMatchingActif(true);
                // financement reste inchangé (Phase 3, allumage explicite)
            }
            case "VERROUILLE" -> {
                axe.setVisibiliteActive(true);
                axe.setMatchingActif(false);
                axe.setFinancementActif(false);
            }
            case "INACTIF" -> {
                axe.setVisibiliteActive(false);
                axe.setMatchingActif(false);
                axe.setFinancementActif(false);
            }
            default -> throw new RuntimeException("ETAT_INVALIDE");
        }
    }

    private static String snapshotFlags(Axe axe) {
        return "visibilite=" + axe.isVisibiliteActive()
                + ";matching=" + axe.isMatchingActif()
                + ";financement=" + axe.isFinancementActif();
    }
}
