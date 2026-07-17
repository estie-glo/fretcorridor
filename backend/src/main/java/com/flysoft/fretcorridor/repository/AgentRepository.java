package com.flysoft.fretcorridor.repository;

import com.flysoft.fretcorridor.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentRepository extends JpaRepository<Agent, UUID> {
    Optional<Agent> findByUtilisateurId(UUID utilisateurId);
}
