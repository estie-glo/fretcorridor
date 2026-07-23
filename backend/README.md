# FretCorridor — Backend

API REST **Spring Boot 3** (Java 17) pour la plateforme logistique multi-tenant FretCorridor.

## Stack

- Spring Boot, Spring Security, JWT
- PostgreSQL 16 (JPA/Hibernate)
- Redis 7 (refresh tokens)

## Prérequis

- Java 17+
- Maven 3.9+
- Docker (PostgreSQL + Redis via `docker-compose` à la racine du monorepo)

## Démarrage rapide

```bash
# Depuis la racine du monorepo
docker compose up -d

# Lancer l'API
cd backend
mvn spring-boot:run
```

L'API écoute sur **http://localhost:8080** (préfixe `/api`).

## Organisation des packages (important)

Une seule application Spring Boot. Les contrôleurs sont **séparés par client** pour que l’équipe web et l’équipe mobile avancent sans se marcher dessus. Le domaine métier reste partagé.

```
backend/src/main/java/com/flysoft/fretcorridor/
├── FretCorridorApplication.java
├── common/                         ← domaine partagé (PAS de @RestController ici)
│   ├── config/                     # Security, DataInitializer…
│   ├── entity/                     # Entités JPA
│   ├── repository/                 # Spring Data
│   ├── security/                   # JWT, filtres
│   ├── service/                    # Logique métier
│   └── dto/                        # DTOs partagés (consommés par services + API)
└── api/                            ← couche HTTP uniquement
    ├── shared/                     # Auth, axes… (web + mobile)
    ├── web/                        # Portail Angular (bureau, chargeur, admin)
    └── mobile/                     # App Flutter (chauffeur, agent)
```

### Qui écrit où ?

| Équipe | Dossier | Exemples |
|--------|---------|----------|
| **Les deux** | `api/shared/` | `AuthController`, `AxeController` |
| **Web** | `api/web/` | `AdminKycController`, `ChargeurController`, `ChauffeurAdminController` |
| **Mobile** | `api/mobile/` | `ChauffeurController`, `MissionController`, `CamionController`, `TransporteurController` |
| **Les deux** | `common/**` | Entités, services, repositories — à modifier avec prudence (impact cross-client) |

### Règles

1. **Nouveau endpoint web** → classe dans `api.web` (ou `api.shared` si aussi mobile).
2. **Nouveau endpoint mobile** → classe dans `api.mobile` (ou `api.shared` si aussi web).
3. **Nouvelle logique métier / table** → `common.service` / `common.entity` / `common.repository`.
4. **Ne pas** créer un second projet Maven sous `backend/mobile` : ce serait un autre serveur. La séparation se fait au niveau des packages Java ci-dessus.
5. Les **URLs publiques** restent sous `/api/...` (pas de préfixe `/api/web` ou `/api/mobile` pour l’instant) — seule l’organisation du code change.

## Endpoints principaux (Sprint 1+)

### Shared
| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/auth/login` | Connexion téléphone + PIN |
| POST | `/api/auth/refresh` | Renouvellement du token |
| POST | `/api/auth/logout` | Déconnexion |
| PUT | `/api/auth/fcm-token` | Token FCM (mobile) |
| GET | `/api/axes` | Liste des axes |
| GET | `/api/axes/{id}/statut` | Statut d’un axe |
| GET | `/api/hubs` | Hubs du réseau (carte) |
| GET | `/api/missions/{id}/tracking` | Points GPS d’une mission |
| GET | `/api/missions/{id}/eta` | ETA simplifiée |

### Web
| Méthode | URL | Description |
|---------|-----|-------------|
| GET | `/api/missions` | Liste missions bureau (`axeId`, `statut` optionnels) |
| GET | `/api/missions/offres` | Offres camion vide (chargeur / bureau) |
| GET | `/api/missions/{id}` | Détail mission |
| PATCH | `/api/axes/{id}/activation` | Changer l’état d’activation GEO |
| GET | `/api/admin/kyc/en-attente` | KYC en attente |
| PUT | `/api/admin/kyc/{id}/valider` | Valider un KYC |
| GET | `/api/admin/chauffeurs` | Liste chauffeurs (back-office) |
| GET | `/api/chauffeurs/{id}` | Profil chauffeur |
| GET/POST | `/api/chargeurs` | Chargeurs |

### Mobile
| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/chauffeurs` | Enrôlement chauffeur |
| GET | `/api/chauffeurs` | Chauffeurs de l’agent |
| POST | `/api/kyc/documents` | Upload justificatif |
| POST | `/api/missions/declare-vide` | Déclaration camion vide |
| GET | `/api/missions/matchs` | Matchs disponibles |
| … | `/api/camions`, `/api/transporteurs` | Flotte |

## Configuration

Variables dans `src/main/resources/application.yml` — base PostgreSQL et Redis alignées sur `docker-compose.yml` (port Postgres **5433** en local).

## Tests

```bash
mvn test
mvn compile   # vérif rapide après refactor
```
