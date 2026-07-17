package com.flysoft.fretcorridor.service;

import com.flysoft.fretcorridor.dto.ChargeurDto;
import com.flysoft.fretcorridor.entity.Agent;
import com.flysoft.fretcorridor.entity.Chargeur;
import com.flysoft.fretcorridor.entity.Utilisateur;
import com.flysoft.fretcorridor.repository.AgentRepository;
import com.flysoft.fretcorridor.repository.ChargeurRepository;
import com.flysoft.fretcorridor.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChargeurService {

    private final ChargeurRepository chargeurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ChargeurDto.ChargeurResponse enroler(
            ChargeurDto.EnrolementRequest request, UUID agentUtilisateurId, String tenantId) {

        if (utilisateurRepository.findByTelephone(request.getTelephone()).isPresent()) {
            throw new RuntimeException("TELEPHONE_DEJA_UTILISE");
        }

        Agent agent = agentRepository.findByUtilisateurId(agentUtilisateurId)
                .orElseThrow(() -> new RuntimeException("AGENT_INTROUVABLE"));

        Utilisateur utilisateur = Utilisateur.builder()
                .telephone(request.getTelephone())
                .codePin(passwordEncoder.encode(request.getCodePinInitial()))
                .role(Utilisateur.Role.CHARGEUR)
                .tenantId(tenantId)
                .actif(true)
                .build();
        utilisateur = utilisateurRepository.save(utilisateur);

        Chargeur chargeur = Chargeur.builder()
                .utilisateur(utilisateur)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .entreprise(request.getEntreprise())
                .agent(agent)
                .tenantId(tenantId)
                .build();

        return ChargeurDto.ChargeurResponse.fromEntity(chargeurRepository.save(chargeur));
    }

    public List<ChargeurDto.ChargeurResponse> getTousLesChargeurs(String tenantId) {
        return chargeurRepository.findByTenantId(tenantId).stream()
                .map(ChargeurDto.ChargeurResponse::fromEntity)
                .toList();
    }
}
