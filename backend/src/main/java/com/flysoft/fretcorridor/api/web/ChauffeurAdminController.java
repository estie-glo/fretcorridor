package com.flysoft.fretcorridor.api.web;

import com.flysoft.fretcorridor.common.dto.ChauffeurDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.security.RoleChecks;
import com.flysoft.fretcorridor.common.service.ChauffeurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * Portail web — consultation chauffeurs (admin / bureau).
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChauffeurAdminController {

    private final ChauffeurService chauffeurService;
    private final JwtService jwtService;

    // ── GET /api/admin/chauffeurs — liste tenant (back-office) ─
    @GetMapping("/admin/chauffeurs")
    public ResponseEntity<List<ChauffeurDto.ChauffeurResponse>> lister(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        String role = jwtService.extraireRole(token);

        if (!RoleChecks.isBackOffice(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(chauffeurService.getChauffeursTenant(tenantId));
    }

    // ── GET /api/chauffeurs/{id} — Profil d'un chauffeur ─────
    @GetMapping("/chauffeurs/{id}")
    public ResponseEntity<ChauffeurDto.ChauffeurResponse> getProfil(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String tenantId = jwtService.extraireTenantId(token);
            return ResponseEntity.ok(chauffeurService.getProfil(id, tenantId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
