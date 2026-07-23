package com.flysoft.fretcorridor.common;

import com.flysoft.fretcorridor.common.dto.ChauffeurDto;
import com.flysoft.fretcorridor.common.entity.Chauffeur;
import com.flysoft.fretcorridor.common.entity.Utilisateur;
import com.flysoft.fretcorridor.common.repository.AgentRepository;
import com.flysoft.fretcorridor.common.repository.ChauffeurRepository;
import com.flysoft.fretcorridor.common.repository.UtilisateurRepository;
import com.flysoft.fretcorridor.common.service.ChauffeurService;
import com.flysoft.fretcorridor.common.service.DocumentStorageService;
import com.flysoft.fretcorridor.common.service.JournalAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChauffeurServiceTest {

    @Mock
    private ChauffeurRepository chauffeurRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private AgentRepository agentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JournalAuditService journalAuditService;
    @Mock
    private DocumentStorageService documentStorageService;

    private ChauffeurService chauffeurService;

    private final String tenantId = "BGFT_CM";
    private final UUID chauffeurId = UUID.randomUUID();
    private final UUID acteurId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        chauffeurService = new ChauffeurService(
                chauffeurRepository, utilisateurRepository, agentRepository,
                passwordEncoder, journalAuditService, documentStorageService);
    }

    @Test
    void validerKyc_niveau2_sansDocs_leveDocsManquants() {
        Chauffeur chauffeur = chauffeurBase();
        chauffeur.setUrlPhotoCNI(null);
        chauffeur.setUrlPhotoPermis(null);
        when(chauffeurRepository.findById(chauffeurId)).thenReturn(Optional.of(chauffeur));

        ChauffeurDto.ValidationKycRequest request = new ChauffeurDto.ValidationKycRequest();
        request.setApprouve(true);
        request.setNouveauNiveau("NIVEAU_2");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                chauffeurService.validerKyc(chauffeurId, request, tenantId, acteurId, "OPERATEUR"));
        assertEquals("DOCS_MANQUANTS", ex.getMessage());
    }

    @Test
    void validerKyc_niveau2_avecDocs_valide() {
        Chauffeur chauffeur = chauffeurBase();
        chauffeur.setUrlPhotoCNI("http://localhost:9000/fretcorridor-kyc/demo/cni.jpg");
        chauffeur.setUrlPhotoPermis("http://localhost:9000/fretcorridor-kyc/demo/permis.jpg");
        when(chauffeurRepository.findById(chauffeurId)).thenReturn(Optional.of(chauffeur));
        when(chauffeurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChauffeurDto.ValidationKycRequest request = new ChauffeurDto.ValidationKycRequest();
        request.setApprouve(true);
        request.setNouveauNiveau("NIVEAU_2");

        ChauffeurDto.ChauffeurResponse response = chauffeurService.validerKyc(
                chauffeurId, request, tenantId, acteurId, "OPERATEUR");

        assertEquals("VALIDE", response.getStatutKyc());
        assertEquals("NIVEAU_2", response.getKycNiveau());
        verify(journalAuditService).enregistrer(
                eq(tenantId), eq(acteurId), eq("OPERATEUR"),
                eq("KYC_VALIDER"), eq("CHAUFFEUR"), eq(chauffeurId),
                any(), any());
    }

    private Chauffeur chauffeurBase() {
        Utilisateur utilisateur = Utilisateur.builder()
                .id(UUID.randomUUID())
                .telephone("+237699000001")
                .role(Utilisateur.Role.CHAUFFEUR)
                .tenantId(tenantId)
                .build();
        return Chauffeur.builder()
                .id(chauffeurId)
                .nom("Mbarga")
                .prenom("Paul")
                .numeroCNI("CNI-001")
                .tenantId(tenantId)
                .utilisateur(utilisateur)
                .statutKyc(Chauffeur.StatutKyc.EN_COURS)
                .kycNiveau(Chauffeur.KycNiveau.NIVEAU_1)
                .build();
    }
}
