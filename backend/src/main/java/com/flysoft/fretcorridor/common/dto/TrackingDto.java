package com.flysoft.fretcorridor.common.dto;

import com.flysoft.fretcorridor.common.entity.PositionGps;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TrackingDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PointResponse {
        private UUID id;
        private Double latitude;
        private Double longitude;
        private LocalDateTime recordedAt;
        private Double vitesseKmh;
        private Double precisionMetres;

        public static PointResponse fromEntity(PositionGps p) {
            return PointResponse.builder()
                    .id(p.getId())
                    .latitude(p.getLatitude())
                    .longitude(p.getLongitude())
                    .recordedAt(p.getRecordedAt())
                    .vitesseKmh(p.getVitesseKmh())
                    .precisionMetres(p.getPrecisionMetres())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrackingResponse {
        private UUID missionId;
        private String statutMission;
        private PointResponse lastPosition;
        private List<PointResponse> points;
        private boolean zoneSensible;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtaResponse {
        private UUID missionId;
        private Integer etaMinutes;
        private LocalDateTime etaAt;
        private Double distanceRestanteKm;
        private String statutCalcul; // OK | INSUFFISANT | MISSION_TERMINEE
    }

    /** S5 mobile — écriture batch de positions GPS. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionBatchRequest {
        @NotNull(message = "missionId obligatoire")
        private UUID missionId;

        @NotEmpty(message = "positions obligatoires")
        @Valid
        private List<PointWriteRequest> positions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointWriteRequest {
        @NotNull private Double latitude;
        @NotNull private Double longitude;
        private LocalDateTime recordedAt;
        private Double vitesseKmh;
        private Double precisionMetres;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PositionBatchResponse {
        private UUID missionId;
        private int enregistrees;
    }
}
