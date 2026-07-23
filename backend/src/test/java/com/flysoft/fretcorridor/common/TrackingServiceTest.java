package com.flysoft.fretcorridor.common;

import com.flysoft.fretcorridor.common.entity.Mission;
import com.flysoft.fretcorridor.common.entity.PositionGps;
import com.flysoft.fretcorridor.common.entity.Axe;
import com.flysoft.fretcorridor.common.entity.Hub;
import com.flysoft.fretcorridor.common.entity.Chauffeur;
import com.flysoft.fretcorridor.common.repository.ChauffeurRepository;
import com.flysoft.fretcorridor.common.repository.MissionRepository;
import com.flysoft.fretcorridor.common.repository.PositionGpsRepository;
import com.flysoft.fretcorridor.common.service.TrackingService;
import com.flysoft.fretcorridor.common.dto.TrackingDto;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private PositionGpsRepository positionGpsRepository;

    @Mock
    private ChauffeurRepository chauffeurRepository;

    private TrackingService trackingService;

    private final UUID missionId = UUID.randomUUID();
    private final String tenantId = "BGFT_CM";

    @BeforeEach
    void setUp() {
        trackingService = new TrackingService(
                missionRepository, positionGpsRepository, chauffeurRepository);
    }

    @Test
    void haversineKm_calculeDistanceRaisonnable() {
        // Douala ≈ N'Djamena ~ 1050–1200 km selon le tracé
        double km = TrackingService.haversineKm(4.0511, 9.7679, 12.1348, 15.0557);
        assertTrue(km > 900 && km < 1400, "distance=" + km);
    }

    @Test
    void getEta_retourneOkAvecDistance() {
        Mission mission = missionDemo(Mission.StatutMission.EN_COURS);
        when(missionRepository.findByIdAndTenantId(missionId, tenantId))
                .thenReturn(Optional.of(mission));
        when(positionGpsRepository.findFirstByMissionIdAndTenantIdOrderByRecordedAtDesc(missionId, tenantId))
                .thenReturn(Optional.empty());

        TrackingDto.EtaResponse eta = trackingService.getEta(missionId, tenantId);

        assertEquals("OK", eta.getStatutCalcul());
        assertNotNull(eta.getEtaMinutes());
        assertNotNull(eta.getDistanceRestanteKm());
        assertTrue(eta.getDistanceRestanteKm() > 0);
    }

    @Test
    void getTracking_fallbackSurDeclarationSiAucunePosition() {
        Mission mission = missionDemo(Mission.StatutMission.CAMION_VIDE_DECLARE);
        when(missionRepository.findByIdAndTenantId(missionId, tenantId))
                .thenReturn(Optional.of(mission));
        when(positionGpsRepository.findByMissionIdAndTenantIdOrderByRecordedAtAsc(missionId, tenantId))
                .thenReturn(List.of());

        TrackingDto.TrackingResponse tracking = trackingService.getTracking(missionId, tenantId);

        assertEquals(1, tracking.getPoints().size());
        assertEquals(4.0511, tracking.getLastPosition().getLatitude());
        assertTrue(tracking.isZoneSensible());
    }

    @Test
    void getTracking_utiliseLesPositionsEnregistrees() {
        Mission mission = missionDemo(Mission.StatutMission.EN_COURS);
        PositionGps p1 = PositionGps.builder()
                .id(UUID.randomUUID())
                .mission(mission)
                .latitude(4.0)
                .longitude(9.0)
                .recordedAt(LocalDateTime.now().minusHours(2))
                .tenantId(tenantId)
                .build();
        PositionGps p2 = PositionGps.builder()
                .id(UUID.randomUUID())
                .mission(mission)
                .latitude(5.0)
                .longitude(10.0)
                .recordedAt(LocalDateTime.now().minusHours(1))
                .tenantId(tenantId)
                .build();

        when(missionRepository.findByIdAndTenantId(missionId, tenantId))
                .thenReturn(Optional.of(mission));
        when(positionGpsRepository.findByMissionIdAndTenantIdOrderByRecordedAtAsc(missionId, tenantId))
                .thenReturn(List.of(p1, p2));

        TrackingDto.TrackingResponse tracking = trackingService.getTracking(missionId, tenantId);

        assertEquals(2, tracking.getPoints().size());
        assertEquals(5.0, tracking.getLastPosition().getLatitude());
    }

    @Test
    void enregistrerPositions_persisteBatchPourChauffeurMission() {
        Mission mission = missionDemo(Mission.StatutMission.EN_COURS);
        Chauffeur chauffeur = mission.getChauffeur();
        UUID userId = UUID.randomUUID();

        when(missionRepository.findByIdAndTenantId(missionId, tenantId))
                .thenReturn(Optional.of(mission));
        when(chauffeurRepository.findByUtilisateurId(userId))
                .thenReturn(Optional.of(chauffeur));
        when(positionGpsRepository.save(org.mockito.ArgumentMatchers.any(PositionGps.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TrackingDto.PositionBatchRequest request = new TrackingDto.PositionBatchRequest(
                missionId,
                List.of(
                        new TrackingDto.PointWriteRequest(4.0, 9.0, LocalDateTime.now(), 50.0, 10.0),
                        new TrackingDto.PointWriteRequest(4.1, 9.1, LocalDateTime.now(), 48.0, 12.0)));

        TrackingDto.PositionBatchResponse response = trackingService.enregistrerPositions(
                request, userId, tenantId);

        assertEquals(2, response.getEnregistrees());
        assertEquals(missionId, response.getMissionId());
    }

    private Mission missionDemo(Mission.StatutMission statut) {
        Hub arrivee = Hub.builder()
                .id(UUID.randomUUID())
                .nom("N'Djamena")
                .pays("TD")
                .latitude(12.1348)
                .longitude(15.0557)
                .tenantId(tenantId)
                .build();
        Hub depart = Hub.builder()
                .id(UUID.randomUUID())
                .nom("Douala")
                .pays("CM")
                .latitude(4.0511)
                .longitude(9.7679)
                .tenantId(tenantId)
                .build();
        Axe axe = Axe.builder()
                .id(UUID.randomUUID())
                .nom("Douala-NDjamena")
                .hubDepart(depart)
                .hubArrivee(arrivee)
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
                .idempotencyKey("test")
                .chauffeur(chauffeur)
                .axe(axe)
                .latitude(4.0511)
                .longitude(9.7679)
                .typeCamion("Semi")
                .capaciteTonnes(30.0)
                .statut(statut)
                .tenantId(tenantId)
                .dateDeclaration(LocalDateTime.now().minusHours(1))
                .build();
    }
}
