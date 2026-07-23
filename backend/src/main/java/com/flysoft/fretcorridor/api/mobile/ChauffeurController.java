package com.flysoft.fretcorridor.api.mobile;

import com.flysoft.fretcorridor.common.dto.ChauffeurDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.service.ChauffeurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints chauffeur côté app mobile (agent / chauffeur).
 * Enrôlement, liste des chauffeurs de l'agent, upload KYC.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChauffeurController {

    private final ChauffeurService chauffeurService;
    private final JwtService jwtService;

    // ── POST /api/chauffeurs — Enrôler un chauffeur ───────────
    @PostMapping("/chauffeurs")
    public ResponseEntity<ChauffeurDto.ChauffeurResponse> enroler(
            @Valid @RequestBody ChauffeurDto.EnrolementRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID agentUserId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            String role = jwtService.extraireRole(token);

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

    // ── GET /api/chauffeurs/me — Profil chauffeur connecté ───
    @GetMapping("/chauffeurs/me")
    public ResponseEntity<ChauffeurDto.ChauffeurResponse> getMonProfil(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID userId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            return ResponseEntity.ok(chauffeurService.getProfilParUtilisateur(userId, tenantId));
        } catch (RuntimeException e) {
            if ("CHAUFFEUR_INTROUVABLE".equals(e.getMessage()) || "ACCES_REFUSE".equals(e.getMessage())) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    // ── GET /api/chauffeurs — Mes chauffeurs (agent) ──────────
    @GetMapping("/chauffeurs")
    public ResponseEntity<List<ChauffeurDto.ChauffeurResponse>> getMesChauffeurs(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UUID agentUserId = jwtService.extraireUserId(token);
        String tenantId = jwtService.extraireTenantId(token);
        String role = jwtService.extraireRole(token);

        if (!"AGENT".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
            chauffeurService.getMesChauffeurs(agentUserId, tenantId));
    }

    // ── POST /api/kyc/documents — Upload document KYC ────────
    @PostMapping("/kyc/documents")
    public ResponseEntity<ChauffeurDto.UploadDocumentResponse> uploaderDocument(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam("typeDocument") String typeDocument,
            @RequestParam("chauffeurId") UUID chauffeurId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID userId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            String role = jwtService.extraireRole(token);

            return ResponseEntity.ok(
                chauffeurService.uploaderDocument(
                    chauffeurId, fichier, typeDocument, tenantId, userId, role));
        } catch (RuntimeException e) {
            if ("ACCES_REFUSE".equals(e.getMessage()) || "CHAUFFEUR_INTROUVABLE".equals(e.getMessage())) {
                return ResponseEntity.status(403).build();
            }
            if ("FICHIER_VIDE".equals(e.getMessage()) || "TYPE_DOCUMENT_INVALIDE".equals(e.getMessage())) {
                return ResponseEntity.badRequest().build();
            }
            throw e;
        }
    }
}
