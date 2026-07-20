# CONTEXT.md — fretcorridor-web

> Document de référence maintenu pour permettre à n'importe quel IDE/agent de reprendre le contexte du projet rapidement.
> Date d'analyse : 2026-07-20. À mettre à jour lors des évolutions majeures (architecture, auth, routes, modèles API).

---

## 1. Vue d'ensemble

**FretCorridor** est un **portail web logistique multi-tenant** (frontoffice d'un système de gestion de « corridor » fret, contexte Afrique/Cameroun — indicatif `+237`). L'application est un client lourd (SPA) Angular qui consomme une API REST Spring-like (non incluse dans ce dépôt) servie derrière le préfixe `/api`.

| Item | Valeur |
|---|---|
| Stack | **Angular 22** (composants standalone, signals, nouveaux control flow `@if/@for`) |
| Langage | TypeScript 6, SCSS |
| Internationalisation | `@ngx-translate/core` 18 + `http-loader` (FR défaut, EN dispo) |
| PWA | `@angular/service-worker` + `manifest.webmanifest` (installable, offline) |
| State | **Angular Signals** (`signal`, `computed`, `effect`) — pas de NgRx |
| Forms | Reactive Forms (`FormBuilder`) |
| Tests | Vitest (builder `@angular/build:unit-test`) — quasi absent (1 spec racine) |
| Formatting | Prettier (100 cols, single quote, parser `angular` pour HTML) |
| Package manager | npm 10.9.8 |
| Branche principale (PRs) | `main` — branche courante `master` |

### Scripts npm
```
npm start    # ng serve --proxy-config proxy.conf.json  (dev server :4200)
npm run build# ng build (production par défaut)
npm run watch# ng build --watch --configuration development
npm test     # ng test (Vitest)
```

### Backend attendu
- API REST préfixée `/api` → proxy vers `http://localhost:8080` en dev (`proxy.conf.json`).
- Auth par **téléphone + code PIN** renvoyant `{ accessToken, refreshToken, role, tenantId, configTenant }`.
- Réponses de collections **flexibles** : le front parse plusieurs formes (`content` / `data` / `items` / tableaux nus…) via `collection-parser.ts`. Le schéma backend est donc tolérant / évolutif.

---

## 2. Architecture des dossiers

```
src/
├── main.ts                         # bootstrapApplication(App, appConfig)
├── index.html                      # <app-root> + manifest
├── styles.scss                     # CSS reset + variables globales --fc-*
├── environments/
│   ├── environment.ts              # dev :  { production:false, apiUrl:'/api' }
│   └── environment.prod.ts         # prod : { production:true,  apiUrl:'/api' }
└── app/
    ├── app.config.ts               # providers (router, http, translate, SW, init)
    ├── app.routes.ts               # routes racines + fabrique createShellRoute()
    ├── app.ts                      # composant racine (juste <router-outlet/>)
    ├── core/                       #基础设施 : auth, guards, interceptors, theme
    │   ├── auth/                   # AuthService + TokenStorageService
    │   ├── guards/                 # auth, guest, role guards
    │   ├── interceptors/           # auth.interceptor (Bearer + refresh 401)
    │   ├── init/                   # provideAuthInit, provideTenantTheme
    │   └── theme/                  # TenantThemeService (couleur primaire dynamique)
    ├── features/                   # fonctionnalités par rôle
    │   ├── auth/                   # /login (+ guestGuard)
    │   ├── bureau/                 # /bureau → axes corridor
    │   ├── chargeur/               # /chargeur → distribution
    │   └── admin/                  # /admin → KYC + chauffeurs (back-office)
    ├── layout/                     # chrome applicatif
    │   ├── shell/                  # ShellComponent + ShellNavComponent
    │   └── home/                   # page d'accueil placeholder
    └── shared/                     # réutilisable
        ├── brand/                  # brand.constants.ts (logo)
        ├── components/             # axes-explorer, brand-logo, language-switcher
        ├── models/                 # DTOs + parsers défensifs (axe, chauffeur, kyc, tenant)
        ├── services/               # AxesService
        └── utils/                  # collection-parser, record-display

public/                             # assets statiques copiés tels quels à la racine
├── i18n/{fr,en}.json               # traductions
├── icons/icon-*.png                # icônes PWA
├── manifest.webmanifest
└── favicon.ico
assets/logo.png                     # logo marque (servi /assets/logo.png)
```

> Convention : `public/*` est servi à la racine (`/`), tandis que `src/assets/*` est servi sous `/assets/`. Le logo vit dans `src/assets/` (`assets/logo.png`), les i18n et icônes dans `public/`.

---

## 3. Configuration applicative (`app.config.ts`)

Providers, dans l'ordre :
1. `provideBrowserGlobalErrorListeners()`
2. `provideRouter(routes)`
3. `provideHttpClient(withInterceptors([authInterceptor]))`
4. `provideTranslateService({ fallbackLang: 'fr' })` + `provideTranslateHttpLoader({ prefix:'/i18n/', suffix:'.json' })`
5. `provideAuthInit()` — `provideAppInitializer` qui appelle `AuthService.initialize()` (restauration de session)
6. `provideTenantTheme()` — instancie `TenantThemeService` (qui s'auto-abonne via `effect`)
7. `provideServiceWorker('ngsw-worker.js', { enabled: !isDevMode(), registrationStrategy:'registerWhenStable:30000' })`

---

## 4. Authentification & sécurité

### Flux
- **Login** : `POST /api/auth/login` avec `{ telephone, pin }` → `AuthResponse`.
- Le **refresh token** est stocké en `localStorage` (`fc_refresh_token`) via `TokenStorageService`.
- Le **access token** est gardé **en mémoire** uniquement (variable privée `AuthService.accessToken`) — il ne survit pas à un rechargement ; c'est le refresh token qui restaure la session au démarrage.
- Au bootstrap, `provideAuthInit` → `AuthService.initialize()` → `restoreSession()` → si refresh token présent, tente `refreshSession()` (sinon rien).
- `refreshPromise` est dédupliqué pour éviter les appels parallèles.
- **Logout** : `POST /api/auth/logout` (best-effort, errors swallowées), puis `clearSession()`.

### Intercepteur HTTP (`auth.interceptor.ts`)
- N'attache le header `Authorization: Bearer <token>` que sur les requêtes vers l'API (`/api` ou `environment.apiUrl`).
- Ignore les endpoints d'auth eux-mêmes (`/auth/login`, `/auth/refresh`, `/auth/logout`).
- Sur **401** (hors endpoints auth) : tente un `refreshSession()` unique, puis rejoue la requête initiale avec le nouveau token. Sinon propage l'erreur.

### Signaux exposés par `AuthService`
`role`, `tenantId`, `tenantConfig`, `isAuthenticated`, `isLoading`, `initComplete`, `normalizedRole` (computed). Méthodes : `login`, `logout`, `getAccessToken`, `waitForInit`, `initialize`, `refreshSession`, `getHomeRoute`, `hasRole`, `clearSession`.

---

## 5. Rôles & routage

### Énum `UserRole` (`shared/models/user-role.enum.ts`)
`Bureau = 'BUREAU'` · `Chargeur = 'CHARGEUR'` · `Admin = 'ADMIN'`

`normalizeRole()` ramène n'importe quelle chaîne backend à un `UserRole` via patterns :
- Admin ← contient `ADMIN` / `BACK_OFFICE` / `BACKOFFICE`
- Chargeur ← contient `CHARGEUR` / `SHIPPER` / `CLIENT`
- Bureau ← défaut

`getHomeRouteForRole()` : Admin→`/admin`, Chargeur→`/chargeur`, Bureau→`/bureau`.

### Guards
| Guard | Rôle |
|---|---|
| `authGuard` | attend l'init ; si non auth → `/login` |
| `guestGuard` | si déjà auth → redirige vers `getHomeRoute()` (protège `/login`) |
| `roleGuard(roles[])` | factory ; redirige vers la home du user si rôle non autorisé |

### Table de routage (`app.routes.ts`)
```
/login                              LoginComponent        [guestGuard]
/bureau        (Shell: Bureau)      bureauRoutes         [authGuard, roleGuard([Bureau])]
/chargeur      (Shell: Chargeur)    chargeurRoutes       [authGuard, roleGuard([Chargeur])]
/admin         (Shell: Admin)       adminRoutes          [authGuard, roleGuard([Admin])]
''                                  → redirectTo /login
**                                  → redirectTo /login
```
Le helper `createShellRoute(roles, children)` enveloppe chaque section protégée dans `ShellComponent` (header + nav + `<router-outlet>`).

Sous-routes admin : `'' → kyc`, `kyc`, `chauffeurs`.
Sous-routes bureau & chargeur : `''` → composant unique.

---

## 6. Fonctionnalités par rôle

### Bureau (`/bureau`) — `AxesListComponent`
Wrappé autour du composant partagé `<app-axes-explorer scope="AXES" />`.

### Chargeur (`/chargeur`) — `ChargeurDistributionComponent`
`<app-axes-explorer scope="CHARGEUR" />` (mêmes données axes, libellés i18n différents).

### Admin (`/admin`)
- **`AdminPageComponent`** : layout avec nav interne (KYC / Chauffeurs) + `<router-outlet>`.
- **KYC en attente** (`KycPendingListComponent`) : liste gauche + détail droite (entries clé/valeur), bouton **Valider** (`PUT /api/admin/kyc/{id}/valider`). Après validation, retire l'item de la liste et affiche un banner succès.
- **Chauffeurs** (`ChauffeursListComponent`) : liste + détail via `GET /api/chauffeurs/{id}`.

### Composant clé — `AxesExplorerComponent` (`shared/components/axes-explorer`)
Réutilisable, piloté par `input.scope` (préfixe i18n). Charge `GET /api/axes`, au clic charge `GET /api/axes/{id}/statut`, affiche le statut en liste `dl/dt/dd`. Pattern master-detail commun à plusieurs écrans.

---

## 7. Modèles & parsing défensif

Le backend ayant un schéma souple, **les modèles ne typent pas fortement** : chaque entité = `{ id: string, raw: Record<string,unknown> }` (`EntitySummary`). Le parsing sélectionne dynamiquement la clé d'id et de libellé.

| Modèle | Endpoints / clés |
|---|---|
| `auth.dto.ts` | `LoginRequest`, `AuthResponse`, `RefreshRequest` |
| `user-role.enum.ts` | `UserRole` + `normalizeRole`, `getHomeRouteForRole` |
| `tenant-config.model.ts` | `TenantConfig` + helpers `getTenantDisplayName`, `getTenantPrimaryColor`, `getResolvedPrimaryColor` (défaut `#0f7a4a`), validation CSS color |
| `axe.model.ts` | `AxeSummary`, parsers axes/statut, label keys `nom/name/libelle/...` |
| `chauffeur.model.ts` | `ChauffeurSummary`, parsers chauffeurs |
| `kyc.model.ts` | `KycSummary`, parser KYC en attente |

**Utils** :
- `collection-parser.ts` — `extractCollection`, `parseEntityItem`, `parseEntityCollection`, `getEntityDisplayLabel`, `parseEntityRecord` (génériques).
- `record-display.ts` — `toRecordEntries(record)` → trie alphabétiquement, formatte scalaires/Stringify JSON, `—` pour null/undefined.

---

## 8. Thème multi-tenant

`TenantThemeService` (auto-instancié via `provideTenantTheme`) utilise un `effect` sur `AuthService.tenantConfig()`. À chaque changement, il pose sur `:root` :
- `--fc-primary` = couleur primaire du tenant (ou `#0f7a4a`)
- `--fc-primary-contrast` = `#ffffff`

### Variables CSS globales (`src/styles.scss`)
```css
--fc-bg, --fc-surface, --fc-text, --fc-muted, --fc-border,
--fc-primary, --fc-primary-contrast, --fc-danger
```
Toutes les feuilles de composants utilisent ces variables → **re-thématisation instantanée par tenant**.

---

## 9. i18n (FR/EN)

- Fichiers `public/i18n/{fr,en}.json` (chargés via HTTP loader).
- FR = langue par défaut et fallback.
- Préférence stockée en `localStorage` clé `fc_lang` (`LanguageSwitcherComponent`).
- Composant `app-language-switcher` présent sur `/login` et dans le shell.
- Sections de clés : `APP`, `AUTH`, `HOME`, `AXES`, `CHARGEUR`, `NAV`, `ADMIN`, `KYC`, `CHAUFFEURS`, `LANG`.

> Convention : `AxesExplorerComponent.translationKey(suffix)` génère `<scope>.<SUFFIX>` — `scope` vaut `AXES` ou `CHARGEUR`.

---

## 10. PWA

- `manifest.webmanifest` : `name`/`short_name` = `fretcorridor-web`, `display: standalone`, icônes 72→512px (`public/icons/`).
- Service worker : `ngsw-worker.js` activé **en prod uniquement**, stratégie `registerWhenStable:30000`.
- `ngsw-config.json` : groupe `app` (prefetch : index, manifest, CSS/JS) + groupe `assets` (lazy install, prefetch update — images/polices).
- Référence dans `index.html` : `<link rel="manifest" href="manifest.webmanifest">`.

---

## 11. Conventions de code

- **Standalone components partout** (pas de NgModule). Imports déclarés dans chaque `@Component.imports`.
- **Signals pour l'état** : `signal()`, `computed()`, `effect()` — pas de `BehaviorSubject` dans l'app.
- **Nouveau control flow** Angular : `@if`, `@for (... track ...)`, `@else` (présent dans tous les templates).
- **Injection** : `inject()` exclusivement (pas de constructeur-injection de services, sauf `TenantThemeService` qui utilise un `effect` en constructeur).
- **Fichiers composants** sans suffixe (Angular 22) : `app.ts`, `login.component.ts`, etc. Sélecteurs préfixés `app-`.
- **Styles** : SCSS, par composant (`styleUrl`/`styles`), BEM-like (`.shell__header`, `.kyc-list__item--active`).
- **Prettier** : `printWidth:100`, single quotes, parser `angular` sur `*.html`.
- TypeScript strict (Angular) : `noImplicitOverride`, `noPropertyAccessFromIndexSignature`, `noImplicitReturns`, `noFallthroughCasesInSwitch`.
- Préfixe i18n toujours passé via `translate` pipe (`{{ 'KEY' | translate }}`).

---

## 12. Build & budgets

`angular.json` : builder `@angular/build:application`, config prod par défaut.
- **Budgets prod** : bundle initial warning >500kB / error >1MB ; style de composant warning >4kB / error >8kB.
- `fileReplacements` prod : `environment.ts` → `environment.prod.ts`.
- `outputHashing: all`, service worker activé.

---

## 13. Endpoints API consommés

| Méthode | URL | Usage |
|---|---|---|
| POST | `/api/auth/login` | connexion (telephone + pin) |
| POST | `/api/auth/refresh` | renouvellement access token |
| POST | `/api/auth/logout` | déconnexion |
| GET | `/api/axes` | liste des axes corridor |
| GET | `/api/axes/{id}/statut` | statut d'un axe |
| GET | `/api/chauffeurs` | liste chauffeurs |
| GET | `/api/chauffeurs/{id}` | détail chauffeur |
| GET | `/api/admin/kyc/en-attente` | KYC en attente de validation |
| PUT | `/api/admin/kyc/{id}/valider` | valider un KYC |

---

## 14. État actuel & points d'attention

- **3 commits** : `Initial commit from Create Next App` → `first commit` → `version finale`. (Le 1er commit mentionne Next App mais le projet est Angular — le README/AGENTS global Next.js ne s'applique **pas**.)
- **Tests quasi inexistants** : un seul `app.spec.ts` (création du composant racine). Pas de tests sur services/guards/components.
- **`HomeComponent`** = placeholder (« Dashboard à venir »), non routé explicitement.
- **`BrandLogoComponent`** existe mais n'est pas utilisé dans les templates actuels (header du shell affiche `APP.TITLE` en texte). Logo dispo : `assets/logo.png`.
- **CORS** : géré côté front via proxy dev uniquement (`/api` → `localhost:8080`). En prod, l'API doit être servie sur le même domaine/origine `/api`.
- **Stockage sensible** : refresh token en `localStorage` (risque XSS à évaluer selon politique sécurité).
- **Aucun `.env`** : tout passe par `environment.{,prod.}ts` (les deux pointent sur `/api`).
- `tsconfig.spec.json` référencé mais Vitest configuré via `@angular/build:unit-test`.

---

## 15. Démarrage rapide (pour un nouvel IDE/agent)

```bash
cd fretcorridor-web
npm install                       # si node_modules absent
npm start                         # dev server http://localhost:4200
# Backend attendu sur http://localhost:8080 (préfixe /api)
npm run build                     # build prod → dist/
npm test                          # Vitest
```

Pour ajouter un écran : créer un composant standalone dans la `feature` concernée, l'enregistrer dans son `*.routes.ts`, ajouter les clés i18n dans **les deux** `public/i18n/{fr,en}.json`, et réutiliser `AxesExplorerComponent` ou le pattern master-detail (signals + `toRecordEntries`) si pertinent.
