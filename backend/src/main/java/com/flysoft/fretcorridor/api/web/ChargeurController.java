package com.flysoft.fretcorridor.api.web;

import com.flysoft.fretcorridor.common.dto.ChargeurDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.service.ChargeurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChargeurController {

    private final ChargeurService chargeurService;
    private final JwtService jwtService;

    // ── POST /api/chargeurs — Enrôler un chargeur (agent) ──
    @PostMapping("/chargeurs")
    public ResponseEntity<?> enroler(
            @Valid @RequestBody ChargeurDto.EnrolementRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID agentUserId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            String role = jwtService.extraireRole(token);

            if (!"AGENT".equals(role)) {
                return ResponseEntity.status(403).build();
            }

            var response = chargeurService.enroler(request, agentUserId, tenantId);
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── GET /api/chargeurs — Liste des chargeurs du tenant ──
    @GetMapping("/chargeurs")
    public ResponseEntity<List<ChargeurDto.ChargeurResponse>> getTous(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        return ResponseEntity.ok(chargeurService.getTousLesChargeurs(tenantId));
    }
}
