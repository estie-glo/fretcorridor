package com.flysoft.fretcorridor.repository;

import com.flysoft.fretcorridor.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CamionRepository extends JpaRepository<Camion, UUID> {
    List<Camion> findByTransporteurIdAndTenantId(UUID transporteurId, String tenantId);
    List<Camion> findByTenantId(String tenantId);
}
