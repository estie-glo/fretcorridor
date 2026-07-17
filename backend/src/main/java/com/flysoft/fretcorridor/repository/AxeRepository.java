package com.flysoft.fretcorridor.repository;

import com.flysoft.fretcorridor.entity.Axe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AxeRepository extends JpaRepository<Axe, UUID> {
    List<Axe> findByTenantId(String tenantId);
}
