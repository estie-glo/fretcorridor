package com.flysoft.fretcorridor.common.repository;

import com.flysoft.fretcorridor.common.entity.Axe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AxeRepository extends JpaRepository<Axe, UUID> {
    List<Axe> findByTenantId(String tenantId);
}
