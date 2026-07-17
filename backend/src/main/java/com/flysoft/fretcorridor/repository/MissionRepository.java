package com.flysoft.fretcorridor.repository;

import com.flysoft.fretcorridor.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MissionRepository extends JpaRepository<Mission, UUID> {

    // Idempotence : retrouver une mission déjà créée avec cette clé
    Optional<Mission> findByIdempotencyKeyAndTenantId(String idempotencyKey, String tenantId);

    List<Mission> findByChauffeurIdAndTenantId(UUID chauffeurId, String tenantId);

    List<Mission> findByStatutAndTenantId(Mission.StatutMission statut, String tenantId);
}
