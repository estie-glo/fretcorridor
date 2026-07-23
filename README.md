# FretCorridor

Monorepo de la plateforme logistique **FretCorridor** — supervision multi-tenant des corridors fret (contexte Afrique / Cameroun).

**Flysoft Engineering SAS** — 2026

## Structure du monorepo

```
fretcorridor/
├── docs/                 # Documentation transverse (architecture, API, ADR…)
├── backend/              # API REST Spring Boot (Java 17) — une app, packages web/mobile/shared
│   └── …/api/{shared,web,mobile} + common/
├── mobile/               # Application Flutter (chauffeurs & agents)
├── web/                  # Portail Angular 22 (bureau, chargeur, admin)
├── docker-compose.yml    # Stack complète (postgres, redis, minio, backend, web)
└── README.md             # Ce fichier
```

> Convention backend : nouveaux endpoints web → `backend/.../api/web/`, mobile → `api/mobile/`, communs → `api/shared/`. Détail : [`docs/backend-structure.md`](./docs/backend-structure.md).

## Démarrage rapide

### Option A — tout en Docker (recommandé)

```bash
docker compose up --build
# → Web http://localhost:4200
# → API http://localhost:8080
# → MinIO console http://localhost:9001 (fretcorridor / fretcorridor123)
```

Premier build : compter 5–10 min (Maven + npm). Les comptes démo sont accessibles via les boutons sur l'écran de login.

### Option B — dev local (hot reload)

```bash
docker compose up -d postgres redis minio
cd backend && mvn spring-boot:run    # :8080
cd web && nvm use 22 && npm start     # :4200
```

### Mobile

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
| Web (Docker ou ng serve) | 4200 |
| API Spring Boot | 8080 |
| PostgreSQL | 5433 |
| Redis | 6379 |
| MinIO API / console | 9000 / 9001 |

## Documentation

| Doc | Rôle |
|-----|------|
| [`docs/ROADMAP.md`](./docs/ROADMAP.md) | **Roadmap sprints** (source de vérité — à suivre à la lettre) |
| [`docs/CONTEXT.md`](./docs/CONTEXT.md) | Contexte technique monorepo (agents / IDE) |
| [`docs/backend-structure.md`](./docs/backend-structure.md) | Packages API `web` / `mobile` / `shared` |
| [`docs/README.md`](./docs/README.md) | Index documentation |

Sprint courant : voir l’en-tête de `ROADMAP.md` (aujourd’hui **S5 web OK — S6 suivant** ; mobile S5 différé).

## Licence

Propriétaire — Flysoft Engineering SAS.
