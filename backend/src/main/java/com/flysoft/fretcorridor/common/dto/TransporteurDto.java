package com.flysoft.fretcorridor.common.dto;

import com.flysoft.fretcorridor.common.entity.Transporteur;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.UUID;

public class TransporteurDto {

    @Data
    public static class EnrolementRequest {
        @NotBlank private String nomEntreprise;
        @NotBlank private String nomResponsable;
        @NotBlank private String prenomResponsable;
        @NotBlank private String telephone;
        @NotBlank private String codePinInitial;
        private String numeroRegistreCommerce;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransporteurResponse {
        private UUID id;
        private String nomEntreprise;
        private String nomResponsable;
        private String prenomResponsable;
        private String telephone;
        private String kycNiveau;
        private String statutKyc;

        public static TransporteurResponse fromEntity(Transporteur t) {
            return TransporteurResponse.builder()
                    .id(t.getId())
                    .nomEntreprise(t.getNomEntreprise())
                    .nomResponsable(t.getNomResponsable())
                    .prenomResponsable(t.getPrenomResponsable())
                    .telephone(t.getUtilisateur().getTelephone())
                    .kycNiveau(t.getKycNiveau().name())
                    .statutKyc(t.getStatutKyc().name())
                    .build();
        }
    }
}
