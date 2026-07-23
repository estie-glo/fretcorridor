package com.flysoft.fretcorridor.common.service;

import com.flysoft.fretcorridor.common.dto.TrackingDto;
import com.flysoft.fretcorridor.common.entity.Chauffeur;
import com.flysoft.fretcorridor.common.entity.Mission;
import com.flysoft.fretcorridor.common.entity.PositionGps;
import com.flysoft.fretcorridor.common.repository.ChauffeurRepository;
import com.flysoft.fretcorridor.common.repository.MissionRepository;
import com.flysoft.fretcorridor.common.repository.PositionGpsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private static final double VITESSE_MOYENNE_KMH = 55.0;

    private static final Set<Mission.StatutMission> STATUTS_TRACKING_ACTIF = EnumSet.of(
            Mission.StatutMission.CAMION_VIDE_DECLARE,
            Mission.StatutMission.MATCH_PROPOSE,
            Mission.StatutMission.MISSION_ACCEPTEE,
            Mission.StatutMission.EN_COURS);

    private final MissionRepository missionRepository;
    private final PositionGpsRepository positionGpsRepository;
    private final ChauffeurRepository chauffeurRepository;

    @Transactional(readOnly = true)
    public TrackingDto.TrackingResponse getTracking(UUID missionId, String tenantId) {
        Mission mission = missionRepository.findByIdAndTenantId(missionId, tenantId)
                .orElseThrow(() -> new RuntimeException("MISSION_INTROUVABLE"));

        List<PositionGps> positions = positionGpsRepository
                .findByMissionIdAndTenantIdOrderByRecordedAtAsc(missionId, tenantId);

        List<TrackingDto.PointResponse> points = new ArrayList<>(
                positions.stream().map(TrackingDto.PointResponse::fromEntity).toList());

        // Fallback : position de déclaration si aucune trace GPS encore
        if (points.isEmpty()) {
            points.add(TrackingDto.PointResponse.builder()
                    .latitude(mission.getLatitude())
                    .longitude(mission.getLongitude())
                    .recordedAt(mission.getDateDeclaration())
                    .build());
        }

        TrackingDto.PointResponse last = points.get(points.size() - 1);

        return TrackingDto.TrackingResponse.builder()
                .missionId(mission.getId())
                .statutMission(mission.getStatut().name())
                .lastPosition(last)
                .points(points)
                .zoneSensible(mission.getAxe().isZoneSensible())
                .build();
    }

    @Transactional(readOnly = true)
    public TrackingDto.EtaResponse getEta(UUID missionId, String tenantId) {
        Mission mission = missionRepository.findByIdAndTenantId(missionId, tenantId)
                .orElseThrow(() -> new RuntimeException("MISSION_INTROUVABLE"));

        if (mission.getStatut() == Mission.StatutMission.TERMINEE
                || mission.getStatut() == Mission.StatutMission.ANNULEE) {
            return TrackingDto.EtaResponse.builder()
                    .missionId(missionId)
                    .statutCalcul("MISSION_TERMINEE")
                    .build();
        }

        Double hubLat = mission.getAxe().getHubArrivee().getLatitude();
        Double hubLng = mission.getAxe().getHubArrivee().getLongitude();
        if (hubLat == null || hubLng == null) {
            return TrackingDto.EtaResponse.builder()
                    .missionId(missionId)
                    .statutCalcul("INSUFFISANT")
                    .build();
        }

        PositionGps last = positionGpsRepository
                .findFirstByMissionIdAndTenantIdOrderByRecordedAtDesc(missionId, tenantId)
                .orElse(null);

        double lat = last != null ? last.getLatitude() : mission.getLatitude();
        double lng = last != null ? last.getLongitude() : mission.getLongitude();

        double distanceKm = haversineKm(lat, lng, hubLat, hubLng);
        int etaMinutes = (int) Math.round((distanceKm / VITESSE_MOYENNE_KMH) * 60.0);
        LocalDateTime etaAt = LocalDateTime.now().plusMinutes(etaMinutes);

        return TrackingDto.EtaResponse.builder()
                .missionId(missionId)
                .etaMinutes(etaMinutes)
                .etaAt(etaAt)
                .distanceRestanteKm(Math.round(distanceKm * 10.0) / 10.0)
                .statutCalcul("OK")
                .build();
    }

    /** S5 — enregistrement batch positions (app mobile chauffeur). */
    @Transactional
    public TrackingDto.PositionBatchResponse enregistrerPositions(
            TrackingDto.PositionBatchRequest request,
            UUID utilisateurId,
            String tenantId) {

        Mission mission = missionRepository.findByIdAndTenantId(request.getMissionId(), tenantId)
                .orElseThrow(() -> new RuntimeException("MISSION_INTROUVABLE"));

        if (!STATUTS_TRACKING_ACTIF.contains(mission.getStatut())) {
            throw new RuntimeException("MISSION_NON_TRACKABLE");
        }

        Chauffeur chauffeur = chauffeurRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new RuntimeException("CHAUFFEUR_INTROUVABLE"));

        if (mission.getChauffeur() == null
                || !mission.getChauffeur().getId().equals(chauffeur.getId())) {
            throw new RuntimeException("ACCES_REFUSE");
        }

        int count = 0;
        for (TrackingDto.PointWriteRequest point : request.getPositions()) {
            LocalDateTime recordedAt = point.getRecordedAt() != null
                    ? point.getRecordedAt()
                    : LocalDateTime.now();

            positionGpsRepository.save(PositionGps.builder()
                    .mission(mission)
                    .latitude(point.getLatitude())
                    .longitude(point.getLongitude())
                    .recordedAt(recordedAt)
                    .vitesseKmh(point.getVitesseKmh())
                    .precisionMetres(point.getPrecisionMetres())
                    .tenantId(tenantId)
                    .build());
            count++;
        }

        return TrackingDto.PositionBatchResponse.builder()
                .missionId(mission.getId())
                .enregistrees(count)
                .build();
    }

    /** Distance approximative en km (Haversine). */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }
}
