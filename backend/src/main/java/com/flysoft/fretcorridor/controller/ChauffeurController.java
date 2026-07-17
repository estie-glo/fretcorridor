package com.flysoft.fretcorridor.controller;

import com.flysoft.fretcorridor.dto.ChauffeurDto;
import com.flysoft.fretcorridor.security.JwtService;
import com.flysoft.fretcorridor.service.ChauffeurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChauffeurController {

    private final ChauffeurService chauffeurService;
    private final JwtService jwtService;

    // ── POST /api/chauffeurs — Enrôler un chauffeur ───────────
    // Accessible uniquement aux agents
    @PostMapping("/chauffeurs")
    public ResponseEntity<ChauffeurDto.ChauffeurResponse> enroler(
            @Valid @RequestBody ChauffeurDto.EnrolementRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token    = authHeader.substring(7);
            UUID agentUserId = jwtService.extraireUserId(token);
            String tenantId  = jwtService.extraireTenantId(token);
            String role      = jwtService.extraireRole(token);

            if (!"AGENT".equals(role)) {
                return ResponseEntity.status(403).build();
            }

            ChauffeurDto.ChauffeurResponse response =
                chauffeurService.enroler(request, agentUserId, tenantId);
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── GET /api/chauffeurs/{id} — Profil d'un chauffeur ─────
    @GetMapping("/chauffeurs/{id}")
    public ResponseEntity<ChauffeurDto.ChauffeurResponse> getProfil(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        String token    = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        return ResponseEntity.ok(chauffeurService.getProfil(id, tenantId));
    }

    // ── GET /api/chauffeurs — Mes chauffeurs (agent) ──────────
    @GetMapping("/chauffeurs")
    public ResponseEntity<List<ChauffeurDto.ChauffeurResponse>> getMesChauffeurs(
            @RequestHeader("Authorization") String authHeader) {
        String token     = authHeader.substring(7);
        UUID agentUserId = jwtService.extraireUserId(token);
        String tenantId  = jwtService.extraireTenantId(token);
        String role      = jwtService.extraireRole(token);

        if (!"AGENT".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
            chauffeurService.getMesChauffeurs(agentUserId, tenantId));
    }

    // ── GET /api/admin/kyc/en-attente — KYC en attente ───────
    @GetMapping("/admin/kyc/en-attente")
    public ResponseEntity<List<ChauffeurDto.ChauffeurResponse>> getKycEnAttente(
            @RequestHeader("Authorization") String authHeader) {
        String token    = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        String role     = jwtService.extraireRole(token);

        if (!"AGENT".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(chauffeurService.getKycEnAttente(tenantId));
    }

    // ── PUT /api/admin/kyc/{id}/valider — Valider un KYC ─────
    @PutMapping("/admin/kyc/{id}/valider")
    public ResponseEntity<ChauffeurDto.ChauffeurResponse> validerKyc(
            @PathVariable UUID id,
            @RequestBody ChauffeurDto.ValidationKycRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token    = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        String role     = jwtService.extraireRole(token);

        if (!"AGENT".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
            chauffeurService.validerKyc(id, request, tenantId));
    }

    // ── POST /api/kyc/documents — Upload document KYC ────────
    @PostMapping("/kyc/documents")
    public ResponseEntity<ChauffeurDto.UploadDocumentResponse> uploaderDocument(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam("typeDocument") String typeDocument,
            @RequestParam("chauffeurId") UUID chauffeurId,
            @RequestHeader("Authorization") String authHeader) {
        String token    = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);

        return ResponseEntity.ok(
            chauffeurService.uploaderDocument(
                chauffeurId, fichier, typeDocument, tenantId));
    }
}
