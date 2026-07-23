package com.flysoft.fretcorridor.common.repository;

import com.flysoft.fretcorridor.common.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface HubRepository extends JpaRepository<Hub, UUID> {

    List<Hub> findByTenantId(String tenantId);
}
