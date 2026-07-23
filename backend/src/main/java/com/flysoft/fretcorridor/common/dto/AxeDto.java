package com.flysoft.fretcorridor.common.dto;

import com.flysoft.fretcorridor.common.entity.Axe;
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
        private UUID hubDepartId;
        private UUID hubArriveeId;
        private Double hubDepartLatitude;
        private Double hubDepartLongitude;
        private Double hubArriveeLatitude;
        private Double hubArriveeLongitude;
        /** Compat carte / mobile : dérivé des 3 flags GEO. */
        private String etatActivation;
        private boolean visibiliteActive;
        private boolean matchingActif;
        private boolean financementActif;
        private boolean zoneSensible;

        public static AxeResponse fromEntity(Axe axe) {
            return AxeResponse.builder()
                    .id(axe.getId())
                    .nom(axe.getNom())
                    .hubDepart(axe.getHubDepart().getNom())
                    .hubArrivee(axe.getHubArrivee().getNom())
                    .hubDepartId(axe.getHubDepart().getId())
                    .hubArriveeId(axe.getHubArrivee().getId())
                    .hubDepartLatitude(axe.getHubDepart().getLatitude())
                    .hubDepartLongitude(axe.getHubDepart().getLongitude())
                    .hubArriveeLatitude(axe.getHubArrivee().getLatitude())
                    .hubArriveeLongitude(axe.getHubArrivee().getLongitude())
                    .etatActivation(axe.deriveEtatActivation())
                    .visibiliteActive(axe.isVisibiliteActive())
                    .matchingActif(axe.isMatchingActif())
                    .financementActif(axe.isFinancementActif())
                    .zoneSensible(axe.isZoneSensible())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateActivationRequest {
        private Boolean visibiliteActive;
        private Boolean matchingActif;
        private Boolean financementActif;

        /**
         * Compat : encore accepter l'ancien enum ACTIF|VERROUILLE|INACTIF.
         * Si présent, il prime sur les booléens partiels.
         */
        private String etatActivation;
    }
}
