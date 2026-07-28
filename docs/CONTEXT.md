# CONTEXT.md — FretCorridor (monorepo)

> Document de référence pour reprendre le contexte du projet rapidement (IDE / agents).
> **À mettre à jour à chaque évolution de code** (architecture, auth, routes, endpoints, sprint).
>
> Date d’analyse : **2026-07-28**.

---

## 0. Sprint & roadmap (pilotage)

| Champ | Valeur |
|-------|--------|
| **Sprint courant** | **MVP Phase 1 clôturé** — prochaine : Phase 2 S9 |
| **Phase** | Phase 1 — MVP ✅ (S1→S8 web + mobile livré sur `dev`) |
| **Branche d’intégration** | **`dev`** (source de vérité après merges web + mobile) |
| **Roadmap vivante** | [`docs/ROADMAP.md`](./ROADMAP.md) — **source de vérité** |

Après chaque feature / merge : mettre à jour `ROADMAP.md` (statut sprint, date, journal) **et** cette section / les endpoints ci-dessous.

> Convention agents **équipe web** : `api/web/` + `web/` + `api/shared/` (en accord). Ne pas modifier `api/mobile/` ni l’UI Flutter sauf coordination.

---

## 0.1 Branches Git

| Branche | Rôle |
|---------|------|
| **`dev`** | Intégration commune (web + mobile + docs) — **puller ici** pour tester l’interaction |
| **`web`** | Branche de travail équipe web (doit être realignée sur `dev` après chaque merge d’intégration) |
| **`mobile`** | Branche de travail équipe mobile |
| **`main`** | Releases / miroir stable (peut diverger temporairement) |

```bash
git fetch origin
git checkout dev && git pull origin dev   # récupérer l’intégration
# checkout seul ≠ pull : toujours pull après un merge annoncé sur GitHub
```

Guide mobile post-merge : [`guide-demarrage-mobile-apres-merge-dev.md`](./guide-demarrage-mobile-apres-merge-dev.md).

---

## 1. Vue d’ensemble

**FretCorridor** est une plateforme logistique multi-tenant (corridors Cameroun / CEMAC, B2B2G) : supervision d’axes, KYC, missions, tracking.

Le dépôt est un **monorepo** :

| Module | Chemin | Stack |
|--------|--------|-------|
| API | `backend/` | Spring Boot 3, Java 17, PostgreSQL, Redis, JWT, MinIO |
| Portail | `web/` | Angular 22, PWA, i18n FR/EN, signals |
| Mobile | `mobile/` | Flutter (chauffeur / agent) |
| Docs | `docs/` | CONTEXT, ROADMAP, gouvernance, guides |

Auth : **téléphone + code PIN** (`codePin`) → `{ accessToken, refreshToken, role, tenantId, configTenant }`.  
API préfixée `/api` (proxy web → `http://localhost:8080`).

Les deux clients (web + mobile) parlent à **une seule** API et **une seule** base — pas de communication directe app ↔ app.

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
Gouvernance web / mobile : [`analyse-integration-mobile.md`](./analyse-integration-mobile.md).  
Modèles Dart vs `backend/target/` : [`mobile-classes-et-backend.md`](./mobile-classes-et-backend.md).

### Web (`web/src/app/`) — résumé

- `core/` — auth, guards, interceptors, thème tenant
- `features/` — `auth`, `bureau` (axes + **missions**), `chargeur` (distribution + **offres**), `admin` (KYC, chauffeurs, missions, audit)
- `layout/shell/` — header logo + nav + outlet
- `shared/` — axes-explorer, corridor-map, mission-tracking-map (Leaflet), brand-logo, models, services
- Design : tokens `--fc-*`, classes `fc-*`, primaire marque `#d40f16`, Montserrat

---

## 3. Rôles

### Web (normalisation Angular)

`UserRole` : `BUREAU` · `CHARGEUR` · `ADMIN`

- Admin ← `ADMIN` / `BACK_OFFICE` / **`OPERATEUR`**
- Chargeur ← `CHARGEUR` / `SHIPPER` / `CLIENT`
- Bureau ← défaut (`AGENT`, etc.)

Homes : Admin → `/admin`, Chargeur → `/chargeur`, Bureau → `/bureau`.

### Qui fait quoi sur les missions (MVP)

| Étape | Acteur |
|-------|--------|
| Déclarer camion vide (créer l’offre) | **Chauffeur** (mobile) `POST /missions/declare-vide` |
| Accepter / démarrer / terminer / annuler | **Bureau** ou **chargeur** (web) — API `canTransitionMission` |
| Matching automatique | **Phase 2 S9** (stub mobile aujourd’hui) |

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

## 5. Endpoints API déjà livrés

### Shared
| Méthode | URL | Usage |
|---------|-----|--------|
| POST | `/api/auth/login` | `{ telephone, codePin }` |
| POST | `/api/auth/refresh` | refresh |
| POST | `/api/auth/logout` | logout |
| PUT | `/api/auth/fcm-token` | token FCM (mobile ; push réel différé) |
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
| PATCH | `/api/axes/{id}/activation` | GEO 3 flags (ou enum legacy ACTIF/VERROUILLE/INACTIF) |
| GET | `/api/admin/kyc/en-attente` | KYC pending |
| PUT | `/api/admin/kyc/{id}/valider` | valider KYC (+ audit) |
| GET | `/api/admin/audit` | journal d'audit |
| GET | `/api/admin/chauffeurs` | liste chauffeurs back-office |
| GET | `/api/chauffeurs/{id}` | profil chauffeur |

### Mobile
| Méthode | URL | Usage |
|---------|-----|--------|
| POST | `/api/chauffeurs` | enrôlement |
| GET | `/api/chauffeurs` | chauffeurs de l’agent |
| GET | `/api/chauffeurs/me` | profil chauffeur connecté |
| POST | `/api/kyc/documents` | upload KYC (MinIO) |
| POST | `/api/missions/declare-vide` | déclaration vide (idempotency) |
| GET/PUT/DELETE | `/api/missions/mes-declarations[/{id}]` | CRUD déclarations chauffeur |
| GET | `/api/missions/matchs` | stub matching (S9) |
| POST | `/api/positions` | S5 écriture GPS (batch / offline sync) |

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
# Racine monorepo — stack complète (recommandé pour tester web + mobile)
docker compose up --build          # Web :4200 · API :8080 · MinIO :9001

# Dev local (hot reload)
docker compose up -d postgres redis minio
cd backend && mvn spring-boot:run          # :8080
cd web && nvm use 22 && npm start         # :4200 (Node ≥ 22.22)

# Mobile (même API)
cd mobile
./scripts/run_dev.sh --emulator           # Android émulateur → 10.0.2.2:8080
./scripts/run_dev.sh                      # téléphone Wi‑Fi (IP LAN)
# USB : adb reverse tcp:8080 tcp:8080 + API_BASE=http://127.0.0.1:8080/api
```

Infra S8 : MinIO (`fretcorridor` / `fretcorridor123`, bucket `fretcorridor-kyc`) — console http://localhost:9001.

Comptes démo (PIN `1234`) :
- Agent bureau Cameroun `+237600000001` (tenant `BGFT_CM`) → web `/bureau` ou mobile agent
- Bureau BNFT Tchad `+235660000001` (tenant `BNFT_TD`)
- Opérateur `+237600000002` → web `/admin`
- Chargeur `+237600000003` → web `/chargeur`
- Chauffeur `+237600000010` → **mobile** (déclarations, profil KYC)

---

## 9. Prochaine étape code (Phase 2 / S9)

- Matching backhaul actif (PostGIS, scoring, activation par axe).
- Push FCM / WhatsApp réel — hors scope web actuel.
- GPS background mobile — optionnel (écriture positions déjà livrée).

Détail et checklist : **[`ROADMAP.md`](./ROADMAP.md)**.
