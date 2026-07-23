package com.flysoft.fretcorridor.common.repository;

import com.flysoft.fretcorridor.common.entity.Transporteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransporteurRepository extends JpaRepository<Transporteur, UUID> {
    List<Transporteur> findByTenantId(String tenantId);
    Optional<Transporteur> findByUtilisateurId(UUID utilisateurId);
}
