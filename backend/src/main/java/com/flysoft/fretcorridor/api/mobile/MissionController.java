package com.flysoft.fretcorridor.api.mobile;

import com.flysoft.fretcorridor.common.dto.MissionDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.service.MissionService;
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

    // ── GET /api/missions/mes-declarations ────────────────────
    @GetMapping("/mes-declarations")
    public ResponseEntity<?> getMesDeclarations(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID utilisateurId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            return ResponseEntity.ok(missionService.getMesDeclarations(utilisateurId, tenantId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── GET /api/missions/{id} — détail d'une déclaration ─────
    @GetMapping("/mes-declarations/{id}")
    public ResponseEntity<?> getDetail(
            @PathVariable UUID id, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID utilisateurId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            return ResponseEntity.ok(missionService.getDetail(id, utilisateurId, tenantId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── PUT /api/missions/{id} — modifier une déclaration ─────
    @PutMapping("/mes-declarations/{id}")
    public ResponseEntity<?> modifier(
            @PathVariable UUID id,
            @RequestBody MissionDto.UpdateRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID utilisateurId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            return ResponseEntity.ok(missionService.modifier(id, request, utilisateurId, tenantId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── DELETE /api/missions/{id} — supprimer une déclaration ─
    @DeleteMapping("/mes-declarations/{id}")
    public ResponseEntity<?> supprimer(
            @PathVariable UUID id, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID utilisateurId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            missionService.supprimer(id, utilisateurId, tenantId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
