package com.flysoft.fretcorridor.common.repository;

import com.flysoft.fretcorridor.common.entity.Mission;
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

    List<Mission> findByTenantIdOrderByDateDeclarationDesc(String tenantId);

    List<Mission> findByTenantIdAndAxeIdOrderByDateDeclarationDesc(String tenantId, UUID axeId);

    List<Mission> findByTenantIdAndStatutOrderByDateDeclarationDesc(
            String tenantId, Mission.StatutMission statut);

    List<Mission> findByTenantIdAndAxeIdAndStatutOrderByDateDeclarationDesc(
            String tenantId, UUID axeId, Mission.StatutMission statut);

    Optional<Mission> findByIdAndTenantId(UUID id, String tenantId);
}
