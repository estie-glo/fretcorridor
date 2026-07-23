package com.flysoft.fretcorridor.common.repository;

import com.flysoft.fretcorridor.common.entity.PositionGps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PositionGpsRepository extends JpaRepository<PositionGps, UUID> {

    List<PositionGps> findByMissionIdAndTenantIdOrderByRecordedAtAsc(UUID missionId, String tenantId);

    Optional<PositionGps> findFirstByMissionIdAndTenantIdOrderByRecordedAtDesc(UUID missionId, String tenantId);
}
