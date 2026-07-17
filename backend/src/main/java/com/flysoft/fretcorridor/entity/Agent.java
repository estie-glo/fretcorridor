package com.flysoft.fretcorridor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "agents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    // Zone géographique supervisée
    private String zone;         // ex: "Douala-Port"
    private String bureauFret;   // ex: "BGFT Cameroun"

    @Column(nullable = false)
    private String tenantId;

    // Chauffeurs supervisés par cet agent
    @OneToMany(mappedBy = "agent")
    private List<Chauffeur> chauffeurs;
}
