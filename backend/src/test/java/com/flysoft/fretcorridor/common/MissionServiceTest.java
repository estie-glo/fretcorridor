package com.flysoft.fretcorridor.common;

import com.flysoft.fretcorridor.common.dto.MissionDto;
import com.flysoft.fretcorridor.common.entity.Axe;
import com.flysoft.fretcorridor.common.entity.Chauffeur;
import com.flysoft.fretcorridor.common.entity.Hub;
import com.flysoft.fretcorridor.common.entity.Mission;
import com.flysoft.fretcorridor.common.repository.AxeRepository;
import com.flysoft.fretcorridor.common.repository.ChauffeurRepository;
import com.flysoft.fretcorridor.common.repository.MissionRepository;
import com.flysoft.fretcorridor.common.service.JournalAuditService;
import com.flysoft.fretcorridor.common.service.MissionService;
import com.flysoft.fretcorridor.common.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    @Mock
    private MissionRepository missionRepository;
    @Mock
    private ChauffeurRepository chauffeurRepository;
    @Mock
    private AxeRepository axeRepository;
    @Mock
    private JournalAuditService journalAuditService;
    @Mock
    private NotificationService notificationService;

    private MissionService missionService;

    private final String tenantId = "BGFT_CM";
    private final UUID axeId = UUID.randomUUID();
    private final UUID missionId = UUID.randomUUID();
    private final UUID acteurId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        missionService = new MissionService(
                missionRepository, chauffeurRepository, axeRepository,
                journalAuditService, notificationService);
    }

    @Test
    void listerBureau_filtreParAxeEtStatut() {
        Mission mission = missionDemo();
        when(missionRepository.findByTenantIdAndAxeIdAndStatutOrderByDateDeclarationDesc(
                tenantId, axeId, Mission.StatutMission.EN_COURS))
                .thenReturn(List.of(mission));

        List<MissionDto.MissionResponse> result =
                missionService.listerBureau(tenantId, axeId, "EN_COURS");

        assertEquals(1, result.size());
        assertEquals(missionId, result.get(0).getId());
        assertEquals("Mbarga Paul", result.get(0).getChauffeurNom());
        assertTrue(result.get(0).isZoneSensible());
    }

    @Test
    void listerBureau_statutInvalide_leveException() {
        assertThrows(RuntimeException.class,
                () -> missionService.listerBureau(tenantId, null, "INEXISTANT"));
    }

    @Test
    void getMissionBureau_retourneDetail() {
        when(missionRepository.findByIdAndTenantId(missionId, tenantId))
                .thenReturn(Optional.of(missionDemo()));

        MissionDto.MissionResponse response = missionService.getMissionBureau(missionId, tenantId);

        assertEquals("Douala-NDjamena", response.getAxeNom());
        assertEquals(axeId, response.getAxeId());
    }

    @Test
    void listerBureau_sansFiltre() {
        when(missionRepository.findByTenantIdOrderByDateDeclarationDesc(tenantId))
                .thenReturn(List.of(missionDemo()));

        assertEquals(1, missionService.listerBureau(tenantId, null, null).size());
    }

    @Test
    void listerOffres_filtreCamionVideDeclare() {
        Mission offre = missionDemo();
        offre.setStatut(Mission.StatutMission.CAMION_VIDE_DECLARE);
        when(missionRepository.findByTenantIdAndStatutOrderByDateDeclarationDesc(
                tenantId, Mission.StatutMission.CAMION_VIDE_DECLARE))
                .thenReturn(List.of(offre));

        List<MissionDto.MissionResponse> result = missionService.listerOffres(tenantId, null);

        assertEquals(1, result.size());
        assertEquals(Mission.StatutMission.CAMION_VIDE_DECLARE.name(), result.get(0).getStatut());
    }

    @Test
    void listerOffres_filtreParAxe() {
        Mission offre = missionDemo();
        offre.setStatut(Mission.StatutMission.CAMION_VIDE_DECLARE);
        when(missionRepository.findByTenantIdAndAxeIdAndStatutOrderByDateDeclarationDesc(
                tenantId, axeId, Mission.StatutMission.CAMION_VIDE_DECLARE))
                .thenReturn(List.of(offre));

        assertEquals(1, missionService.listerOffres(tenantId, axeId).size());
    }

    @Test
    void listerOffres_exclutAxesSansMatching() {
        Mission offre = missionDemo();
        offre.setStatut(Mission.StatutMission.CAMION_VIDE_DECLARE);
        offre.getAxe().setMatchingActif(false);
        when(missionRepository.findByTenantIdAndStatutOrderByDateDeclarationDesc(
                tenantId, Mission.StatutMission.CAMION_VIDE_DECLARE))
                .thenReturn(List.of(offre));

        assertTrue(missionService.listerOffres(tenantId, null).isEmpty());
    }

    @Test
    void declarerVide_refuseAxeSansMatching() {
        UUID userId = UUID.randomUUID();
        UUID axeVerrouilleId = UUID.randomUUID();
        Hub hub = Hub.builder().id(UUID.randomUUID()).nom("Garoua").pays("CM").tenantId(tenantId).build();
        Axe axe = Axe.builder()
                .id(axeVerrouilleId)
                .nom("Garoua-Maroua")
                .hubDepart(hub)
                .hubArrivee(hub)
                .visibiliteActive(true)
                .matchingActif(false)
                .zoneSensible(true)
                .tenantId(tenantId)
                .build();
        Chauffeur chauffeur = Chauffeur.builder().id(UUID.randomUUID()).tenantId(tenantId).build();

        when(missionRepository.findByIdempotencyKeyAndTenantId("k1", tenantId))
                .thenReturn(Optional.empty());
        when(chauffeurRepository.findByUtilisateurId(userId)).thenReturn(Optional.of(chauffeur));
        when(axeRepository.findById(axeVerrouilleId)).thenReturn(Optional.of(axe));

        MissionDto.DeclareVideRequest req = new MissionDto.DeclareVideRequest();
        req.setAxeId(axeVerrouilleId);
        req.setLatitude(9.3);
        req.setLongitude(13.4);
        req.setTypeCamion("Semi");
        req.setCapaciteTonnes(20.0);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> missionService.declarerVide(req, "k1", userId, tenantId));
        assertEquals("AXE_VERROUILLE", ex.getMessage());
    }

    @Test
    void accepter_passeEnMissionAcceptee() {
        Mission mission = missionDemo();
        mission.setStatut(Mission.StatutMission.CAMION_VIDE_DECLARE);
        when(missionRepository.findByIdAndTenantId(missionId, tenantId))
                .thenReturn(Optional.of(mission));
        when(missionRepository.save(any(Mission.class))).thenAnswer(inv -> inv.getArgument(0));

        MissionDto.MissionResponse response =
                missionService.accepter(missionId, tenantId, acteurId, "OPERATEUR");

        assertEquals("MISSION_ACCEPTEE", response.getStatut());
        verify(journalAuditService).enregistrer(
                eq(tenantId), eq(acteurId), eq("OPERATEUR"),
                eq("MISSION_ACCEPTER"), eq("MISSION"), eq(missionId),
                eq("CAMION_VIDE_DECLARE"), eq("MISSION_ACCEPTEE"));
    }

    @Test
    void accepter_refuseSiStatutInvalide() {
        Mission mission = missionDemo();
        mission.setStatut(Mission.StatutMission.EN_COURS);
        when(missionRepository.findByIdAndTenantId(missionId, tenantId))
                .thenReturn(Optional.of(mission));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> missionService.accepter(missionId, tenantId, acteurId, "OPERATEUR"));
        assertEquals("TRANSITION_INVALIDE", ex.getMessage());
    }

    private Mission missionDemo() {
        Hub hub = Hub.builder().id(UUID.randomUUID()).nom("Douala").pays("CM").tenantId(tenantId).build();
        Axe axe = Axe.builder()
                .id(axeId)
                .nom("Douala-NDjamena")
                .hubDepart(hub)
                .hubArrivee(hub)
                .visibiliteActive(true)
                .matchingActif(true)
                .zoneSensible(true)
                .tenantId(tenantId)
                .build();
        Chauffeur chauffeur = Chauffeur.builder()
                .id(UUID.randomUUID())
                .nom("Mbarga")
                .prenom("Paul")
                .tenantId(tenantId)
                .build();

        return Mission.builder()
                .id(missionId)
                .idempotencyKey("k")
                .chauffeur(chauffeur)
                .axe(axe)
                .latitude(4.0)
                .longitude(9.0)
                .typeCamion("Semi")
                .capaciteTonnes(30.0)
                .statut(Mission.StatutMission.EN_COURS)
                .tenantId(tenantId)
                .dateDeclaration(LocalDateTime.now())
                .build();
    }
}
