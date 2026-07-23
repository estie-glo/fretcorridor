package com.flysoft.fretcorridor.common.service;

import com.flysoft.fretcorridor.common.dto.NotificationDto;
import com.flysoft.fretcorridor.common.entity.Notification;
import com.flysoft.fretcorridor.common.entity.Utilisateur;
import com.flysoft.fretcorridor.common.repository.NotificationRepository;
import com.flysoft.fretcorridor.common.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * EF-NOT-01..03 — notifications multicanal.
 * IN_APP = canal web ; FCM/SMS/WhatsApp = stubs (coût / BSP Phase ultérieure).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional(readOnly = true)
    public List<NotificationDto.NotificationResponse> lister(
            UUID destinataireId, String tenantId) {
        return notificationRepository
                .findByDestinataireIdAndTenantIdOrderByDateCreationDesc(destinataireId, tenantId)
                .stream()
                .map(NotificationDto.NotificationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public long compterNonLues(UUID destinataireId, String tenantId) {
        return notificationRepository.countByDestinataireIdAndTenantIdAndLueFalse(
                destinataireId, tenantId);
    }

    @Transactional
    public NotificationDto.NotificationResponse marquerLue(
            UUID notificationId, UUID destinataireId, String tenantId) {
        Notification n = notificationRepository
                .findByIdAndDestinataireIdAndTenantId(notificationId, destinataireId, tenantId)
                .orElseThrow(() -> new RuntimeException("NOTIFICATION_INTROUVABLE"));
        n.setLue(true);
        return NotificationDto.NotificationResponse.fromEntity(notificationRepository.save(n));
    }

    /**
     * Envoi multicanal : toujours une entrée IN_APP ; canal préféré externe en stub
     * (EF-NOT-02 fenêtre WhatsApp / coût — pas d'appel BSP réel en S7).
     */
    @Transactional
    public List<NotificationDto.NotificationResponse> envoyer(
            NotificationDto.SendRequest request, String tenantId) {

        Utilisateur destinataire = utilisateurRepository.findById(request.getDestinataireId())
                .orElseThrow(() -> new RuntimeException("DESTINATAIRE_INTROUVABLE"));
        if (!destinataire.getTenantId().equals(tenantId)) {
            throw new RuntimeException("DESTINATAIRE_INTROUVABLE");
        }

        List<NotificationDto.NotificationResponse> created = new ArrayList<>();

        // Canal principal web
        created.add(NotificationDto.NotificationResponse.fromEntity(
                saveInApp(destinataire, request, Notification.StatutEnvoi.DELIVRE)));

        Notification.Canal prefere = parseCanal(request.getCanalPrefere());
        if (prefere != null && prefere != Notification.Canal.IN_APP) {
            created.add(NotificationDto.NotificationResponse.fromEntity(
                    saveExternalStub(destinataire, request, prefere)));
        } else if (destinataire.getFcmToken() != null && !destinataire.getFcmToken().isBlank()) {
            // Repli FCM si token présent (EF-NOT-03)
            created.add(NotificationDto.NotificationResponse.fromEntity(
                    saveExternalStub(destinataire, request, Notification.Canal.FCM)));
        }

        return created;
    }

    /** Notifie un utilisateur pour une transition de mission (S6 → S7). */
    @Transactional
    public void notifierMission(
            UUID destinataireId,
            String tenantId,
            UUID missionId,
            String statutAvant,
            String statutApres) {
        if (destinataireId == null) {
            return;
        }
        NotificationDto.SendRequest req = new NotificationDto.SendRequest();
        req.setDestinataireId(destinataireId);
        req.setType("MISSION_STATUT");
        req.setTitreFr("Mission mise à jour");
        req.setTitreEn("Mission updated");
        req.setCorpsFr("Statut : " + statutAvant + " → " + statutApres);
        req.setCorpsEn("Status: " + statutAvant + " → " + statutApres);
        req.setRessourceType("MISSION");
        req.setRessourceId(missionId);
        req.setCanalPrefere("IN_APP");
        envoyer(req, tenantId);
    }

    private Notification saveInApp(
            Utilisateur destinataire,
            NotificationDto.SendRequest request,
            Notification.StatutEnvoi statut) {
        return notificationRepository.save(Notification.builder()
                .tenantId(destinataire.getTenantId())
                .destinataireId(destinataire.getId())
                .canal(Notification.Canal.IN_APP)
                .type(request.getType())
                .titreFr(request.getTitreFr())
                .titreEn(request.getTitreEn())
                .corpsFr(request.getCorpsFr())
                .corpsEn(request.getCorpsEn())
                .ressourceType(request.getRessourceType())
                .ressourceId(request.getRessourceId())
                .statutEnvoi(statut)
                .build());
    }

    private Notification saveExternalStub(
            Utilisateur destinataire,
            NotificationDto.SendRequest request,
            Notification.Canal canal) {
        log.info("NOT stub {} → user {} (tokenFcm={})",
                canal, destinataire.getId(),
                destinataire.getFcmToken() != null ? "oui" : "non");
        return notificationRepository.save(Notification.builder()
                .tenantId(destinataire.getTenantId())
                .destinataireId(destinataire.getId())
                .canal(canal)
                .type(request.getType())
                .titreFr(request.getTitreFr())
                .titreEn(request.getTitreEn())
                .corpsFr(request.getCorpsFr())
                .corpsEn(request.getCorpsEn())
                .ressourceType(request.getRessourceType())
                .ressourceId(request.getRessourceId())
                .statutEnvoi(Notification.StatutEnvoi.STUB_QUEUED)
                .build());
    }

    private static Notification.Canal parseCanal(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Notification.Canal.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
