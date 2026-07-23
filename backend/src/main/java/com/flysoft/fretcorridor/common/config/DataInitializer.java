package com.flysoft.fretcorridor.common.config;

import com.flysoft.fretcorridor.common.entity.*;
import com.flysoft.fretcorridor.common.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Jeu de données de démo pour le développement local (web + mobile).
 * Ne s'exécute que si la table utilisateurs est vide.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final AgentRepository agentRepository;
    private final HubRepository hubRepository;
    private final AxeRepository axeRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final MissionRepository missionRepository;
    private final PositionGpsRepository positionGpsRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        if (utilisateurRepository.count() == 0) {
            seedComplet();
            return;
        }

        // Base déjà peuplée : migrer flags GEO si besoin, puis compléter seeds
        migrerActivationFlagsSiBesoin();
        seedEpineNordSiPossible();
        seedBnftTchadSiPossible();
        if (missionRepository.count() == 0) {
            seedMissionDemoSiPossible();
        }
        seedOffresChargeurSiPossible();
        seedNotificationsSiPossible();
        seedKycDocsDemoSiPossible();
    }

    private void seedComplet() {
        log.info("Initialisation des données de démo…");

        String pinHash = passwordEncoder.encode("1234");
        String tenant = "BGFT_CM";

        Utilisateur agentUser = utilisateurRepository.save(Utilisateur.builder()
                .telephone("+237600000001")
                .codePin(pinHash)
                .role(Utilisateur.Role.AGENT)
                .tenantId(tenant)
                .actif(true)
                .build());

        Agent agent = agentRepository.save(Agent.builder()
                .utilisateur(agentUser)
                .nom("Nkodo")
                .prenom("Jean")
                .zone("Douala-Port")
                .bureauFret("BGFT Cameroun")
                .tenantId(tenant)
                .build());

        utilisateurRepository.save(Utilisateur.builder()
                .telephone("+237600000002")
                .codePin(pinHash)
                .role(Utilisateur.Role.OPERATEUR)
                .tenantId(tenant)
                .actif(true)
                .build());

        utilisateurRepository.save(Utilisateur.builder()
                .telephone("+237600000003")
                .codePin(pinHash)
                .role(Utilisateur.Role.CHARGEUR)
                .tenantId(tenant)
                .actif(true)
                .build());

        Utilisateur chauffeurUser = utilisateurRepository.save(Utilisateur.builder()
                .telephone("+237600000010")
                .codePin(pinHash)
                .role(Utilisateur.Role.CHAUFFEUR)
                .tenantId(tenant)
                .actif(true)
                .build());

        Chauffeur chauffeur = chauffeurRepository.save(Chauffeur.builder()
                .utilisateur(chauffeurUser)
                .nom("Mbarga")
                .prenom("Paul")
                .numeroCNI("CM123456")
                .agent(agent)
                .tenantId(tenant)
                .kycNiveau(Chauffeur.KycNiveau.NIVEAU_1)
                .statutKyc(Chauffeur.StatutKyc.EN_ATTENTE)
                .build());

        Hub douala = hubRepository.save(Hub.builder()
                .nom("Douala")
                .pays("CM")
                .latitude(4.0511)
                .longitude(9.7679)
                .tenantId(tenant)
                .build());

        Hub ndjamena = hubRepository.save(Hub.builder()
                .nom("N'Djamena")
                .pays("TD")
                .latitude(12.1348)
                .longitude(15.0557)
                .tenantId(tenant)
                .build());

        Hub yaounde = hubRepository.save(Hub.builder()
                .nom("Yaoundé")
                .pays("CM")
                .latitude(3.8480)
                .longitude(11.5021)
                .tenantId(tenant)
                .build());

        Axe axeNord = axeRepository.save(Axe.builder()
                .nom("Douala-NDjamena")
                .hubDepart(douala)
                .hubArrivee(ndjamena)
                .visibiliteActive(true)
                .matchingActif(true)
                .financementActif(false)
                .zoneSensible(true)
                .tenantId(tenant)
                .build());

        axeRepository.save(Axe.builder()
                .nom("Douala-Yaounde")
                .hubDepart(douala)
                .hubArrivee(yaounde)
                .visibiliteActive(true)
                .matchingActif(true)
                .financementActif(false)
                .zoneSensible(false)
                .tenantId(tenant)
                .build());

        seedEpineNordSiPossible();
        seedBnftTchadSiPossible();
        seedMissionSurAxe(axeNord, chauffeur, tenant);
        seedOffresSurAxes(axeNord, chauffeur, tenant);
        seedNotificationsSiPossible();
        seedKycDocsDemoSiPossible();

        log.info("Données de démo prêtes — PIN=1234, agent=+237600000001, opérateur=+237600000002, chargeur=+237600000003, bureau Tchad=+235660000001");
    }

    /**
     * Tenant BNFT Tchad — agent bureau + CDC Phase 1 (corridor ancre + épine Cameroun).
     * Idempotent : safe sur base déjà peuplée.
     */
    private void seedBnftTchadSiPossible() {
        String tenant = "BNFT_TD";
        String telephone = "+235660000001";

        if (!utilisateurRepository.existsByTelephone(telephone)) {
            String pinHash = passwordEncoder.encode("1234");

            Utilisateur agentUser = utilisateurRepository.save(Utilisateur.builder()
                    .telephone(telephone)
                    .codePin(pinHash)
                    .role(Utilisateur.Role.AGENT)
                    .tenantId(tenant)
                    .actif(true)
                    .build());

            agentRepository.save(Agent.builder()
                    .utilisateur(agentUser)
                    .nom("Mahamat")
                    .prenom("Idriss")
                    .zone("N'Djamena-Port")
                    .bureauFret("BNFT Tchad")
                    .tenantId(tenant)
                    .build());

            log.info("Tenant BNFT Tchad — agent bureau {} (PIN=1234)", telephone);
        }

        seedCorridorDoualaNdjamenaPourTenant(tenant);
        retirerAxesHorsCdcPourBnft(tenant);
    }

    /**
     * CDC §5.2 Phase 1 — corridor ancre Douala–N'Djamena + épine domestique Cameroun
     * Douala → Yaoundé → Ngaoundéré → Garoua → Maroua.
     */
    private void seedCorridorDoualaNdjamenaPourTenant(String tenant) {
        Hub douala = hubOuCreer(tenant, "Douala", "CM", 4.0511, 9.7679);
        Hub yaounde = hubOuCreer(tenant, "Yaoundé", "CM", 3.8480, 11.5021);
        Hub ngaoundere = hubOuCreer(tenant, "Ngaoundéré", "CM", 7.3159, 13.5847);
        Hub garoua = hubOuCreer(tenant, "Garoua", "CM", 9.3265, 13.3958);
        Hub maroua = hubOuCreer(tenant, "Maroua", "CM", 10.5913, 14.3153);
        Hub ndjamena = hubOuCreer(tenant, "N'Djamena", "TD", 12.1348, 15.0557);

        axeOuCreer(tenant, "Douala-NDjamena", douala, ndjamena, true, true, true);
        axeOuCreer(tenant, "Douala-Yaounde", douala, yaounde, true, true, false);
        axeOuCreer(tenant, "Yaounde-Ngaoundere", yaounde, ngaoundere, true, true, false);
        axeOuCreer(tenant, "Ngaoundere-Garoua", ngaoundere, garoua, true, true, false);
        axeOuCreer(tenant, "Garoua-Maroua", garoua, maroua, true, false, true);

        log.info("Corridor CDC Phase 1 disponible pour tenant {}", tenant);
    }

    /** Supprime les axes / hubs BNFT hors CDC Phase 1 (ex. domestiques Tchad). */
    private void retirerAxesHorsCdcPourBnft(String tenant) {
        var autorises = java.util.Set.of(
                "Douala-NDjamena",
                "Douala-Yaounde",
                "Yaounde-Ngaoundere",
                "Ngaoundere-Garoua",
                "Garoua-Maroua");

        var aSupprimer = axeRepository.findByTenantId(tenant).stream()
                .filter(a -> !autorises.contains(a.getNom()))
                .toList();
        if (!aSupprimer.isEmpty()) {
            axeRepository.deleteAll(aSupprimer);
            log.info("BNFT_TD — axe(s) hors CDC retirés : {}",
                    aSupprimer.stream().map(Axe::getNom).toList());
        }

        var hubsUtilises = axeRepository.findByTenantId(tenant).stream()
                .flatMap(a -> java.util.stream.Stream.of(
                        a.getHubDepart().getId(), a.getHubArrivee().getId()))
                .collect(java.util.stream.Collectors.toSet());
        var hubsOrphelins = hubRepository.findByTenantId(tenant).stream()
                .filter(h -> !hubsUtilises.contains(h.getId()))
                .toList();
        if (!hubsOrphelins.isEmpty()) {
            hubRepository.deleteAll(hubsOrphelins);
            log.info("BNFT_TD — hubs hors CDC retirés : {}",
                    hubsOrphelins.stream().map(Hub::getNom).toList());
        }
    }

    private Hub hubOuCreer(String tenant, String nom, String pays, double lat, double lng) {
        return hubRepository.findByTenantId(tenant).stream()
                .filter(h -> nom.equals(h.getNom()))
                .findFirst()
                .orElseGet(() -> hubRepository.save(Hub.builder()
                        .nom(nom)
                        .pays(pays)
                        .latitude(lat)
                        .longitude(lng)
                        .tenantId(tenant)
                        .build()));
    }

    private void axeOuCreer(
            String tenant,
            String nom,
            Hub depart,
            Hub arrivee,
            boolean visibilite,
            boolean matching,
            boolean zoneSensible) {
        boolean exists = axeRepository.findByTenantId(tenant).stream()
                .anyMatch(a -> nom.equals(a.getNom()));
        if (exists) {
            return;
        }
        axeRepository.save(Axe.builder()
                .nom(nom)
                .hubDepart(depart)
                .hubArrivee(arrivee)
                .visibiliteActive(visibilite)
                .matchingActif(matching)
                .financementActif(false)
                .zoneSensible(zoneSensible)
                .tenantId(tenant)
                .build());
    }

    /**
     * CDC §5.2 — Phase 1 : épine domestique Douala–Yaoundé–Ngaoundéré–Garoua–Maroua
     * (partage la jambe sud du corridor ancre et le même client-ancre BGFT).
     * Idempotent : s'applique aussi sur une base déjà seedée (ne recrée rien si déjà présent).
     */
    private void seedEpineNordSiPossible() {
        String tenant = "BGFT_CM";
        var hubs = hubRepository.findByTenantId(tenant);
        Hub yaounde = hubs.stream().filter(h -> h.getNom().startsWith("Yaound")).findFirst().orElse(null);
        if (yaounde == null || hubs.stream().anyMatch(h -> "Ngaoundéré".equals(h.getNom()))) {
            return;
        }

        Hub ngaoundere = hubRepository.save(Hub.builder()
                .nom("Ngaoundéré")
                .pays("CM")
                .latitude(7.3159)
                .longitude(13.5847)
                .tenantId(tenant)
                .build());

        Hub garoua = hubRepository.save(Hub.builder()
                .nom("Garoua")
                .pays("CM")
                .latitude(9.3265)
                .longitude(13.3958)
                .tenantId(tenant)
                .build());

        Hub maroua = hubRepository.save(Hub.builder()
                .nom("Maroua")
                .pays("CM")
                .latitude(10.5913)
                .longitude(14.3153)
                .tenantId(tenant)
                .build());

        axeRepository.save(Axe.builder()
                .nom("Yaounde-Ngaoundere")
                .hubDepart(yaounde)
                .hubArrivee(ngaoundere)
                .visibiliteActive(true)
                .matchingActif(true)
                .financementActif(false)
                .zoneSensible(false)
                .tenantId(tenant)
                .build());

        axeRepository.save(Axe.builder()
                .nom("Ngaoundere-Garoua")
                .hubDepart(ngaoundere)
                .hubArrivee(garoua)
                .visibiliteActive(true)
                .matchingActif(true)
                .financementActif(false)
                .zoneSensible(false)
                .tenantId(tenant)
                .build());

        // Garoua-Maroua : Extrême-Nord — visibilité OK, matching gelé (EF-GEO-03 / §5.3)
        axeRepository.save(Axe.builder()
                .nom("Garoua-Maroua")
                .hubDepart(garoua)
                .hubArrivee(maroua)
                .visibiliteActive(true)
                .matchingActif(false)
                .financementActif(false)
                .zoneSensible(true)
                .tenantId(tenant)
                .build());

        log.info("Épine Nord semée : hubs Ngaoundéré/Garoua/Maroua + axes Yaoundé→Ngaoundéré→Garoua→Maroua");
    }

    private void seedNotificationsSiPossible() {
        if (notificationRepository.count() > 0) {
            return;
        }
        String tenant = "BGFT_CM";
        utilisateurRepository.findByTelephone("+237600000002").ifPresent(op -> {
            notificationRepository.save(Notification.builder()
                    .tenantId(tenant)
                    .destinataireId(op.getId())
                    .canal(Notification.Canal.IN_APP)
                    .type("SYSTEME")
                    .titreFr("Bienvenue sur FretCorridor")
                    .titreEn("Welcome to FretCorridor")
                    .corpsFr("Centre de notifications opérationnel (S7). Les alertes mission apparaîtront ici.")
                    .corpsEn("Notifications center is live (S7). Mission alerts will appear here.")
                    .statutEnvoi(Notification.StatutEnvoi.DELIVRE)
                    .build());
            notificationRepository.save(Notification.builder()
                    .tenantId(tenant)
                    .destinataireId(op.getId())
                    .canal(Notification.Canal.WHATSAPP)
                    .type("SYSTEME")
                    .titreFr("Canal WhatsApp (stub)")
                    .titreEn("WhatsApp channel (stub)")
                    .corpsFr("Envoi WhatsApp simulé — BSP réel en phase ultérieure (coût fenêtre 24h).")
                    .corpsEn("WhatsApp send simulated — real BSP in a later phase (24h window cost).")
                    .statutEnvoi(Notification.StatutEnvoi.STUB_QUEUED)
                    .build());
        });
        utilisateurRepository.findByTelephone("+237600000003").ifPresent(ch ->
                notificationRepository.save(Notification.builder()
                        .tenantId(tenant)
                        .destinataireId(ch.getId())
                        .canal(Notification.Canal.IN_APP)
                        .type("MKT")
                        .titreFr("Nouvelles offres camion vide")
                        .titreEn("New empty-truck offers")
                        .corpsFr("Des camions vides sont disponibles sur vos axes. Consultez Offres.")
                        .corpsEn("Empty trucks are available on your corridors. Check Offers.")
                        .statutEnvoi(Notification.StatutEnvoi.DELIVRE)
                        .build()));
        log.info("Notifications de démo semées (opérateur + chargeur)");
    }

    /** S8 — chauffeurs démo avec CNI+permis pour pouvoir tester KYC N2. */
    private void seedKycDocsDemoSiPossible() {
        for (Chauffeur c : chauffeurRepository.findByTenantId("BGFT_CM")) {
            if (c.getUrlPhotoCNI() != null && !c.getUrlPhotoCNI().isBlank()
                    && c.getUrlPhotoPermis() != null && !c.getUrlPhotoPermis().isBlank()) {
                continue;
            }
            c.setUrlPhotoCNI("http://localhost:9000/fretcorridor-kyc/demo/" + c.getId() + "/cni.jpg");
            c.setUrlPhotoPermis("http://localhost:9000/fretcorridor-kyc/demo/" + c.getId() + "/permis.jpg");
            if (c.getStatutKyc() == Chauffeur.StatutKyc.EN_ATTENTE
                    || c.getStatutKyc() == Chauffeur.StatutKyc.VALIDE) {
                c.setStatutKyc(Chauffeur.StatutKyc.EN_COURS);
            }
            chauffeurRepository.save(c);
            log.info("Docs KYC démo attachés pour chauffeur {}", c.getId());
        }
    }

    /**
     * Migration one-shot des flags GEO.
     * Préfère la réparation via JPA (axes encore à false/false/false).
     * La migration SQL native depuis etat_activation est best-effort.
     */
    @SuppressWarnings("unchecked")
    private void migrerActivationFlagsSiBesoin() {
        try {
            List<String> cols = entityManager.createNativeQuery(
                            "SELECT column_name FROM information_schema.columns "
                                    + "WHERE table_schema = 'public' AND table_name = 'axes' "
                                    + "AND column_name = 'etat_activation'")
                    .getResultList();
            if (!cols.isEmpty()) {
                entityManager.createNativeQuery("""
                        UPDATE axes SET
                          visibilite_active = CASE
                            WHEN etat_activation = 'INACTIF' THEN false ELSE true END,
                          matching_actif = CASE
                            WHEN etat_activation = 'ACTIF' THEN true ELSE false END,
                          financement_actif = COALESCE(financement_actif, false)
                        WHERE (visibilite_active IS NULL OR matching_actif IS NULL
                               OR (visibilite_active = false AND matching_actif = false
                                   AND financement_actif = false))
                        """).executeUpdate();
                log.info("Migration GEO flags depuis etat_activation effectuée");
            }
        } catch (Exception e) {
            log.warn("Migration SQL GEO flags ignorée : {}", e.getMessage());
        }

        entityManager.flush();
        entityManager.clear();

        for (Axe axe : axeRepository.findAll()) {
            if (axe.isVisibiliteActive() || axe.isMatchingActif() || axe.isFinancementActif()) {
                continue;
            }
            if ("Garoua-Maroua".equals(axe.getNom())) {
                axe.setVisibiliteActive(true);
                axe.setMatchingActif(false);
            } else {
                axe.setVisibiliteActive(true);
                axe.setMatchingActif(true);
            }
            axe.setFinancementActif(false);
            axeRepository.save(axe);
            log.info("Flags GEO initialisés pour axe {}", axe.getNom());
        }
    }

    private void seedMissionDemoSiPossible() {
        String tenant = "BGFT_CM";
        var axes = axeRepository.findByTenantId(tenant);
        var chauffeurs = chauffeurRepository.findByTenantId(tenant);
        if (axes.isEmpty() || chauffeurs.isEmpty()) {
            log.warn("Impossible de semer une mission démo (axe/chauffeur manquant)");
            return;
        }
        Axe axe = axes.stream()
                .filter(a -> a.getNom().contains("NDjamena") || a.getNom().contains("N'Djamena"))
                .findFirst()
                .orElse(axes.get(0));
        seedMissionSurAxe(axe, chauffeurs.get(0), tenant);
        log.info("Mission démo + positions GPS semées pour le dashboard bureau");
    }

    private void seedOffresChargeurSiPossible() {
        String tenant = "BGFT_CM";
        var existantes = missionRepository.findByTenantIdAndStatutOrderByDateDeclarationDesc(
                tenant, Mission.StatutMission.CAMION_VIDE_DECLARE);
        if (!existantes.isEmpty()) {
            return;
        }

        var axes = axeRepository.findByTenantId(tenant);
        var chauffeurs = chauffeurRepository.findByTenantId(tenant);
        if (axes.isEmpty() || chauffeurs.isEmpty()) {
            log.warn("Impossible de semer des offres camion vide (axe/chauffeur manquant)");
            return;
        }

        Axe axeNord = axes.stream()
                .filter(a -> a.getNom().contains("NDjamena") || a.getNom().contains("N'Djamena"))
                .findFirst()
                .orElse(axes.get(0));
        seedOffresSurAxes(axeNord, chauffeurs.get(0), tenant);
        log.info("Offres camion vide semées pour la vue chargeur");
    }

    private void seedOffresSurAxes(Axe axeNord, Chauffeur chauffeur, String tenant) {
        if (missionRepository.findByIdempotencyKeyAndTenantId("seed-offre-vide-001", tenant).isPresent()) {
            return;
        }

        Axe axeSud = axeRepository.findByTenantId(tenant).stream()
                .filter(a -> a.getNom().contains("Yaounde") || a.getNom().contains("Yaoundé"))
                .findFirst()
                .orElse(axeNord);

        missionRepository.save(Mission.builder()
                .idempotencyKey("seed-offre-vide-001")
                .chauffeur(chauffeur)
                .axe(axeNord)
                .latitude(4.0610)
                .longitude(9.7100)
                .typeCamion("Semi-remorque")
                .capaciteTonnes(28.0)
                .statut(Mission.StatutMission.CAMION_VIDE_DECLARE)
                .tenantId(tenant)
                .dateDeclaration(LocalDateTime.now().minusHours(2))
                .build());

        missionRepository.save(Mission.builder()
                .idempotencyKey("seed-offre-vide-002")
                .chauffeur(chauffeur)
                .axe(axeSud)
                .latitude(3.8700)
                .longitude(11.5200)
                .typeCamion("Porteur")
                .capaciteTonnes(18.0)
                .statut(Mission.StatutMission.CAMION_VIDE_DECLARE)
                .tenantId(tenant)
                .dateDeclaration(LocalDateTime.now().minusMinutes(45))
                .build());
    }

    private void seedMissionSurAxe(Axe axeNord, Chauffeur chauffeur, String tenant) {
        Mission mission = missionRepository.save(Mission.builder()
                .idempotencyKey("seed-mission-demo-001")
                .chauffeur(chauffeur)
                .axe(axeNord)
                .latitude(4.0511)
                .longitude(9.7679)
                .typeCamion("Semi-remorque")
                .capaciteTonnes(30.0)
                .statut(Mission.StatutMission.EN_COURS)
                .tenantId(tenant)
                .dateDeclaration(LocalDateTime.now().minusHours(6))
                .build());

        LocalDateTime base = LocalDateTime.now().minusHours(5);
        positionGpsRepository.save(PositionGps.builder()
                .mission(mission)
                .latitude(4.0511)
                .longitude(9.7679)
                .recordedAt(base)
                .vitesseKmh(0.0)
                .tenantId(tenant)
                .build());
        positionGpsRepository.save(PositionGps.builder()
                .mission(mission)
                .latitude(5.4800)
                .longitude(10.4200)
                .recordedAt(base.plusHours(2))
                .vitesseKmh(62.0)
                .tenantId(tenant)
                .build());
        positionGpsRepository.save(PositionGps.builder()
                .mission(mission)
                .latitude(7.3200)
                .longitude(13.5800)
                .recordedAt(base.plusHours(4))
                .vitesseKmh(58.0)
                .tenantId(tenant)
                .build());
    }
}
