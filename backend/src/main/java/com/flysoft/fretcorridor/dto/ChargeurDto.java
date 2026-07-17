package com.flysoft.fretcorridor.dto;

import com.flysoft.fretcorridor.entity.Chargeur;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.UUID;

public class ChargeurDto {

    @Data
    public static class EnrolementRequest {
        @NotBlank private String nom;
        @NotBlank private String prenom;
        @NotBlank private String telephone;
        @NotBlank private String codePinInitial;
        private String entreprise;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChargeurResponse {
        private UUID id;
        private String nom;
        private String prenom;
        private String entreprise;
        private String telephone;
        private String kycNiveau;
        private String statutKyc;

        public static ChargeurResponse fromEntity(Chargeur c) {
            return ChargeurResponse.builder()
                    .id(c.getId())
                    .nom(c.getNom())
                    .prenom(c.getPrenom())
                    .entreprise(c.getEntreprise())
                    .telephone(c.getUtilisateur().getTelephone())
                    .kycNiveau(c.getKycNiveau().name())
                    .statutKyc(c.getStatutKyc().name())
                    .build();
        }
    }
}
