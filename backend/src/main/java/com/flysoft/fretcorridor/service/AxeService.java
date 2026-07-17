package com.flysoft.fretcorridor.service;

import com.flysoft.fretcorridor.dto.AxeDto;
import com.flysoft.fretcorridor.entity.Axe;
import com.flysoft.fretcorridor.repository.AxeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AxeService {

    private final AxeRepository axeRepository;

    public List<AxeDto.AxeResponse> getAxesDisponibles(String tenantId) {
        return axeRepository.findByTenantId(tenantId).stream()
                .map(AxeDto.AxeResponse::fromEntity)
                .toList();
    }

    public AxeDto.AxeResponse getStatut(UUID axeId, String tenantId) {
        Axe axe = axeRepository.findById(axeId)
                .orElseThrow(() -> new RuntimeException("AXE_INTROUVABLE"));
        if (!axe.getTenantId().equals(tenantId)) {
            throw new RuntimeException("AXE_INTROUVABLE"); // isolation multi-tenant
        }
        return AxeDto.AxeResponse.fromEntity(axe);
    }
}
