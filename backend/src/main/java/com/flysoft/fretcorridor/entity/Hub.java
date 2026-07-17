package com.flysoft.fretcorridor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "hubs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hub {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nom;        // ex: "Douala", "N'Djamena"

    @Column(nullable = false)
    private String pays;       // ex: "CM", "TD"

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private String tenantId;
}
