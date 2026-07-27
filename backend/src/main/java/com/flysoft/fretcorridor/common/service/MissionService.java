package com.flysoft.fretcorridor.common.service;

import com.flysoft.fretcorridor.common.dto.MissionDto;
import com.flysoft.fretcorridor.common.entity.Axe;
import com.flysoft.fretcorridor.common.entity.Chauffeur;
import com.flysoft.fretcorridor.common.entity.Mission;
import com.flysoft.fretcorridor.common.repository.AxeRepository;
import com.flysoft.fretcorridor.common.repository.ChauffeurRepository;
import com.flysoft.fretcorridor.common.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final AxeRepository axeRepository;
    private final JournalAuditService journalAuditService;
    private final NotificationService notificationService;

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

        // EF-MKT-02 / EF-GEO-03 : matching seulement si le flag matchingActif est allumé
        if (!axe.isVisibiliteActive()) {
            throw new RuntimeException("AXE_INACTIF");
        }
        if (!axe.isMatchingActif()) {
            throw new RuntimeException("AXE_VERROUILLE");
        }

        Mission mission = Mission.builder()
                .idempotencyKey(idempotencyKey)
                .chauffeur(chauffeur)
                .axe(axe)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .typeCamion(request.getTypeCamion())
                .capaciteTonnes(request.getCapaciteTonnes())
                .disponibleDe(request.getDisponibleDe() != null
                        ? request.getDisponibleDe()
                        : java.time.LocalDateTime.now())
                .tenantId(tenantId)
                .build();

        Mission sauvegardee = missionRepository.save(mission);
        return MissionDto.MissionResponse.fromEntity(sauvegardee);
    }

    // Stub Sprint 3 : le vrai matching PostGIS arrive en Phase 2 (Sprint 9)
    public List<MissionDto.MissionResponse> getMatchsDisponibles(String tenantId) {
        return List.of();
    }

    @Transactional(readOnly = true)
    public List<MissionDto.MissionResponse> listerBureau(
            String tenantId, UUID axeId, String statut) {
        Mission.StatutMission statutEnum = null;
        if (statut != null && !statut.isBlank()) {
            try {
                statutEnum = Mission.StatutMission.valueOf(statut.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("STATUT_INVALIDE");
            }
        }

        List<Mission> missions;
        if (axeId != null && statutEnum != null) {
            missions = missionRepository.findByTenantIdAndAxeIdAndStatutOrderByDateDeclarationDesc(
                    tenantId, axeId, statutEnum);
        } else if (axeId != null) {
            missions = missionRepository.findByTenantIdAndAxeIdOrderByDateDeclarationDesc(tenantId, axeId);
        } else if (statutEnum != null) {
            missions = missionRepository.findByTenantIdAndStatutOrderByDateDeclarationDesc(tenantId, statutEnum);
        } else {
            missions = missionRepository.findByTenantIdOrderByDateDeclarationDesc(tenantId);
        }

        return missions.stream()
                .map(MissionDto.MissionResponse::fromEntity)
                .toList();
    }

    /**
     * S4 MKT — offres marketplace = missions déclarées vides (lecture chargeur / bureau).
     * Uniquement sur axes dont le matching GEO est actif.
     */
    @Transactional(readOnly = true)
    public List<MissionDto.MissionResponse> listerOffres(
            String tenantId, UUID axeId) {
        List<Mission> missions;
        if (axeId != null) {
            missions = missionRepository.findByTenantIdAndAxeIdAndStatutOrderByDateDeclarationDesc(
                    tenantId, axeId, Mission.StatutMission.CAMION_VIDE_DECLARE);
        } else {
            missions = missionRepository.findByTenantIdAndStatutOrderByDateDeclarationDesc(
                    tenantId, Mission.StatutMission.CAMION_VIDE_DECLARE);
        }

        return missions.stream()
                .filter(m -> m.getAxe() != null && m.getAxe().peutMatcher())
                .map(MissionDto.MissionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public MissionDto.MissionResponse getMissionBureau(UUID missionId, String tenantId) {
        Mission mission = missionRepository.findByIdAndTenantId(missionId, tenantId)
                .orElseThrow(() -> new RuntimeException("MISSION_INTROUVABLE"));
        return MissionDto.MissionResponse.fromEntity(mission);
    }

    /** S6 — accepter une offre / match → MISSION_ACCEPTEE. */
    @Transactional
    public MissionDto.MissionResponse accepter(
            UUID missionId, String tenantId, UUID acteurId, String acteurRole) {
        return transitionner(
                missionId, tenantId, acteurId, acteurRole,
                "MISSION_ACCEPTER",
                EnumSet.of(Mission.StatutMission.CAMION_VIDE_DECLARE, Mission.StatutMission.MATCH_PROPOSE),
                Mission.StatutMission.MISSION_ACCEPTEE,
                true);
    }

    /** S6 — démarrer la mission → EN_COURS. */
    @Transactional
    public MissionDto.MissionResponse demarrer(
            UUID missionId, String tenantId, UUID acteurId, String acteurRole) {
        return transitionner(
                missionId, tenantId, acteurId, acteurRole,
                "MISSION_DEMARRER",
                EnumSet.of(Mission.StatutMission.MISSION_ACCEPTEE),
                Mission.StatutMission.EN_COURS,
                false);
    }

    /** S6 — terminer → TERMINEE. */
    @Transactional
    public MissionDto.MissionResponse terminer(
            UUID missionId, String tenantId, UUID acteurId, String acteurRole) {
        return transitionner(
                missionId, tenantId, acteurId, acteurRole,
                "MISSION_TERMINER",
                EnumSet.of(Mission.StatutMission.EN_COURS),
                Mission.StatutMission.TERMINEE,
                false);
    }

    /** S6 — annuler. */
    @Transactional
    public MissionDto.MissionResponse annuler(
            UUID missionId, String tenantId, UUID acteurId, String acteurRole) {
        return transitionner(
                missionId, tenantId, acteurId, acteurRole,
                "MISSION_ANNULER",
                EnumSet.of(
                        Mission.StatutMission.CAMION_VIDE_DECLARE,
                        Mission.StatutMission.MATCH_PROPOSE,
                        Mission.StatutMission.MISSION_ACCEPTEE,
                        Mission.StatutMission.EN_COURS),
                Mission.StatutMission.ANNULEE,
                false);
    }

    private MissionDto.MissionResponse transitionner(
            UUID missionId,
            String tenantId,
            UUID acteurId,
            String acteurRole,
            String actionAudit,
            Set<Mission.StatutMission> depuis,
            Mission.StatutMission vers,
            boolean exigerMatchingActif) {

        Mission mission = missionRepository.findByIdAndTenantId(missionId, tenantId)
                .orElseThrow(() -> new RuntimeException("MISSION_INTROUVABLE"));

        if (!depuis.contains(mission.getStatut())) {
            throw new RuntimeException("TRANSITION_INVALIDE");
        }

        if (exigerMatchingActif) {
            Axe axe = mission.getAxe();
            if (axe == null || !axe.peutMatcher()) {
                throw new RuntimeException("AXE_VERROUILLE");
            }
        }

        String avant = mission.getStatut().name();
        mission.setStatut(vers);
        Mission saved = missionRepository.save(mission);

        journalAuditService.enregistrer(
                tenantId, acteurId, acteurRole,
                actionAudit, "MISSION", missionId,
                avant, vers.name());

        // EF-NOT — notification IN_APP à l'acteur + chauffeur (si profil lié)
        notificationService.notifierMission(acteurId, tenantId, missionId, avant, vers.name());
        if (saved.getChauffeur() != null && saved.getChauffeur().getUtilisateur() != null) {
            UUID chauffeurUserId = saved.getChauffeur().getUtilisateur().getId();
            if (!chauffeurUserId.equals(acteurId)) {
                notificationService.notifierMission(
                        chauffeurUserId, tenantId, missionId, avant, vers.name());
            }
        }

        return MissionDto.MissionResponse.fromEntity(saved);
    }

    // ── Mes déclarations (chauffeur connecté, mobile) ─────────
    @Transactional(readOnly = true)
    public List<MissionDto.MissionResponse> getMesDeclarations(
            UUID chauffeurUtilisateurId, String tenantId) {
        Chauffeur chauffeur = chauffeurRepository.findByUtilisateurId(chauffeurUtilisateurId)
                .orElseThrow(() -> new RuntimeException("CHAUFFEUR_INTROUVABLE"));
        return missionRepository.findByChauffeurIdAndTenantId(chauffeur.getId(), tenantId).stream()
                .map(MissionDto.MissionResponse::fromEntity)
                .toList();
    }

    // ── Détail d'une déclaration (chauffeur propriétaire) ──────
    @Transactional(readOnly = true)
    public MissionDto.MissionResponse getDetail(
            UUID missionId, UUID chauffeurUtilisateurId, String tenantId) {
        Mission mission = trouverEtVerifierProprietaire(missionId, chauffeurUtilisateurId, tenantId);
        return MissionDto.MissionResponse.fromEntity(mission);
    }

    // ── Modifier une déclaration (tant qu'elle n'a pas de match) ──
    @Transactional
    public MissionDto.MissionResponse modifier(
            UUID missionId, MissionDto.UpdateRequest request,
            UUID chauffeurUtilisateurId, String tenantId) {

        Mission mission = trouverEtVerifierProprietaire(missionId, chauffeurUtilisateurId, tenantId);

        if (mission.getStatut() != Mission.StatutMission.CAMION_VIDE_DECLARE) {
            throw new RuntimeException("MODIFICATION_IMPOSSIBLE_STATUT_" + mission.getStatut());
        }

        if (request.getTypeCamion() != null && !request.getTypeCamion().isBlank()) {
            mission.setTypeCamion(request.getTypeCamion());
        }
        if (request.getCapaciteTonnes() != null && request.getCapaciteTonnes() > 0) {
            mission.setCapaciteTonnes(request.getCapaciteTonnes());
        }
        if (request.getDisponibleDe() != null) {
            mission.setDisponibleDe(request.getDisponibleDe());
        }

        Mission saved = missionRepository.save(mission);

        journalAuditService.enregistrer(
                tenantId, chauffeurUtilisateurId, "CHAUFFEUR",
                "DECLARATION_MODIFIER", "MISSION", missionId,
                null, saved.getStatut().name());

        return MissionDto.MissionResponse.fromEntity(saved);
    }

    // ── Supprimer une déclaration (suppression logique) ─────────
    @Transactional
    public void supprimer(UUID missionId, UUID chauffeurUtilisateurId, String tenantId) {
        Mission mission = trouverEtVerifierProprietaire(missionId, chauffeurUtilisateurId, tenantId);

        if (mission.getStatut() != Mission.StatutMission.CAMION_VIDE_DECLARE) {
            throw new RuntimeException("SUPPRESSION_IMPOSSIBLE_STATUT_" + mission.getStatut());
        }

        String avant = mission.getStatut().name();
        mission.setStatut(Mission.StatutMission.ANNULEE);
        missionRepository.save(mission);

        journalAuditService.enregistrer(
                tenantId, chauffeurUtilisateurId, "CHAUFFEUR",
                "DECLARATION_SUPPRIMER", "MISSION", missionId,
                avant, Mission.StatutMission.ANNULEE.name());
    }

    private Mission trouverEtVerifierProprietaire(
            UUID missionId, UUID chauffeurUtilisateurId, String tenantId) {
        Mission mission = missionRepository.findByIdAndTenantId(missionId, tenantId)
                .orElseThrow(() -> new RuntimeException("MISSION_INTROUVABLE"));

        Chauffeur chauffeur = chauffeurRepository.findByUtilisateurId(chauffeurUtilisateurId)
                .orElseThrow(() -> new RuntimeException("CHAUFFEUR_INTROUVABLE"));

        if (!mission.getChauffeur().getId().equals(chauffeur.getId())) {
            throw new RuntimeException("MISSION_INTROUVABLE"); // pas la sienne → pas de fuite d'info
        }

        return mission;
    }
}
