package com.flysoft.fretcorridor.common.service;

import com.flysoft.fretcorridor.common.dto.AuthDto;
import com.flysoft.fretcorridor.common.entity.Axe;
import com.flysoft.fretcorridor.common.entity.Utilisateur;
import com.flysoft.fretcorridor.common.repository.AxeRepository;
import com.flysoft.fretcorridor.common.repository.UtilisateurRepository;
import com.flysoft.fretcorridor.common.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final AxeRepository axeRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_TENTATIVES = 3;
    private static final String REDIS_REFRESH_PREFIX = "refreshToken:";
    private static final String REDIS_LOGIN_RATE_PREFIX = "loginRate:";
    private static final int MAX_LOGIN_PAR_MINUTE = 15;

    // ── LOGIN ─────────────────────────────────────────────────
    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {

        verifierLimiteLogin(request.getTelephone());

        // 1. Vérifier si l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository
                .findByTelephone(request.getTelephone())
                .orElseThrow(() -> new RuntimeException("UTILISATEUR_INTROUVABLE"));

        // 2. Vérifier si le compte est actif
        if (!utilisateur.getActif()) {
            throw new RuntimeException("COMPTE_DESACTIVE");
        }

        // 3. Vérifier le nombre de tentatives
        if (utilisateur.getTentativesEchouees() >= MAX_TENTATIVES) {
            throw new RuntimeException("COMPTE_BLOQUE");
        }
        
        // 4. Vérifier le PIN avec BCrypt
        if (!passwordEncoder.matches(request.getCodePin(), utilisateur.getCodePin())) {
            // Incrémenter les tentatives échouées
            utilisateur.setTentativesEchouees(utilisateur.getTentativesEchouees() + 1);
            utilisateurRepository.save(utilisateur);
            int restantes = MAX_TENTATIVES - utilisateur.getTentativesEchouees();
            throw new RuntimeException("PIN_INCORRECT:" + restantes);
        }

        // 5. Réinitialiser les tentatives après succès
        utilisateur.setTentativesEchouees(0);
        utilisateurRepository.save(utilisateur);

        // 6. Générer les tokens
        String accessToken  = jwtService.genererAccessToken(utilisateur);
        String refreshToken = jwtService.genererRefreshToken(utilisateur.getId());

        // 7. Stocker le refresh token dans Redis (TTL 30j)
        redisTemplate.opsForValue().set(
                REDIS_REFRESH_PREFIX + utilisateur.getId(),
                refreshToken,
                30, TimeUnit.DAYS
        );

        log.info("Login réussi — utilisateur: {} tenant: {}", utilisateur.getTelephone(), utilisateur.getTenantId());

        // 8. Construire et retourner la réponse avec config tenant
        return AuthDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(utilisateur.getRole().name())
                .tenantId(utilisateur.getTenantId())
                .configTenant(getConfigTenant(utilisateur.getTenantId()))
                .build();
    }

    // ── REFRESH TOKEN ─────────────────────────────────────────
    public AuthDto.LoginResponse refresh(AuthDto.RefreshRequest request) {

        // 1. Vérifier validité du refresh token
        if (!jwtService.estValide(request.getRefreshToken())) {
            throw new RuntimeException("REFRESH_TOKEN_INVALIDE");
        }

        // 2. Extraire l'userId
        UUID userId = jwtService.extraireUserId(request.getRefreshToken());

        // 3. Vérifier dans Redis
        String tokenEnBase = redisTemplate.opsForValue()
                .get(REDIS_REFRESH_PREFIX + userId);

        if (tokenEnBase == null || !tokenEnBase.equals(request.getRefreshToken())) {
            throw new RuntimeException("SESSION_EXPIREE");
        }

        // 4. Récupérer l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("UTILISATEUR_INTROUVABLE"));

        // 5. Générer nouveaux tokens (rotation)
        String newAccessToken  = jwtService.genererAccessToken(utilisateur);
        String newRefreshToken = jwtService.genererRefreshToken(userId);

        // 6. Mettre à jour Redis
        redisTemplate.opsForValue().set(
                REDIS_REFRESH_PREFIX + userId,
                newRefreshToken,
                30, TimeUnit.DAYS
        );

        return AuthDto.LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .role(utilisateur.getRole().name())
                .tenantId(utilisateur.getTenantId())
                .configTenant(getConfigTenant(utilisateur.getTenantId()))
                .build();
    }

    // ── LOGOUT ───────────────────────────────────────────────
    public void logout(UUID userId, String fcmToken) {
        // 1. Supprimer refresh token de Redis
        redisTemplate.delete(REDIS_REFRESH_PREFIX + userId);

        // 2. Supprimer FCM token
        utilisateurRepository.findById(userId).ifPresent(u -> {
            u.setFcmToken(null);
            utilisateurRepository.save(u);
        });

        log.info("Logout réussi — userId: {}", userId);
    }

    // ── MISE À JOUR FCM TOKEN ─────────────────────────────────
    public void mettreAJourFcmToken(UUID userId, String fcmToken) {
        utilisateurRepository.findById(userId).ifPresent(u -> {
            u.setFcmToken(fcmToken);
            utilisateurRepository.save(u);
        });
    }

    private void verifierLimiteLogin(String telephone) {
        String key = REDIS_LOGIN_RATE_PREFIX + telephone;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }
        if (count != null && count > MAX_LOGIN_PAR_MINUTE) {
            throw new RuntimeException("TROP_DE_TENTATIVES");
        }
    }

    // ── CONFIG TENANT — métadonnées bureau + axes visibles (hors INACTIF) depuis GEO ──
    private AuthDto.ConfigTenant getConfigTenant(String tenantId) {
        String nomBureau = switch (tenantId) {
            case "BGFT_CM" -> "BGFT Cameroun";
            case "BNFT_TD" -> "BNFT Tchad";
            case "BARC_RCA" -> "BARC RCA";
            default -> "Bureau " + tenantId;
        };

        // EF-GEO-03 : seuls les axes à visibilité active apparaissent
        String[] axesDisponibles = axeRepository.findByTenantId(tenantId).stream()
                .filter(Axe::isVisibiliteActive)
                .map(Axe::getNom)
                .sorted()
                .toArray(String[]::new);

        return AuthDto.ConfigTenant.builder()
                .tenantId(tenantId)
                .nomBureau(nomBureau)
                .langue("fr")
                .devise("FCFA")
                .axesDisponibles(axesDisponibles)
                .build();
    }
}
