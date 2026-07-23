package com.flysoft.fretcorridor.api.mobile;

import com.flysoft.fretcorridor.common.dto.TransporteurDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.service.TransporteurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransporteurController {

    private final TransporteurService transporteurService;
    private final JwtService jwtService;

    // ── POST /api/transporteurs — Enrôler un transporteur (agent) ──
    @PostMapping("/transporteurs")
    public ResponseEntity<?> enroler(
            @Valid @RequestBody TransporteurDto.EnrolementRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID agentUserId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            String role = jwtService.extraireRole(token);

            if (!"AGENT".equals(role)) {
                return ResponseEntity.status(403).build();
            }

            var response = transporteurService.enroler(request, agentUserId, tenantId);
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── GET /api/transporteurs — Liste des transporteurs du tenant ──
    @GetMapping("/transporteurs")
    public ResponseEntity<List<TransporteurDto.TransporteurResponse>> getTous(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        return ResponseEntity.ok(transporteurService.getTousLesTransporteurs(tenantId));
    }
}
