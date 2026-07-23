package com.flysoft.fretcorridor.common.dto;

import com.flysoft.fretcorridor.common.entity.Camion;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

public class CamionDto {

    @Data
    public static class AjoutRequest {
        @NotBlank private String immatriculation;
        @NotBlank private String type; // SEMI_REMORQUE, PORTEUR, CITERNE, PLATEAU
        @NotNull @Positive private Double capaciteTonnes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CamionResponse {
        private UUID id;
        private String immatriculation;
        private String type;
        private Double capaciteTonnes;
        private String statut;

        public static CamionResponse fromEntity(Camion c) {
            return CamionResponse.builder()
                    .id(c.getId())
                    .immatriculation(c.getImmatriculation())
                    .type(c.getType().name())
                    .capaciteTonnes(c.getCapaciteTonnes())
                    .statut(c.getStatut().name())
                    .build();
        }
    }
}
