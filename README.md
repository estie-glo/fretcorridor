# FretCorridor — Sprint 1 : Authentification
## Flysoft Engineering SAS — Juillet 2026

---

## Structure du projet

```
fretcorridor/
├── docker-compose.yml          ← Lance PostgreSQL + Redis
├── backend/                    ← Spring Boot (Java 17)
│   ├── pom.xml
│   └── src/main/java/com/flysoft/fretcorridor/
│       ├── FretCorridorApplication.java
│       ├── config/SecurityConfig.java
│       ├── controller/AuthController.java
│       ├── dto/AuthDto.java
│       ├── entity/Utilisateur.java
│       ├── repository/UtilisateurRepository.java
│       ├── security/JwtService.java
│       └── service/AuthService.java
└── flutter/                    ← App mobile Flutter
    ├── pubspec.yaml
    └── lib/
        ├── main.dart
        ├── models/utilisateur_model.dart
        ├── providers/auth_provider.dart
        ├── screens/login_screen.dart
        └── services/auth_service.dart
```

---

## ÉTAPE 1 — Installer Docker (si pas encore fait)

```bash
sudo apt install docker.io docker-compose -y
sudo usermod -aG docker $USER
# Redémarrer le terminal après cette commande
```

---

## ÉTAPE 2 — Démarrer PostgreSQL + Redis

```bash
cd fretcorridor
docker-compose up -d

# Vérifier que les conteneurs tournent
docker ps
# Tu dois voir : fretcorridor-postgres et fretcorridor-redis
```

---

## ÉTAPE 3 — Lancer le back-end Spring Boot

```bash
cd fretcorridor/backend

# Compiler et lancer
mvn spring-boot:run

# Tu dois voir dans les logs :
# Started FretCorridorApplication in X.XXX seconds
# Tomcat started on port(s): 8080
```

---

## ÉTAPE 4 — Tester l'API avec curl

### Créer un utilisateur test en base (une seule fois)
```bash
# Se connecter à PostgreSQL
docker exec -it fretcorridor-postgres psql -U fretcorridor -d fretcorridor

# Insérer un chauffeur test (PIN : 1234 hashé avec BCrypt)
INSERT INTO utilisateurs (id, telephone, code_pin, role, tenant_id, actif, tentatives_echouees)
VALUES (
  gen_random_uuid(),
  '+237699000001',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EL7YhMlMd1bGHVxRkWMc5u',
  'CHAUFFEUR',
  'BGFT_CM',
  true,
  0
);
# (le hash correspond au PIN : 1234)
\q
```

### Tester le login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"telephone":"+237699000001","codePin":"1234"}'
```

### Réponse attendue :
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci...",
  "role": "CHAUFFEUR",
  "tenantId": "BGFT_CM",
  "configTenant": {
    "tenantId": "BGFT_CM",
    "nomBureau": "BGFT Cameroun",
    "langue": "fr",
    "devise": "FCFA",
    "axesDisponibles": ["Douala-NDjamena", "Epine-Nord", "Douala-Yaounde"]
  }
}
```

### Tester le refresh
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"TON_REFRESH_TOKEN_ICI"}'
```

### Tester le logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer TON_ACCESS_TOKEN_ICI"
```

---

## ÉTAPE 5 — Lancer l'app Flutter

```bash
cd fretcorridor/flutter

# Installer les dépendances
flutter pub get

# Vérifier l'environnement Flutter
flutter doctor

# Lancer sur émulateur Android
# (d'abord ouvrir Android Studio et démarrer un émulateur)
flutter run

# OU lancer sur Chrome pour tester rapidement
flutter run -d chrome
```

---

## ENDPOINTS Sprint 1

| Méthode | URL | Description | Auth |
|---------|-----|-------------|------|
| POST | /api/auth/login | Connexion | ❌ Public |
| POST | /api/auth/refresh | Renouveler token | ❌ Public |
| POST | /api/auth/logout | Déconnexion | ✅ JWT |
| PUT | /api/auth/fcm-token | MAJ token FCM | ✅ JWT |

---

## Ce qui est implémenté (Sprint 1)

### Back-end Spring Boot ✅
- [x] Entité Utilisateur avec rôles et tenantId
- [x] Login avec BCrypt + tentatives limitées (3 max)
- [x] Génération JWT (24h) + RefreshToken (30j)
- [x] Stockage RefreshToken dans Redis
- [x] Rotation du RefreshToken au refresh
- [x] Config tenant renvoyée au mobile (axes, langue, devise)
- [x] Mise à jour FCM token
- [x] Logout propre (suppression Redis + FCM)

### Flutter Mobile ✅
- [x] Écran de connexion (téléphone + PIN)
- [x] Sélection rôle (Chauffeur / Agent)
- [x] Validation formulaire
- [x] Stockage JWT dans SecureStorage
- [x] Intercepteur Dio (refresh automatique si 401)
- [x] AuthProvider Riverpod (state management)
- [x] Redirection automatique selon le rôle
- [x] Gestion des erreurs (PIN incorrect, compte bloqué...)

---

## Sprint 2 — ce qui vient ensuite
- Dashboard chauffeur (missions, statut réseau)
- Dashboard agent (liste chauffeurs, KYC)
- Service GPS arrière-plan
- Module offline-first (SQLite)
