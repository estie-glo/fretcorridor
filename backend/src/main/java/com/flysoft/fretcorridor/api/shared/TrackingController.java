package com.flysoft.fretcorridor.api.shared;

import com.flysoft.fretcorridor.common.dto.TrackingDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * TRK — lecture tracking / ETA (consommé par web bureau et éventuellement mobile).
 * L'écriture des positions reste côté api.mobile.
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;
    private final JwtService jwtService;

    @GetMapping("/{id}/tracking")
    public ResponseEntity<TrackingDto.TrackingResponse> getTracking(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String tenantId = jwtService.extraireTenantId(token);
            return ResponseEntity.ok(trackingService.getTracking(id, tenantId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/eta")
    public ResponseEntity<TrackingDto.EtaResponse> getEta(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String tenantId = jwtService.extraireTenantId(token);
            return ResponseEntity.ok(trackingService.getEta(id, tenantId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
