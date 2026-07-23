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
 * MKT S4 — offres camion vide pour le portail chargeur (et lecture bureau).
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionChargeurController {

    private final MissionService missionService;
    private final JwtService jwtService;

    @GetMapping("/offres")
    public ResponseEntity<List<MissionDto.MissionResponse>> listerOffres(
            @RequestParam(required = false) UUID axeId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        String role = jwtService.extraireRole(token);

        if (!RoleChecks.canReadOffres(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(missionService.listerOffres(tenantId, axeId));
    }
}
