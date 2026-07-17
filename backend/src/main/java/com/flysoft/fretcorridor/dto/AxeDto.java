package com.flysoft.fretcorridor.dto;

import com.flysoft.fretcorridor.entity.Axe;
import lombok.*;
import java.util.UUID;

public class AxeDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AxeResponse {
        private UUID id;
        private String nom;
        private String hubDepart;
        private String hubArrivee;
        private String etatActivation;
        private boolean zoneSensible;

        public static AxeResponse fromEntity(Axe axe) {
            return AxeResponse.builder()
                    .id(axe.getId())
                    .nom(axe.getNom())
                    .hubDepart(axe.getHubDepart().getNom())
                    .hubArrivee(axe.getHubArrivee().getNom())
                    .etatActivation(axe.getEtatActivation().name())
                    .zoneSensible(axe.isZoneSensible())
                    .build();
        }
    }
}
