package com.flysoft.fretcorridor.api.web;

import com.flysoft.fretcorridor.common.dto.MissionDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.security.RoleChecks;
import com.flysoft.fretcorridor.common.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * OPS — supervision des missions pour le portail bureau / back-office (S6 cycle A→Z).
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionBureauController {

    private final MissionService missionService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<MissionDto.MissionResponse>> lister(
            @RequestParam(required = false) UUID axeId,
            @RequestParam(required = false) String statut,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        String role = jwtService.extraireRole(token);

        if (!RoleChecks.isBureauOrBackOffice(role)) {
            return ResponseEntity.status(403).build();
        }

        try {
            return ResponseEntity.ok(missionService.listerBureau(tenantId, axeId, statut));
        } catch (RuntimeException e) {
            if ("STATUT_INVALIDE".equals(e.getMessage())) {
                return ResponseEntity.badRequest().build();
            }
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MissionDto.MissionResponse> detail(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        String role = jwtService.extraireRole(token);

        if (!RoleChecks.isBureauOrBackOffice(role)) {
            return ResponseEntity.status(403).build();
        }

        try {
            return ResponseEntity.ok(missionService.getMissionBureau(id, tenantId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/accepter")
    public ResponseEntity<?> accepter(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        return transition(id, authHeader, missionService::accepter);
    }

    @PostMapping("/{id}/demarrer")
    public ResponseEntity<?> demarrer(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        return transition(id, authHeader, missionService::demarrer);
    }

    @PostMapping("/{id}/terminer")
    public ResponseEntity<?> terminer(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        return transition(id, authHeader, missionService::terminer);
    }

    @PostMapping("/{id}/annuler")
    public ResponseEntity<?> annuler(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        return transition(id, authHeader, missionService::annuler);
    }

    @FunctionalInterface
    private interface Transition {
        MissionDto.MissionResponse apply(UUID id, String tenantId, UUID acteurId, String role);
    }

    private ResponseEntity<?> transition(UUID id, String authHeader, Transition action) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        String role = jwtService.extraireRole(token);
        UUID acteurId = jwtService.extraireUserId(token);

        if (!RoleChecks.canTransitionMission(role)) {
            return ResponseEntity.status(403).build();
        }

        try {
            return ResponseEntity.ok(action.apply(id, tenantId, acteurId, role));
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "MISSION_INTROUVABLE" -> ResponseEntity.notFound().build();
                case "TRANSITION_INVALIDE", "AXE_VERROUILLE" ->
                        ResponseEntity.badRequest().body(e.getMessage());
                default -> ResponseEntity.badRequest().body(e.getMessage());
            };
        }
    }
}
