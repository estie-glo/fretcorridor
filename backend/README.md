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

## Structure

```
backend/
├── pom.xml
└── src/main/java/com/flysoft/fretcorridor/
    ├── config/          # Security, CORS…
    ├── controller/      # REST (auth, axes, chauffeurs, missions…)
    ├── dto/             # Objets de transfert
    ├── entity/          # Entités JPA
    ├── repository/      # Spring Data
    ├── security/        # JWT, filtres
    └── service/         # Logique métier
```

## Endpoints principaux (Sprint 1+)

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/auth/login` | Connexion téléphone + PIN |
| POST | `/api/auth/refresh` | Renouvellement du token |
| POST | `/api/auth/logout` | Déconnexion |
| PUT | `/api/auth/fcm-token` | Mise à jour token FCM (mobile) |

## Configuration

Variables dans `src/main/resources/application.yml` — base PostgreSQL et Redis alignées sur `docker-compose.yml` (port Postgres **5433** en local).

## Tests

```bash
mvn test
```
