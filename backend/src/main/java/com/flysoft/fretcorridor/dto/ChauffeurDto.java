package com.flysoft.fretcorridor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class ChauffeurDto {

    // ── Enrôlement d'un nouveau chauffeur (par l'agent) ──────
    @Data
    public static class EnrolementRequest {

        @NotBlank(message = "Le nom est obligatoire")
        private String nom;

        @NotBlank(message = "Le prénom est obligatoire")
        private String prenom;

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^\\+?[0-9]{9,15}$")
        private String telephone;

        @NotBlank(message = "Le code PIN initial est obligatoire")
        @Pattern(regexp = "^[0-9]{4,6}$")
        private String codePinInitial;

        private String numeroCNI;
    }

    // ── Réponse profil chauffeur ──────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChauffeurResponse {
        private UUID id;
        private String nom;
        private String prenom;
        private String telephone;
        private String tenantId;
        private String kycNiveau;
        private String statutKyc;
        private String urlPhotoCNI;
        private String urlPhotoPermis;
        private LocalDateTime dateEnrolement;
        private LocalDateTime dateValidationKyc;
        private String agentNom;
        private String badgeKyc; // ex: "KYC Niveau 1 validé ✅"
    }

    // ── Validation KYC par l'agent ────────────────────────────
    @Data
    public static class ValidationKycRequest {
        private boolean approuve;
        private String commentaire;
        private String nouveauNiveau; // "NIVEAU_1" ou "NIVEAU_2"
    }

    // ── Réponse upload document KYC ───────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadDocumentResponse {
        private String urlDocument;
        private String typeDocument; // "CNI" ou "PERMIS"
        private String message;
    }
}
