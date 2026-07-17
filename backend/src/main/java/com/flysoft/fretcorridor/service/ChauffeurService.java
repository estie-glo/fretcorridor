package com.flysoft.fretcorridor.service;

import com.flysoft.fretcorridor.dto.ChauffeurDto;
import com.flysoft.fretcorridor.entity.*;
import com.flysoft.fretcorridor.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChauffeurService {

    private final ChauffeurRepository chauffeurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    // ── ENRÔLER UN CHAUFFEUR (par l'agent) ───────────────────
    @Transactional
    public ChauffeurDto.ChauffeurResponse enroler(
            ChauffeurDto.EnrolementRequest request,
            UUID agentUserId,
            String tenantId) {

        // 1. Vérifier que l'agent existe
        Agent agent = agentRepository.findByUtilisateurId(agentUserId)
                .orElseThrow(() -> new RuntimeException("AGENT_INTROUVABLE"));

        // 2. Vérifier que le téléphone n'est pas déjà pris
        if (utilisateurRepository.existsByTelephone(request.getTelephone())) {
            throw new RuntimeException("TELEPHONE_DEJA_UTILISE");
        }

        // 3. Créer l'utilisateur de base
        Utilisateur utilisateur = Utilisateur.builder()
                .telephone(request.getTelephone())
                .codePin(passwordEncoder.encode(request.getCodePinInitial()))
                .role(Utilisateur.Role.CHAUFFEUR)
                .tenantId(tenantId)
                .actif(true)
                .build();
        utilisateurRepository.save(utilisateur);

        // 4. Créer le profil chauffeur
        Chauffeur chauffeur = Chauffeur.builder()
                .utilisateur(utilisateur)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .numeroCNI(request.getNumeroCNI())
                .agent(agent)
                .tenantId(tenantId)
                .kycNiveau(Chauffeur.KycNiveau.NIVEAU_1) // KYC niveau 1 à l'enrôlement
                .statutKyc(Chauffeur.StatutKyc.EN_ATTENTE)
                .build();
        chauffeurRepository.save(chauffeur);

        log.info("Chauffeur enrôlé : {} par agent : {}", request.getTelephone(), agentUserId);

        return toResponse(chauffeur);
    }

    // ── RÉCUPÉRER PROFIL CHAUFFEUR ────────────────────────────
    public ChauffeurDto.ChauffeurResponse getProfil(UUID chauffeurId, String tenantId) {
        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new RuntimeException("CHAUFFEUR_INTROUVABLE"));

        // Vérifier isolation tenant
        if (!chauffeur.getTenantId().equals(tenantId)) {
            throw new RuntimeException("ACCES_REFUSE");
        }

        return toResponse(chauffeur);
    }

    // ── LISTE CHAUFFEURS PAR AGENT ────────────────────────────
    public List<ChauffeurDto.ChauffeurResponse> getMesChauffeurs(
            UUID agentUserId, String tenantId) {
        Agent agent = agentRepository.findByUtilisateurId(agentUserId)
                .orElseThrow(() -> new RuntimeException("AGENT_INTROUVABLE"));

        return chauffeurRepository
                .findByAgentIdAndTenantId(agent.getId(), tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── LISTE KYC EN ATTENTE ──────────────────────────────────
    public List<ChauffeurDto.ChauffeurResponse> getKycEnAttente(String tenantId) {
        return chauffeurRepository
                .findByStatutKycAndTenantId(Chauffeur.StatutKyc.EN_ATTENTE, tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── VALIDER KYC (par l'agent) ─────────────────────────────
    @Transactional
    public ChauffeurDto.ChauffeurResponse validerKyc(
            UUID chauffeurId,
            ChauffeurDto.ValidationKycRequest request,
            String tenantId) {

        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new RuntimeException("CHAUFFEUR_INTROUVABLE"));

        if (!chauffeur.getTenantId().equals(tenantId)) {
            throw new RuntimeException("ACCES_REFUSE");
        }

        if (request.isApprouve()) {
            chauffeur.setStatutKyc(Chauffeur.StatutKyc.VALIDE);
            chauffeur.setKycNiveau(
                Chauffeur.KycNiveau.valueOf(
                    request.getNouveauNiveau() != null
                        ? request.getNouveauNiveau()
                        : "NIVEAU_1"
                )
            );
            chauffeur.setDateValidationKyc(LocalDateTime.now());
        } else {
            chauffeur.setStatutKyc(Chauffeur.StatutKyc.REJETE);
        }

        chauffeurRepository.save(chauffeur);
        log.info("KYC {} pour chauffeur : {}", request.isApprouve() ? "validé" : "rejeté", chauffeurId);

        return toResponse(chauffeur);
    }

    // ── UPLOAD DOCUMENT KYC ───────────────────────────────────
    @Transactional
    public ChauffeurDto.UploadDocumentResponse uploaderDocument(
            UUID chauffeurId,
            MultipartFile fichier,
            String typeDocument,
            String tenantId) {

        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new RuntimeException("CHAUFFEUR_INTROUVABLE"));

        if (!chauffeur.getTenantId().equals(tenantId)) {
            throw new RuntimeException("ACCES_REFUSE");
        }

        // Simuler le stockage (MinIO sera intégré plus tard)
        // Pour l'instant on génère une URL fictive
        String urlDocument = "/documents/" + tenantId + "/" + chauffeurId + "/" + typeDocument.toLowerCase() + "_" + System.currentTimeMillis() + ".jpg";

        if ("CNI".equals(typeDocument)) {
            chauffeur.setUrlPhotoCNI(urlDocument);
        } else if ("PERMIS".equals(typeDocument)) {
            chauffeur.setUrlPhotoPermis(urlDocument);
        }

        // Passer en statut EN_COURS si au moins un document uploadé
        if (chauffeur.getStatutKyc() == Chauffeur.StatutKyc.EN_ATTENTE) {
            chauffeur.setStatutKyc(Chauffeur.StatutKyc.EN_COURS);
        }

        chauffeurRepository.save(chauffeur);

        return ChauffeurDto.UploadDocumentResponse.builder()
                .urlDocument(urlDocument)
                .typeDocument(typeDocument)
                .message("Document " + typeDocument + " uploadé avec succès")
                .build();
    }

    // ── MAPPER VERS DTO ───────────────────────────────────────
    private ChauffeurDto.ChauffeurResponse toResponse(Chauffeur c) {
        String badge = switch (c.getStatutKyc()) {
            case VALIDE    -> "KYC " + c.getKycNiveau().name().replace("_", " ") + " validé ✅";
            case EN_ATTENTE -> "KYC en attente ⏳";
            case EN_COURS  -> "KYC en cours 🔄";
            case REJETE    -> "KYC rejeté ❌";
        };

        return ChauffeurDto.ChauffeurResponse.builder()
                .id(c.getId())
                .nom(c.getNom())
                .prenom(c.getPrenom())
                .telephone(c.getUtilisateur().getTelephone())
                .tenantId(c.getTenantId())
                .kycNiveau(c.getKycNiveau().name())
                .statutKyc(c.getStatutKyc().name())
                .urlPhotoCNI(c.getUrlPhotoCNI())
                .urlPhotoPermis(c.getUrlPhotoPermis())
                .dateEnrolement(c.getDateEnrolement())
                .dateValidationKyc(c.getDateValidationKyc())
                .agentNom(c.getAgent() != null
                    ? c.getAgent().getNom() + " " + c.getAgent().getPrenom()
                    : null)
                .badgeKyc(badge)
                .build();
    }
}
