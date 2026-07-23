package com.flysoft.fretcorridor.common.repository;

import com.flysoft.fretcorridor.common.entity.Chargeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChargeurRepository extends JpaRepository<Chargeur, UUID> {
    List<Chargeur> findByTenantId(String tenantId);
    Optional<Chargeur> findByUtilisateurId(UUID utilisateurId);
}
