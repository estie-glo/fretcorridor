package com.flysoft.fretcorridor.common.dto;

import com.flysoft.fretcorridor.common.entity.Mission;
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

        // EF-MKT-01 : optionnel — si absent, "disponible immédiatement"
        private LocalDateTime disponibleDe;
    }

    // ── Modification d'une déclaration existante (chauffeur, mobile) ──
    @Data
    public static class UpdateRequest {
        private String typeCamion;       // optionnel : ne change que si fourni
        private Double capaciteTonnes;   // optionnel
        private LocalDateTime disponibleDe;   // optionnel
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MissionResponse {
        private UUID id;
        private UUID axeId;
        private String axeNom;
        private UUID chauffeurId;
        private String chauffeurNom;
        private Double latitude;
        private Double longitude;
        private String typeCamion;
        private Double capaciteTonnes;
        private String statut;
        private boolean zoneSensible;
        private LocalDateTime dateDeclaration;
        private LocalDateTime disponibleDe;

        public static MissionResponse fromEntity(Mission m) {
            String chauffeurNom = null;
            UUID chauffeurId = null;
            if (m.getChauffeur() != null) {
                chauffeurId = m.getChauffeur().getId();
                chauffeurNom = m.getChauffeur().getNom() + " " + m.getChauffeur().getPrenom();
            }
            return MissionResponse.builder()
                    .id(m.getId())
                    .axeId(m.getAxe() != null ? m.getAxe().getId() : null)
                    .axeNom(m.getAxe() != null ? m.getAxe().getNom() : null)
                    .chauffeurId(chauffeurId)
                    .chauffeurNom(chauffeurNom)
                    .latitude(m.getLatitude())
                    .longitude(m.getLongitude())
                    .typeCamion(m.getTypeCamion())
                    .capaciteTonnes(m.getCapaciteTonnes())
                    .statut(m.getStatut().name())
                    .zoneSensible(m.getAxe() != null && m.getAxe().isZoneSensible())
                    .dateDeclaration(m.getDateDeclaration())
                    .disponibleDe(m.getDisponibleDe())
                    .build();
        }
    }
}
