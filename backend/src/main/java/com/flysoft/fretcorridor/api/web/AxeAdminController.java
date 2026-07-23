package com.flysoft.fretcorridor.api.web;

import com.flysoft.fretcorridor.common.dto.AxeDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.security.RoleChecks;
import com.flysoft.fretcorridor.common.service.AxeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * GEO / OPS — activation progressive d'un axe (EF-GEO-03, 3 flags).
 */
@RestController
@RequestMapping("/api/axes")
@RequiredArgsConstructor
public class AxeAdminController {

    private final AxeService axeService;
    private final JwtService jwtService;

    @PatchMapping("/{id}/activation")
    public ResponseEntity<AxeDto.AxeResponse> updateActivation(
            @PathVariable UUID id,
            @Valid @RequestBody AxeDto.UpdateActivationRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        String role = jwtService.extraireRole(token);
        UUID acteurId = jwtService.extraireUserId(token);

        if (!RoleChecks.isBackOffice(role)) {
            return ResponseEntity.status(403).build();
        }

        try {
            return ResponseEntity.ok(
                    axeService.updateActivation(id, request, tenantId, acteurId, role));
        } catch (RuntimeException e) {
            if ("ETAT_INVALIDE".equals(e.getMessage())) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.notFound().build();
        }
    }
}
