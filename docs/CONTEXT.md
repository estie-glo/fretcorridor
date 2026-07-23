# CONTEXT.md — FretCorridor (monorepo)

> Document de référence pour reprendre le contexte du projet rapidement (IDE / agents).
> **À mettre à jour à chaque évolution de code** (architecture, auth, routes, endpoints, sprint).
>
> Date d’analyse : **2026-07-21**.

---

## 0. Sprint & roadmap (pilotage)

| Champ | Valeur |
|-------|--------|
| **Sprint courant** | **MVP Phase 1 clôturé** — prochaine : Phase 2 S9 |
| **Phase** | Phase 1 — MVP ✅ (S1→S8 web) |
| **Roadmap vivante** | [`docs/ROADMAP.md`](./ROADMAP.md) — **source de vérité** ; la suivre **à la lettre** |

Après chaque feature / merge : mettre à jour `ROADMAP.md` (statut sprint, date, journal) **et** cette section / les endpoints ci-dessous.

S5 mobile (`POST /positions`) reste différée hors `api.web`.

> Convention agents : **uniquement `api.web` + `web/`** — jamais `api.mobile`.

---

## 1. Vue d’ensemble

**FretCorridor** est une plateforme logistique multi-tenant (corridors Cameroun / CEMAC, B2B2G) : supervision d’axes, KYC, missions, tracking.

Le dépôt est un **monorepo** :

| Module | Chemin | Stack |
|--------|--------|-------|
| API | `backend/` | Spring Boot 3, Java 17, PostgreSQL, Redis, JWT |
| Portail | `web/` | Angular 22, PWA, i18n FR/EN, signals |
| Mobile | `mobile/` | Flutter (chauffeur / agent) |
| Docs | `docs/` | CONTEXT, ROADMAP, backend-structure |

Auth : **téléphone + code PIN** (`codePin`) → `{ accessToken, refreshToken, role, tenantId, configTenant }`.  
API préfixée `/api` (proxy web → `http://localhost:8080`).

---

## 2. Architecture monorepo

```
fretcorridor/
├── backend/          # une app Spring Boot
│   └── …/api/{shared,web,mobile} + common/
├── web/              # Angular 22
├── mobile/           # Flutter
├── docs/             # CONTEXT, ROADMAP, …
└── docker-compose.yml
```

Convention packages backend : voir [`backend-structure.md`](./backend-structure.md).

### Web (`web/src/app/`) — résumé

- `core/` — auth, guards, interceptors, thème tenant
- `features/` — `auth`, `bureau` (axes + **missions**), `chargeur` (distribution + **offres**), `admin` (KYC, chauffeurs, missions)
- `layout/shell/` — header logo + nav + outlet
- `shared/` — axes-explorer, corridor-map, mission-tracking-map (Leaflet), brand-logo, models, services (`AxesService`, `HubsService`, `MissionsService`)
- Design : tokens `--fc-*`, classes `fc-*`, primaire marque `#d40f16`, Montserrat (`DESIGN.md` / `PRODUCT.md` à la racine `web/`)

---

## 3. Rôles web (normalisation)

`UserRole` : `BUREAU` · `CHARGEUR` · `ADMIN`

- Admin ← `ADMIN` / `BACK_OFFICE` / **`OPERATEUR`**
- Chargeur ← `CHARGEUR` / `SHIPPER` / `CLIENT`
- Bureau ← défaut

Homes : Admin → `/admin`, Chargeur → `/chargeur`, Bureau → `/bureau`.

---

## 4. Routes web principales

```
/login
/bureau              → axes + carte Leaflet
/bureau/missions     → missions + carte suivi GPS + ETA
/bureau/notifications → centre notifications
/chargeur            → distribution (axes + carte)
/chargeur/offres     → offres camion vide (MKT)
/chargeur/notifications
/admin/kyc
/admin/chauffeurs
/admin/missions
/admin/notifications
/admin/audit
```

---

## 5. Endpoints API déjà livrés (consommés / exposés)

### Shared
| Méthode | URL | Usage |
|---------|-----|--------|
| POST | `/api/auth/login` | `{ telephone, codePin }` |
| POST | `/api/auth/refresh` | refresh |
| POST | `/api/auth/logout` | logout |
| GET | `/api/axes` | liste axes (coords hubs enrichies) |
| GET | `/api/axes/{id}/statut` | statut axe |
| GET | `/api/hubs` | hubs réseau |
| GET | `/api/missions/{id}/tracking` | points GPS |
| GET | `/api/missions/{id}/eta` | ETA simplifiée |
| GET | `/api/notifications` | liste notifications utilisateur |
| GET | `/api/notifications/non-lues` | compteur non lues |
| PATCH | `/api/notifications/{id}/lue` | marquer lue |
| POST | `/api/notifications/send` | envoi (back-office ; stubs canaux) |

### Web
| Méthode | URL | Usage |
|---------|-----|--------|
| GET | `/api/missions` | liste missions bureau (`axeId`, `statut`) |
| GET | `/api/missions/offres` | offres camion vide (chargeur / bureau) |
| GET | `/api/missions/{id}` | détail mission |
| POST | `/api/missions/{id}/accepter` | S6 — accepter offre/match |
| POST | `/api/missions/{id}/demarrer` | S6 — démarrer (EN_COURS) |
| POST | `/api/missions/{id}/terminer` | S6 — terminer |
| POST | `/api/missions/{id}/annuler` | S6 — annuler |
| PATCH | `/api/axes/{id}/activation` | GEO 3 flags (ou enum legacy) |
| GET | `/api/admin/kyc/en-attente` | KYC pending |
| PUT | `/api/admin/kyc/{id}/valider` | valider KYC (+ audit) |
| GET | `/api/admin/audit` | journal d'audit |
| GET | `/api/admin/chauffeurs` | liste chauffeurs back-office |
| GET | `/api/chauffeurs/{id}` | profil chauffeur |

### Mobile (exemples)
| Méthode | URL | Usage |
|---------|-----|--------|
| POST | `/api/chauffeurs` | enrôlement |
| GET | `/api/chauffeurs` | chauffeurs de l’agent |
| POST | `/api/kyc/documents` | upload KYC |
| POST | `/api/missions/declare-vide` | déclaration vide |
| GET | `/api/missions/matchs` | stub matching |

---

## 6. Auth web (détail)

- Login envoie **`codePin`** (pas `pin`).
- Refresh token : `localStorage` (`fc_refresh_token`) ; access token en mémoire.
- Intercepteur Bearer + refresh sur 401.

---

## 7. Thème

- Défaut primaire : **`#d40f16`** (marque).
- `TenantThemeService` pose `--fc-primary`, `--fc-primary-hover`, `--fc-primary-soft`, `--fc-primary-contrast`.
- UI via classes globales `fc-page`, `fc-split`, `fc-panel`, `fc-list`, `fc-btn`, etc. (`web/src/styles.scss`).

---

## 8. Démarrage rapide

```bash
# Racine monorepo — stack complète
docker compose up --build          # Web :4200 · API :8080 · MinIO :9001

# Dev local (hot reload)
docker compose up -d postgres redis minio
cd backend && mvn spring-boot:run          # :8080
cd web && nvm use 22 && npm start         # :4200 (Node ≥ 22.22)
cd mobile && flutter run
```

Infra S8 : MinIO (`fretcorridor` / `fretcorridor123`, bucket `fretcorridor-kyc`) — console http://localhost:9001.

Comptes démo (PIN `1234`) :
- Agent bureau Cameroun `+237600000001` (tenant `BGFT_CM`)
- Bureau BNFT Tchad `+235660000001` (tenant `BNFT_TD`)
- Opérateur `+237600000002`, chargeur `+237600000003` (tenant `BGFT_CM`)

---

## 9. Prochaine étape code (Phase 2 / S9)

- Matching backhaul actif (PostGIS, scoring, activation par axe).
- S5 mobile (`POST /positions`) reste hors périmètre web.

Détail et checklist : **[`ROADMAP.md`](./ROADMAP.md)**.
