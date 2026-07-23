package com.flysoft.fretcorridor.api.mobile;

import com.flysoft.fretcorridor.common.dto.TrackingDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * S5 mobile — écriture des positions GPS (batch offline sync).
 */
@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final TrackingService trackingService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> enregistrer(
            @Valid @RequestBody TrackingDto.PositionBatchRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID userId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            String role = jwtService.extraireRole(token);

            if (!"CHAUFFEUR".equals(role)) {
                return ResponseEntity.status(403).build();
            }

            return ResponseEntity.status(201).body(
                    trackingService.enregistrerPositions(request, userId, tenantId));
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "MISSION_INTROUVABLE", "CHAUFFEUR_INTROUVABLE" -> ResponseEntity.notFound().build();
                case "ACCES_REFUSE", "MISSION_NON_TRACKABLE" -> ResponseEntity.status(403).build();
                default -> ResponseEntity.badRequest().body(e.getMessage());
            };
        }
    }
}
