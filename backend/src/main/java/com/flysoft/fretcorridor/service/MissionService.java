package com.flysoft.fretcorridor.service;

import com.flysoft.fretcorridor.dto.MissionDto;
import com.flysoft.fretcorridor.entity.Axe;
import com.flysoft.fretcorridor.entity.Chauffeur;
import com.flysoft.fretcorridor.entity.Mission;
import com.flysoft.fretcorridor.repository.AxeRepository;
import com.flysoft.fretcorridor.repository.ChauffeurRepository;
import com.flysoft.fretcorridor.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final AxeRepository axeRepository;

    // EF-MKT-05 : idempotence — même clé = même résultat, jamais de doublon
    public MissionDto.MissionResponse declarerVide(
            MissionDto.DeclareVideRequest request,
            String idempotencyKey,
            UUID chauffeurUtilisateurId,
            String tenantId) {

        var existante = missionRepository.findByIdempotencyKeyAndTenantId(idempotencyKey, tenantId);
        if (existante.isPresent()) {
            return MissionDto.MissionResponse.fromEntity(existante.get());
        }

        Chauffeur chauffeur = chauffeurRepository.findByUtilisateurId(chauffeurUtilisateurId)
                .orElseThrow(() -> new RuntimeException("CHAUFFEUR_INTROUVABLE"));

        Axe axe = axeRepository.findById(request.getAxeId())
                .orElseThrow(() -> new RuntimeException("AXE_INTROUVABLE"));

        if (!axe.getTenantId().equals(tenantId)) {
            throw new RuntimeException("AXE_INTROUVABLE");
        }

        // EF-MKT-02 : le matching ne s'active que sur les axes dont l'état GEO l'autorise
        if (axe.getEtatActivation() == Axe.EtatActivation.INACTIF) {
            throw new RuntimeException("AXE_INACTIF");
        }

        Mission mission = Mission.builder()
                .idempotencyKey(idempotencyKey)
                .chauffeur(chauffeur)
                .axe(axe)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .typeCamion(request.getTypeCamion())
                .capaciteTonnes(request.getCapaciteTonnes())
                .tenantId(tenantId)
                .build();

        Mission sauvegardee = missionRepository.save(mission);
        return MissionDto.MissionResponse.fromEntity(sauvegardee);
    }

    // Stub Sprint 3 : le vrai matching PostGIS arrive en Phase 2 (Sprint 9)
    public List<MissionDto.MissionResponse> getMatchsDisponibles(String tenantId) {
        return List.of();
    }
}
