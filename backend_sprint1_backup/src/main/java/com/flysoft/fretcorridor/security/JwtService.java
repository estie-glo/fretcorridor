package com.flysoft.fretcorridor.security;

import com.flysoft.fretcorridor.entity.Utilisateur;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // Génère la clé de signature
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ── Génère un Access Token JWT (24h) ─────────────────────
    public String genererAccessToken(Utilisateur utilisateur) {
        return Jwts.builder()
                .subject(utilisateur.getId().toString())
                .claim("role", utilisateur.getRole().name())
                .claim("tenantId", utilisateur.getTenantId())
                .claim("telephone", utilisateur.getTelephone())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Génère un Refresh Token (30j) ────────────────────────
    public String genererRefreshToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Vérifie et parse un token ─────────────────────────────
    public Claims parserToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ── Extrait l'userId du token ─────────────────────────────
    public UUID extraireUserId(String token) {
        return UUID.fromString(parserToken(token).getSubject());
    }

    // ── Extrait le tenantId du token ──────────────────────────
    public String extraireTenantId(String token) {
        return parserToken(token).get("tenantId", String.class);
    }

    // ── Extrait le rôle du token ──────────────────────────────
    public String extraireRole(String token) {
        return parserToken(token).get("role", String.class);
    }

    // ── Vérifie si le token est valide ────────────────────────
    public boolean estValide(String token) {
        try {
            parserToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Vérifie si le token est expiré ────────────────────────
    public boolean estExpire(String token) {
        try {
            return parserToken(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
