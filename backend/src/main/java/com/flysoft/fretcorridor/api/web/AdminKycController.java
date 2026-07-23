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
 * Back-office web — modération KYC (portail admin / bureau).
 */
@RestController
@RequestMapping("/api/admin/kyc")
@RequiredArgsConstructor
public class AdminKycController {

    private final ChauffeurService chauffeurService;
    private final JwtService jwtService;

    @GetMapping("/en-attente")
    public ResponseEntity<List<ChauffeurDto.ChauffeurResponse>> getKycEnAttente(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        String role = jwtService.extraireRole(token);

        if (!RoleChecks.isBackOffice(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(chauffeurService.getKycEnAttente(tenantId));
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<ChauffeurDto.ChauffeurResponse> validerKyc(
            @PathVariable UUID id,
            @RequestBody(required = false) ChauffeurDto.ValidationKycRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        String role = jwtService.extraireRole(token);

        if (!RoleChecks.isBackOffice(role)) {
            return ResponseEntity.status(403).build();
        }

        if (request == null) {
            request = new ChauffeurDto.ValidationKycRequest();
        }
        if (request.getApprouve() == null) {
            request.setApprouve(true);
        }
        if (request.getNouveauNiveau() == null || request.getNouveauNiveau().isBlank()) {
            request.setNouveauNiveau("NIVEAU_1");
        }

        UUID acteurId = jwtService.extraireUserId(token);
        try {
            return ResponseEntity.ok(
                chauffeurService.validerKyc(id, request, tenantId, acteurId, role));
        } catch (RuntimeException e) {
            if ("DOCS_MANQUANTS".equals(e.getMessage())) {
                return ResponseEntity.badRequest().build();
            }
            if ("CHAUFFEUR_INTROUVABLE".equals(e.getMessage()) || "ACCES_REFUSE".equals(e.getMessage())) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }
}
