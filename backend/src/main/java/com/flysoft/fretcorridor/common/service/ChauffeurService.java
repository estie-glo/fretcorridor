package com.flysoft.fretcorridor.common.service;

import com.flysoft.fretcorridor.common.dto.ChauffeurDto;
import com.flysoft.fretcorridor.common.entity.*;
import com.flysoft.fretcorridor.common.repository.*;
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
    private final JournalAuditService journalAuditService;
    private final DocumentStorageService documentStorageService;

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

    /** Profil du chauffeur connecté (JWT subject = utilisateur.id). */
    public ChauffeurDto.ChauffeurResponse getProfilParUtilisateur(UUID utilisateurId, String tenantId) {
        Chauffeur chauffeur = chauffeurRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new RuntimeException("CHAUFFEUR_INTROUVABLE"));

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

    // ── LISTE KYC À TRAITER (attente + en cours docs) ─────────
    public List<ChauffeurDto.ChauffeurResponse> getKycEnAttente(String tenantId) {
        List<Chauffeur> enAttente = chauffeurRepository
                .findByStatutKycAndTenantId(Chauffeur.StatutKyc.EN_ATTENTE, tenantId);
        List<Chauffeur> enCours = chauffeurRepository
                .findByStatutKycAndTenantId(Chauffeur.StatutKyc.EN_COURS, tenantId);
        return java.util.stream.Stream.concat(enAttente.stream(), enCours.stream())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── LISTE TOUS LES CHAUFFEURS DU TENANT (back-office web) ─
    public List<ChauffeurDto.ChauffeurResponse> getChauffeursTenant(String tenantId) {
        return chauffeurRepository.findByTenantId(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── VALIDER KYC (par l'agent / back-office) ───────────────
    @Transactional
    public ChauffeurDto.ChauffeurResponse validerKyc(
            UUID chauffeurId,
            ChauffeurDto.ValidationKycRequest request,
            String tenantId,
            UUID acteurId,
            String acteurRole) {

        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new RuntimeException("CHAUFFEUR_INTROUVABLE"));

        if (!chauffeur.getTenantId().equals(tenantId)) {
            throw new RuntimeException("ACCES_REFUSE");
        }

        String avant = chauffeur.getStatutKyc().name() + "/" + chauffeur.getKycNiveau().name();

        if (Boolean.TRUE.equals(request.getApprouve())) {
            String niveau = request.getNouveauNiveau() != null
                    ? request.getNouveauNiveau()
                    : "NIVEAU_1";
            Chauffeur.KycNiveau kycNiveau = Chauffeur.KycNiveau.valueOf(niveau);

            // EF-IDA-02 : NIVEAU_2 exige justificatifs CNI + permis
            if (kycNiveau == Chauffeur.KycNiveau.NIVEAU_2) {
                if (isBlank(chauffeur.getUrlPhotoCNI()) || isBlank(chauffeur.getUrlPhotoPermis())) {
                    throw new RuntimeException("DOCS_MANQUANTS");
                }
            }

            chauffeur.setStatutKyc(Chauffeur.StatutKyc.VALIDE);
            chauffeur.setKycNiveau(kycNiveau);
            chauffeur.setDateValidationKyc(LocalDateTime.now());
        } else {
            chauffeur.setStatutKyc(Chauffeur.StatutKyc.REJETE);
        }

        chauffeurRepository.save(chauffeur);

        String apres = chauffeur.getStatutKyc().name() + "/" + chauffeur.getKycNiveau().name();
        String action = Boolean.TRUE.equals(request.getApprouve()) ? "KYC_VALIDER" : "KYC_REJETER";
        journalAuditService.enregistrer(
                tenantId, acteurId, acteurRole,
                action, "CHAUFFEUR", chauffeurId, avant, apres);

        log.info("KYC {} pour chauffeur : {}", Boolean.TRUE.equals(request.getApprouve()) ? "validé" : "rejeté", chauffeurId);

        return toResponse(chauffeur);
    }

    // ── UPLOAD DOCUMENT KYC (MinIO) ───────────────────────────
    @Transactional
    public ChauffeurDto.UploadDocumentResponse uploaderDocument(
            UUID chauffeurId,
            MultipartFile fichier,
            String typeDocument,
            String tenantId,
            UUID utilisateurId,
            String role) {

        if (fichier == null || fichier.isEmpty()) {
            throw new RuntimeException("FICHIER_VIDE");
        }
        if (!"CNI".equals(typeDocument) && !"PERMIS".equals(typeDocument)) {
            throw new RuntimeException("TYPE_DOCUMENT_INVALIDE");
        }

        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new RuntimeException("CHAUFFEUR_INTROUVABLE"));

        if (!chauffeur.getTenantId().equals(tenantId)) {
            throw new RuntimeException("ACCES_REFUSE");
        }

        if ("CHAUFFEUR".equals(role)) {
            if (chauffeur.getUtilisateur() == null
                    || !chauffeur.getUtilisateur().getId().equals(utilisateurId)) {
                throw new RuntimeException("ACCES_REFUSE");
            }
        }

        String urlDocument = documentStorageService.uploadKycDocument(
                tenantId, chauffeurId, typeDocument, fichier);

        if ("CNI".equals(typeDocument)) {
            chauffeur.setUrlPhotoCNI(urlDocument);
        } else {
            chauffeur.setUrlPhotoPermis(urlDocument);
        }

        if (chauffeur.getStatutKyc() == Chauffeur.StatutKyc.EN_ATTENTE
                || chauffeur.getStatutKyc() == Chauffeur.StatutKyc.VALIDE) {
            chauffeur.setStatutKyc(Chauffeur.StatutKyc.EN_COURS);
        }

        chauffeurRepository.save(chauffeur);

        journalAuditService.enregistrer(
                tenantId, null, null,
                "KYC_UPLOAD_" + typeDocument, "CHAUFFEUR", chauffeurId,
                null, urlDocument);

        return ChauffeurDto.UploadDocumentResponse.builder()
                .urlDocument(documentStorageService.resolveAccessUrl(urlDocument))
                .typeDocument(typeDocument)
                .message("Document " + typeDocument + " uploadé avec succès")
                .build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
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
                .urlPhotoCNI(documentStorageService.resolveAccessUrl(c.getUrlPhotoCNI()))
                .urlPhotoPermis(documentStorageService.resolveAccessUrl(c.getUrlPhotoPermis()))
                .dateEnrolement(c.getDateEnrolement())
                .dateValidationKyc(c.getDateValidationKyc())
                .agentNom(c.getAgent() != null
                    ? c.getAgent().getNom() + " " + c.getAgent().getPrenom()
                    : null)
                .badgeKyc(badge)
                .build();
    }
}
