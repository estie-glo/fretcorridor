package com.flysoft.fretcorridor.common.dto;

import com.flysoft.fretcorridor.common.entity.Hub;
import lombok.*;
import java.util.UUID;

public class HubDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HubResponse {
        private UUID id;
        private String nom;
        private String pays;
        private Double latitude;
        private Double longitude;

        public static HubResponse fromEntity(Hub hub) {
            return HubResponse.builder()
                    .id(hub.getId())
                    .nom(hub.getNom())
                    .pays(hub.getPays())
                    .latitude(hub.getLatitude())
                    .longitude(hub.getLongitude())
                    .build();
        }
    }
}
