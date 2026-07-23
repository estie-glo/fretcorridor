package com.flysoft.fretcorridor.common.service;

import com.flysoft.fretcorridor.common.dto.TransporteurDto;
import com.flysoft.fretcorridor.common.entity.Agent;
import com.flysoft.fretcorridor.common.entity.Transporteur;
import com.flysoft.fretcorridor.common.entity.Utilisateur;
import com.flysoft.fretcorridor.common.repository.AgentRepository;
import com.flysoft.fretcorridor.common.repository.TransporteurRepository;
import com.flysoft.fretcorridor.common.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransporteurService {

    private final TransporteurRepository transporteurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TransporteurDto.TransporteurResponse enroler(
            TransporteurDto.EnrolementRequest request, UUID agentUtilisateurId, String tenantId) {

        if (utilisateurRepository.findByTelephone(request.getTelephone()).isPresent()) {
            throw new RuntimeException("TELEPHONE_DEJA_UTILISE");
        }

        Agent agent = agentRepository.findByUtilisateurId(agentUtilisateurId)
                .orElseThrow(() -> new RuntimeException("AGENT_INTROUVABLE"));

        Utilisateur utilisateur = Utilisateur.builder()
                .telephone(request.getTelephone())
                .codePin(passwordEncoder.encode(request.getCodePinInitial()))
                .role(Utilisateur.Role.TRANSPORTEUR)
                .tenantId(tenantId)
                .actif(true)
                .build();
        utilisateur = utilisateurRepository.save(utilisateur);

        Transporteur transporteur = Transporteur.builder()
                .utilisateur(utilisateur)
                .nomEntreprise(request.getNomEntreprise())
                .nomResponsable(request.getNomResponsable())
                .prenomResponsable(request.getPrenomResponsable())
                .numeroRegistreCommerce(request.getNumeroRegistreCommerce())
                .agent(agent)
                .tenantId(tenantId)
                .build();

        return TransporteurDto.TransporteurResponse.fromEntity(transporteurRepository.save(transporteur));
    }

    public List<TransporteurDto.TransporteurResponse> getTousLesTransporteurs(String tenantId) {
        return transporteurRepository.findByTenantId(tenantId).stream()
                .map(TransporteurDto.TransporteurResponse::fromEntity)
                .toList();
    }
}
