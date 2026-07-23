package com.flysoft.fretcorridor.common.repository;

import com.flysoft.fretcorridor.common.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    // Trouver un utilisateur par numéro de téléphone
    Optional<Utilisateur> findByTelephone(String telephone);

    // Vérifier si un téléphone existe déjà
    boolean existsByTelephone(String telephone);
}
