package com.flysoft.fretcorridor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

public class AuthDto {

    // ── Requête de login ──────────────────────────────────────
    @Data
    public static class LoginRequest {

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Format téléphone invalide")
        private String telephone;

        @NotBlank(message = "Le code PIN est obligatoire")
        @Pattern(regexp = "^[0-9]{4,6}$", message = "Le PIN doit contenir 4 à 6 chiffres")
        private String codePin;
    }

    // ── Réponse login réussie ─────────────────────────────────
    @Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private String role;
        private String tenantId;
        private ConfigTenant configTenant;
    }

    // ── Configuration du tenant renvoyée au mobile ────────────
    @Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ConfigTenant {
        private String tenantId;
        private String nomBureau;      // ex: "BGFT Cameroun"
        private String langue;         // "fr" ou "fr-ar"
        private String devise;         // "FCFA"
        private String[] axesDisponibles; // ex: ["Douala-NDjamena", "Epine-Nord"]
    }

    // ── Requête refresh token ─────────────────────────────────
    @Data
    public static class RefreshRequest {
        @NotBlank(message = "Le refresh token est obligatoire")
        private String refreshToken;
    }

    // ── Requête mise à jour FCM token ─────────────────────────
    @Data
    public static class FcmTokenRequest {
        @NotBlank(message = "Le FCM token est obligatoire")
        private String fcmToken;
    }

    // ── Réponse d'erreur standardisée ────────────────────────
    @Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ErreurResponse {
        private String code;
        private String message;
        private int tentativesRestantes;
    }
}
