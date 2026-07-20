# FretCorridor

Monorepo de la plateforme logistique **FretCorridor** — supervision multi-tenant des corridors fret (contexte Afrique / Cameroun).

**Flysoft Engineering SAS** — 2026

## Structure du monorepo

```
fretcorridor/
├── docs/                 # Documentation transverse (architecture, API, ADR…)
├── backend/              # API REST Spring Boot (Java 17)
├── mobile/               # Application Flutter (chauffeurs & agents)
├── web/                  # Portail Angular 22 (bureau, chargeur, admin)
├── docker-compose.yml    # PostgreSQL + Redis (dev local)
└── README.md             # Ce fichier
```

## Démarrage rapide (environnement complet)

### 1. Infrastructure

```bash
docker compose up -d
docker ps   # fretcorridor-postgres, fretcorridor-redis
```

### 2. Backend

```bash
cd backend && mvn spring-boot:run
# → http://localhost:8080/api
```

### 3. Web

```bash
cd web && npm install && npm start
# → http://localhost:4200
```

### 4. Mobile

```bash
cd mobile && flutter pub get && flutter run
```

## Modules

| Module | Stack | README |
|--------|-------|--------|
| [backend](./backend/) | Spring Boot, PostgreSQL, Redis, JWT | [backend/README.md](./backend/README.md) |
| [mobile](./mobile/) | Flutter, Riverpod, Dio | [mobile/README.md](./mobile/README.md) |
| [web](./web/) | Angular 22, PWA, i18n | [web/README.md](./web/README.md) |
| [docs](./docs/) | Markdown, Mermaid | [docs/README.md](./docs/README.md) |

## Auth & multi-tenant

- Connexion par **téléphone + code PIN**
- JWT (access + refresh via Redis)
- Configuration tenant renvoyée au login (langue, devise, axes disponibles)
- Rôles : `CHAUFFEUR`, `AGENT`, `CHARGEUR`, `ADMIN`…

## Ports locaux

| Service | Port |
|---------|------|
| API Spring Boot | 8080 |
| Angular dev server | 4200 |
| PostgreSQL | 5433 |
| Redis | 6379 |

## Documentation

Voir le dossier [`docs/`](./docs/) pour l'architecture, les spécifications API et le contexte technique du portail web ([`docs/CONTEXT.md`](./docs/CONTEXT.md)).

## Licence

Propriétaire — Flysoft Engineering SAS.
