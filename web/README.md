# FretCorridor — Web

Portail web **Angular 22** (SPA multi-tenant) pour bureaux, chargeurs et administration.

## Stack

- Angular 22 (standalone, signals)
- SCSS, `@ngx-translate` (FR/EN)
- PWA (`@angular/service-worker`)
- Proxy dev vers l'API Spring Boot

## Prérequis

- Node.js 20+ et npm 10+
- Backend sur `http://localhost:8080`

## Démarrage rapide

```bash
cd web
npm install
npm start
```

Application disponible sur **http://localhost:4200** — les appels `/api/*` sont proxifiés vers le backend.

## Scripts

| Commande | Description |
|----------|-------------|
| `npm start` | Serveur de dev avec proxy |
| `npm run build` | Build production |
| `npm run watch` | Build watch (dev) |
| `npm test` | Tests Vitest |

## Structure

```
web/
├── src/app/
│   ├── core/         # Auth, guards, interceptors, thème tenant
│   ├── features/     # auth, bureau, chargeur, admin
│   ├── layout/       # Shell, navigation
│   └── shared/       # Composants, modèles, services
├── public/i18n/      # Traductions
├── angular.json
└── proxy.conf.json
```

## Créer le projet from scratch (référence)

Si vous repartez d'un dossier vide :

```bash
cd ..
ng new web --routing --style=scss --ssr=false
cd web
ng add @angular/service-worker
npm install @ngx-translate/core @ngx-translate/http-loader
```

Puis brancher le proxy (`proxy.conf.json`) et les modules métier selon `../docs/CONTEXT.md`.

## Rôles UI

- **Bureau** — exploration des axes corridor
- **Chargeur** — distribution
- **Admin** — KYC chauffeurs, gestion flotte
