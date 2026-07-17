package com.flysoft.fretcorridor.controller;

import com.flysoft.fretcorridor.dto.MissionDto;
import com.flysoft.fretcorridor.security.JwtService;
import com.flysoft.fretcorridor.service.MissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;
    private final JwtService jwtService;

    // ── POST /api/missions/declare-vide ───────────────────────
    @PostMapping("/declare-vide")
    public ResponseEntity<?> declarerVide(
            @Valid @RequestBody MissionDto.DeclareVideRequest request,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().body("Header X-Idempotency-Key obligatoire");
        }

        try {
            String token = authHeader.substring(7);
            UUID utilisateurId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);

            MissionDto.MissionResponse response = missionService.declarerVide(
                    request, idempotencyKey, utilisateurId, tenantId);

            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── GET /api/missions/matchs — stub Sprint 3 ──────────────
    @GetMapping("/matchs")
    public ResponseEntity<List<MissionDto.MissionResponse>> getMatchs(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        return ResponseEntity.ok(missionService.getMatchsDisponibles(tenantId));
    }
}
