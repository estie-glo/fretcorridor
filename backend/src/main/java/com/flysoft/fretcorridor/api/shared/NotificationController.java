package com.flysoft.fretcorridor.api.shared;

import com.flysoft.fretcorridor.common.dto.NotificationDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.security.RoleChecks;
import com.flysoft.fretcorridor.common.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * EF-NOT — endpoints notifications (web + mobile).
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<NotificationDto.NotificationResponse>> lister(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UUID userId = jwtService.extraireUserId(token);
        String tenantId = jwtService.extraireTenantId(token);
        return ResponseEntity.ok(notificationService.lister(userId, tenantId));
    }

    @GetMapping("/non-lues")
    public ResponseEntity<Map<String, Long>> compterNonLues(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UUID userId = jwtService.extraireUserId(token);
        String tenantId = jwtService.extraireTenantId(token);
        return ResponseEntity.ok(Map.of(
                "count", notificationService.compterNonLues(userId, tenantId)));
    }

    @PatchMapping("/{id}/lue")
    public ResponseEntity<?> marquerLue(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UUID userId = jwtService.extraireUserId(token);
        String tenantId = jwtService.extraireTenantId(token);
        try {
            return ResponseEntity.ok(notificationService.marquerLue(id, userId, tenantId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> envoyer(
            @Valid @RequestBody NotificationDto.SendRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        String role = jwtService.extraireRole(token);

        if (!RoleChecks.isBackOffice(role)) {
            return ResponseEntity.status(403).build();
        }

        try {
            return ResponseEntity.status(201).body(notificationService.envoyer(request, tenantId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
