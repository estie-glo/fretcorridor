package com.flysoft.fretcorridor.dto;

import com.flysoft.fretcorridor.entity.Mission;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class MissionDto {

    @Data
    public static class DeclareVideRequest {
        @NotNull(message = "axeId obligatoire")
        private UUID axeId;

        @NotNull(message = "latitude obligatoire")
        private Double latitude;

        @NotNull(message = "longitude obligatoire")
        private Double longitude;

        @NotBlank(message = "typeCamion obligatoire")
        private String typeCamion;

        @NotNull(message = "capaciteTonnes obligatoire")
        @Positive(message = "capaciteTonnes doit être positive")
        private Double capaciteTonnes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MissionResponse {
        private UUID id;
        private String axeNom;
        private Double latitude;
        private Double longitude;
        private String typeCamion;
        private Double capaciteTonnes;
        private String statut;
        private LocalDateTime dateDeclaration;

        public static MissionResponse fromEntity(Mission m) {
            return MissionResponse.builder()
                    .id(m.getId())
                    .axeNom(m.getAxe().getNom())
                    .latitude(m.getLatitude())
                    .longitude(m.getLongitude())
                    .typeCamion(m.getTypeCamion())
                    .capaciteTonnes(m.getCapaciteTonnes())
                    .statut(m.getStatut().name())
                    .dateDeclaration(m.getDateDeclaration())
                    .build();
        }
    }
}
