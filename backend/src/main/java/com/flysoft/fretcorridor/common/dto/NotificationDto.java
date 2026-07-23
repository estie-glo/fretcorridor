package com.flysoft.fretcorridor.common.dto;

import com.flysoft.fretcorridor.common.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

public class NotificationDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationResponse {
        private UUID id;
        private String canal;
        private String type;
        private String titreFr;
        private String titreEn;
        private String corpsFr;
        private String corpsEn;
        private String ressourceType;
        private UUID ressourceId;
        private boolean lue;
        private String statutEnvoi;
        private Instant dateCreation;

        public static NotificationResponse fromEntity(Notification n) {
            return NotificationResponse.builder()
                    .id(n.getId())
                    .canal(n.getCanal().name())
                    .type(n.getType())
                    .titreFr(n.getTitreFr())
                    .titreEn(n.getTitreEn())
                    .corpsFr(n.getCorpsFr())
                    .corpsEn(n.getCorpsEn())
                    .ressourceType(n.getRessourceType())
                    .ressourceId(n.getRessourceId())
                    .lue(n.isLue())
                    .statutEnvoi(n.getStatutEnvoi().name())
                    .dateCreation(n.getDateCreation())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendRequest {
        @NotNull
        private UUID destinataireId;

        @NotBlank
        private String type;

        @NotBlank
        private String titreFr;

        @NotBlank
        private String titreEn;

        @NotBlank
        private String corpsFr;

        @NotBlank
        private String corpsEn;

        /** Canal préféré ; repli automatique vers IN_APP (EF-NOT-03). */
        private String canalPrefere;

        private String ressourceType;
        private UUID ressourceId;
    }
}
