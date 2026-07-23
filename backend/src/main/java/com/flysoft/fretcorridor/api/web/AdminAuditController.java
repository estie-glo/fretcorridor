package com.flysoft.fretcorridor.api.web;

import com.flysoft.fretcorridor.common.entity.JournalAudit;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.security.RoleChecks;
import com.flysoft.fretcorridor.common.service.JournalAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * OPS — consultation du journal d'audit (EF-OPS-03).
 */
@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AdminAuditController {

    private final JournalAuditService journalAuditService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<JournalAudit>> lister(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        String role = jwtService.extraireRole(token);

        if (!RoleChecks.isBackOffice(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(journalAuditService.lister(tenantId));
    }
}
