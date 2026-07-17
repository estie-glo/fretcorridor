package com.flysoft.fretcorridor.repository;

import com.flysoft.fretcorridor.entity.Chauffeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChauffeurRepository extends JpaRepository<Chauffeur, UUID> {

    // Tous les chauffeurs d'un tenant
    List<Chauffeur> findByTenantId(String tenantId);

    // Chauffeurs supervisés par un agent
    List<Chauffeur> findByAgentIdAndTenantId(UUID agentId, String tenantId);

    // Chauffeurs en attente de validation KYC
    List<Chauffeur> findByStatutKycAndTenantId(
        Chauffeur.StatutKyc statut, String tenantId);

    // Trouver par utilisateur
    Optional<Chauffeur> findByUtilisateurId(UUID utilisateurId);
}
