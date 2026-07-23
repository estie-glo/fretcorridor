package com.flysoft.fretcorridor.api.mobile;

import com.flysoft.fretcorridor.common.dto.CamionDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.service.CamionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/camions")
@RequiredArgsConstructor
public class CamionController {

    private final CamionService camionService;
    private final JwtService jwtService;

    // ── POST /api/camions — Le transporteur ajoute un camion à sa flotte ──
    @PostMapping
    public ResponseEntity<?> ajouter(
            @Valid @RequestBody CamionDto.AjoutRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID utilisateurId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            String role = jwtService.extraireRole(token);

            if (!"TRANSPORTEUR".equals(role)) {
                return ResponseEntity.status(403).build();
            }

            var response = camionService.ajouter(request, utilisateurId, tenantId);
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── GET /api/camions — Mes camions (transporteur connecté) ──
    @GetMapping
    public ResponseEntity<?> getMesCamions(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID utilisateurId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            return ResponseEntity.ok(camionService.getMesCamions(utilisateurId, tenantId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
