package com.flysoft.fretcorridor.api.shared;

import com.flysoft.fretcorridor.common.dto.AxeDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.service.AxeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AxeController {

    private final AxeService axeService;
    private final JwtService jwtService;

    // ── GET /api/axes — Axes disponibles pour le tenant courant ──
    @GetMapping("/axes")
    public ResponseEntity<List<AxeDto.AxeResponse>> getAxes(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        return ResponseEntity.ok(axeService.getAxesDisponibles(tenantId));
    }

    // ── GET /api/axes/{id}/statut — État d'un axe ─────────────
    @GetMapping("/axes/{id}/statut")
    public ResponseEntity<AxeDto.AxeResponse> getStatut(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String tenantId = jwtService.extraireTenantId(token);
            return ResponseEntity.ok(axeService.getStatut(id, tenantId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
