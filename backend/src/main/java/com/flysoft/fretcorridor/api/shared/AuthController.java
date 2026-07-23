package com.flysoft.fretcorridor.api.shared;

import com.flysoft.fretcorridor.common.dto.AuthDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    // ── POST /api/auth/login ──────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        try {
            AuthDto.LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            String msg = e.getMessage();

            if (msg.equals("UTILISATEUR_INTROUVABLE")) {
                return ResponseEntity.status(401).body(
                    AuthDto.ErreurResponse.builder()
                        .code("UTILISATEUR_INTROUVABLE")
                        .message("Numéro non reconnu. Contactez votre agent.")
                        .build()
                );
            }

            if (msg.startsWith("PIN_INCORRECT:")) {
                int restantes = Integer.parseInt(msg.split(":")[1]);
                return ResponseEntity.status(401).body(
                    AuthDto.ErreurResponse.builder()
                        .code("PIN_INCORRECT")
                        .message("PIN incorrect.")
                        .tentativesRestantes(restantes)
                        .build()
                );
            }

            if (msg.equals("COMPTE_BLOQUE")) {
                return ResponseEntity.status(403).body(
                    AuthDto.ErreurResponse.builder()
                        .code("COMPTE_BLOQUE")
                        .message("Compte bloqué après 3 tentatives. Contactez votre agent.")
                        .build()
                );
            }

            if (msg.equals("COMPTE_DESACTIVE")) {
                return ResponseEntity.status(403).body(
                    AuthDto.ErreurResponse.builder()
                        .code("COMPTE_DESACTIVE")
                        .message("Compte désactivé. Contactez votre agent.")
                        .build()
                );
            }

            if (msg.equals("TROP_DE_TENTATIVES")) {
                return ResponseEntity.status(429).body(
                    AuthDto.ErreurResponse.builder()
                        .code("TROP_DE_TENTATIVES")
                        .message("Trop de tentatives. Réessayez dans une minute.")
                        .build()
                );
            }

            return ResponseEntity.status(500).body(
                AuthDto.ErreurResponse.builder()
                    .code("ERREUR_INTERNE")
                    .message("Une erreur est survenue.")
                    .build()
            );
        }
    }

    // ── POST /api/auth/refresh ────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody AuthDto.RefreshRequest request) {
        try {
            AuthDto.LoginResponse response = authService.refresh(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(
                AuthDto.ErreurResponse.builder()
                    .code(e.getMessage())
                    .message("Session expirée. Veuillez vous reconnecter.")
                    .build()
            );
        }
    }

    // ── POST /api/auth/logout ─────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) AuthDto.FcmTokenRequest request) {
        try {
            String token = authHeader.substring(7); // Enlever "Bearer "
            UUID userId  = jwtService.extraireUserId(token);
            authService.logout(userId, request != null ? request.getFcmToken() : null);
            return ResponseEntity.ok().body("{\"message\": \"Déconnexion réussie\"}");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                AuthDto.ErreurResponse.builder()
                    .code("ERREUR_LOGOUT")
                    .message("Erreur lors de la déconnexion.")
                    .build()
            );
        }
    }

    // ── PUT /api/auth/fcm-token ───────────────────────────────
    @PutMapping("/fcm-token")
    public ResponseEntity<?> mettreAJourFcmToken(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AuthDto.FcmTokenRequest request) {
        String token = authHeader.substring(7);
        UUID userId  = jwtService.extraireUserId(token);
        authService.mettreAJourFcmToken(userId, request.getFcmToken());
        return ResponseEntity.ok().body("{\"message\": \"FCM token mis à jour\"}");
    }
}
