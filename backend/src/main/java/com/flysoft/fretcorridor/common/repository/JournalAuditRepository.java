package com.flysoft.fretcorridor.common.repository;

import com.flysoft.fretcorridor.common.entity.JournalAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JournalAuditRepository extends JpaRepository<JournalAudit, UUID> {

    List<JournalAudit> findByTenantIdOrderByHorodatageDesc(String tenantId);
}
