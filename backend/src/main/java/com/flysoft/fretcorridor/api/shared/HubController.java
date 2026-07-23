package com.flysoft.fretcorridor.api.shared;

import com.flysoft.fretcorridor.common.dto.HubDto;
import com.flysoft.fretcorridor.common.security.JwtService;
import com.flysoft.fretcorridor.common.service.HubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * GEO — hubs du réseau (carte corridors), accessible web + mobile.
 */
@RestController
@RequestMapping("/api/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubService hubService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<HubDto.HubResponse>> getHubs(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String tenantId = jwtService.extraireTenantId(token);
        return ResponseEntity.ok(hubService.getHubs(tenantId));
    }
}
