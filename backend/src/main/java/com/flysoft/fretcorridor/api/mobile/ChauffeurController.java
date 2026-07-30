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
    public ResponseEntity<?> enroler(
            @Valid @RequestBody ChauffeurDto.EnrolementRequest request,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);

            UUID agentUserId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);
            String role = jwtService.extraireRole(token);

            if (!"AGENT".equals(role)) {
                return ResponseEntity.status(403)
                        .body(new ErrorResponse("Vous n'avez pas l'autorisation d'enrôler un chauffeur."));
            }

            ChauffeurDto.ChauffeurResponse response = chauffeurService.enroler(request, agentUserId, tenantId);

            return ResponseEntity.status(201).body(response);

        } catch (RuntimeException e) {

            e.printStackTrace();

            String message;

            switch (e.getMessage()) {

                case "TELEPHONE_DEJA_UTILISE":
                    message = "Ce numéro de téléphone est déjà utilisé.";
                    break;

                case "AGENT_INTROUVABLE":
                    message = "Agent introuvable.";
                    break;

                case "ACCES_REFUSE":
                    message = "Vous n'avez pas les droits nécessaires.";
                    break;

                default:
                    message = "Impossible d'enrôler ce chauffeur. Veuillez réessayer.";
                    break;
            }

            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(message));
        }
    }

    public record ErrorResponse(String message) {
    }

    // ── GET /api/chauffeurs/me — Profil chauffeur connecté ───
    @GetMapping("/chauffeurs/me")
    public ResponseEntity<ChauffeurDto.ChauffeurResponse> getMonProfil(
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);

            UUID userId = jwtService.extraireUserId(token);
            String tenantId = jwtService.extraireTenantId(token);

            return ResponseEntity.ok(
                    chauffeurService.getProfilParUtilisateur(userId, tenantId));

        } catch (RuntimeException e) {

            if ("CHAUFFEUR_INTROUVABLE".equals(e.getMessage())
                    || "ACCES_REFUSE".equals(e.getMessage())) {

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
                            chauffeurId,
                            fichier,
                            typeDocument,
                            tenantId,
                            userId,
                            role));

        } catch (RuntimeException e) {

            if ("ACCES_REFUSE".equals(e.getMessage())
                    || "CHAUFFEUR_INTROUVABLE".equals(e.getMessage())) {

                return ResponseEntity.status(403).build();
            }

            if ("FICHIER_VIDE".equals(e.getMessage())
                    || "TYPE_DOCUMENT_INVALIDE".equals(e.getMessage())) {

                return ResponseEntity.badRequest().build();
            }

            throw e;
        }
    }
}

