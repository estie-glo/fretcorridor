package com.flysoft.fretcorridor.common.repository;

import com.flysoft.fretcorridor.common.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByDestinataireIdAndTenantIdOrderByDateCreationDesc(
            UUID destinataireId, String tenantId);

    long countByDestinataireIdAndTenantIdAndLueFalse(UUID destinataireId, String tenantId);

    Optional<Notification> findByIdAndDestinataireIdAndTenantId(
            UUID id, UUID destinataireId, String tenantId);
}
