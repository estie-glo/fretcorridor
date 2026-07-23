package com.flysoft.fretcorridor.common;

import com.flysoft.fretcorridor.common.dto.NotificationDto;
import com.flysoft.fretcorridor.common.entity.Notification;
import com.flysoft.fretcorridor.common.entity.Utilisateur;
import com.flysoft.fretcorridor.common.repository.NotificationRepository;
import com.flysoft.fretcorridor.common.repository.UtilisateurRepository;
import com.flysoft.fretcorridor.common.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;

    private NotificationService notificationService;

    private final String tenantId = "BGFT_CM";
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, utilisateurRepository);
    }

    @Test
    void envoyer_creeToujoursInApp() {
        Utilisateur user = Utilisateur.builder()
                .id(userId)
                .telephone("+237600000002")
                .codePin("x")
                .role(Utilisateur.Role.OPERATEUR)
                .tenantId(tenantId)
                .build();
        when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> {
                    Notification n = inv.getArgument(0);
                    n.setId(UUID.randomUUID());
                    return n;
                });

        NotificationDto.SendRequest req = new NotificationDto.SendRequest();
        req.setDestinataireId(userId);
        req.setType("SYSTEME");
        req.setTitreFr("Titre");
        req.setTitreEn("Title");
        req.setCorpsFr("Corps");
        req.setCorpsEn("Body");
        req.setCanalPrefere("WHATSAPP");

        List<NotificationDto.NotificationResponse> result =
                notificationService.envoyer(req, tenantId);

        assertEquals(2, result.size());
        assertEquals("IN_APP", result.get(0).getCanal());
        assertEquals("DELIVRE", result.get(0).getStatutEnvoi());
        assertEquals("WHATSAPP", result.get(1).getCanal());
        assertEquals("STUB_QUEUED", result.get(1).getStatutEnvoi());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
    }

    @Test
    void marquerLue_ok() {
        UUID notifId = UUID.randomUUID();
        Notification n = Notification.builder()
                .id(notifId)
                .tenantId(tenantId)
                .destinataireId(userId)
                .canal(Notification.Canal.IN_APP)
                .type("SYSTEME")
                .titreFr("t")
                .titreEn("t")
                .corpsFr("c")
                .corpsEn("c")
                .lue(false)
                .build();
        when(notificationRepository.findByIdAndDestinataireIdAndTenantId(notifId, userId, tenantId))
                .thenReturn(Optional.of(n));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(notificationService.marquerLue(notifId, userId, tenantId).isLue());
    }
}
