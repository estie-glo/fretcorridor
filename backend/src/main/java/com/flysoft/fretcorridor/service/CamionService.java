package com.flysoft.fretcorridor.service;

import com.flysoft.fretcorridor.dto.CamionDto;
import com.flysoft.fretcorridor.entity.Camion;
import com.flysoft.fretcorridor.entity.Transporteur;
import com.flysoft.fretcorridor.repository.CamionRepository;
import com.flysoft.fretcorridor.repository.TransporteurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CamionService {

    private final CamionRepository camionRepository;
    private final TransporteurRepository transporteurRepository;

    // Le transporteur ajoute un camion à sa propre flotte (utilisateurId = son propre compte)
    public CamionDto.CamionResponse ajouter(
            CamionDto.AjoutRequest request, UUID transporteurUtilisateurId, String tenantId) {

        Transporteur transporteur = transporteurRepository.findByUtilisateurId(transporteurUtilisateurId)
                .orElseThrow(() -> new RuntimeException("TRANSPORTEUR_INTROUVABLE"));

        if (!transporteur.getTenantId().equals(tenantId)) {
            throw new RuntimeException("TRANSPORTEUR_INTROUVABLE");
        }

        Camion camion = Camion.builder()
                .transporteur(transporteur)
                .immatriculation(request.getImmatriculation())
                .type(Camion.TypeCamion.valueOf(request.getType()))
                .capaciteTonnes(request.getCapaciteTonnes())
                .tenantId(tenantId)
                .build();

        return CamionDto.CamionResponse.fromEntity(camionRepository.save(camion));
    }

    public List<CamionDto.CamionResponse> getMesCamions(UUID transporteurUtilisateurId, String tenantId) {
        Transporteur transporteur = transporteurRepository.findByUtilisateurId(transporteurUtilisateurId)
                .orElseThrow(() -> new RuntimeException("TRANSPORTEUR_INTROUVABLE"));

        return camionRepository.findByTransporteurIdAndTenantId(transporteur.getId(), tenantId).stream()
                .map(CamionDto.CamionResponse::fromEntity)
                .toList();
    }
}
