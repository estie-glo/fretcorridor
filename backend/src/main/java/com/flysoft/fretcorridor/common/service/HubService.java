package com.flysoft.fretcorridor.common.service;

import com.flysoft.fretcorridor.common.dto.HubDto;
import com.flysoft.fretcorridor.common.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HubService {

    private final HubRepository hubRepository;

    public List<HubDto.HubResponse> getHubs(String tenantId) {
        return hubRepository.findByTenantId(tenantId).stream()
                .map(HubDto.HubResponse::fromEntity)
                .toList();
    }
}
