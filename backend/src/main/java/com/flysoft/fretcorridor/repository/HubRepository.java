package com.flysoft.fretcorridor.repository;

import com.flysoft.fretcorridor.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface HubRepository extends JpaRepository<Hub, UUID> {
}
