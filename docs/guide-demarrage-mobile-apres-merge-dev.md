# Guide mobile — récupérer `dev` et lancer l’app (post-merge)

> **Public :** développeur·euse Flutter (équipe mobile)  
> **Contexte :** le code mobile récent (déclarations vide, « Mes déclarations », CRUD API) est intégré sur la branche **`dev`**, pas automatiquement sur toutes les branches locales.

---

## Idée importante

| Action | Effet |
|--------|--------|
| `flutter run` | Compile le code **présent dans votre dossier** `mobile/lib/` (état Git local). |
| `docker compose up` | Démarre le **backend** (API `:8080`, base PostgreSQL, MinIO, etc.). **Ne met pas à jour le code Flutter.** |

**Voir une « vieille » interface** après un merge sur `dev` vient en général d’un **mauvais checkout Git**, d’un **`pull` manquant**, ou d’un **cache Flutter / ancienne install** sur l’émulateur — pas du fait d’avoir omis Docker.

Sans backend sur le port **8080**, l’app peut afficher les **nouveaux écrans** mais les **appels API** (login, listes, sync) échouent ou semblent vides : ce n’est pas forcément l’ancienne version.

---

## Checklist — avant `flutter run`

Exécuter depuis la **racine du monorepo** `fretcorridor` (pas une copie dans `annexes/`, pas un ancien clone).

### 1. Être sur `dev` et à jour

```bash
cd fretcorridor
git fetch origin
git checkout dev
git pull origin dev
git log -1 --oneline
```

Vous devez être **aligné avec `origin/dev`** (`git status` → « à jour avec origin/dev »).  
Un simple `git checkout dev` **ne récupère pas** les commits distants : toujours `git pull origin dev` après un merge annoncé.

### 2. Vérifier que le bon code mobile est bien là

```bash
grep -n "Mes déclarations" mobile/lib/screens/declaration_vide_screen.dart
```

- **Une ligne trouvée** → bonne version UI (écran enrichi).
- **Rien** → vous n’êtes pas sur le bon commit/branche : refaire l’étape 1 (ne pas rester sur `mobile` locale figée à l’ancien pivot `737b97c`).

Contrôle optionnel :

```bash
wc -l mobile/lib/screens/declaration_vide_screen.dart
```

Environ **600+** lignes sur `dev` à jour ; environ **270** lignes sur l’ancienne version.

### 3. Nettoyer le build Flutter

```bash
cd mobile
flutter clean
flutter pub get
```

Sur l’émulateur ou le téléphone : désinstaller l’ancienne app **FretCorridor** si le comportement reste bizarre, puis relancer.

---

## Lancer le backend (recommandé pour tester les fonctionnalités)

Sur la machine qui héberge l’API (souvent le même PC que le dev) :

```bash
cd fretcorridor
docker compose up -d
# ou, après changement backend : docker compose up --build -d
```

| Service | URL |
|---------|-----|
| API | http://localhost:8080/api |
| Web (optionnel, équipe web) | http://localhost:4200 |
| MinIO (KYC) | http://localhost:9001 |

Endpoints mobile ajoutés récemment (exemples) :

- `GET /api/missions/mes-declarations`
- `GET/PUT/DELETE /api/missions/mes-declarations/{id}`
- (déjà présents) `POST /api/missions/declare-vide`, `GET /api/chauffeurs/me`, etc.

---

## Lancer l’app Flutter (URL API correcte)

**Ne pas** se limiter à `flutter run` sans config réseau : utiliser le script du repo.

```bash
cd mobile
chmod +x scripts/run_dev.sh    # une seule fois si besoin

./scripts/run_dev.sh --emulator    # émulateur Android → API http://10.0.2.2:8080/api
./scripts/run_dev.sh               # téléphone physique (Wi‑Fi) → IP LAN du PC + :8080
```

Valeur manuelle si besoin :

```bash
flutter run --dart-define=API_BASE=http://VOTRE_IP:8080/api
```

Le backend doit écouter sur **0.0.0.0:8080** (cas Docker : OK).

**Défaut sans `--dart-define` :** `http://127.0.0.1:8080/api` — adapté simulateur iOS / desktop, souvent **incorrect sur téléphone physique** (échecs réseau, pas « ancienne app »).

---

## Comptes démo (PIN `1234`)

| Rôle | Téléphone | Usage |
|------|-----------|--------|
| Chauffeur | `+237600000010` | Dashboard chauffeur, déclarations, profil KYC |
| Agent | `+237600000001` | Enrôlement, modération (app mobile agent) |

Comptes **web** (navigateur `:4200`) : opérateur `+237600000002`, chargeur `+237600000003`, etc. — voir `docs/CONTEXT.md`.

---

## Pièges fréquents

| Symptôme | Cause probable | Action |
|----------|----------------|--------|
| UI sans « Mes déclarations » | Branche `mobile` locale ou `main`/`web` non mergée, ou pas de `pull` sur `dev` | `checkout dev` + `pull` + grep ci-dessus |
| UI correcte mais login / listes KO | Pas de backend (`docker compose` ou `mvn spring-boot:run`) | Démarrer stack, vérifier `:8080` |
| Téléphone ne joint pas l’API | `127.0.0.1` au lieu de l’IP du PC | `./scripts/run_dev.sh` sans `--emulator` |
| Ancien comportement malgré bon Git | Cache / ancienne APK | `flutter clean`, désinstaller l’app, réinstaller |
| Code backend obsolète dans Docker | Image non rebuild | `docker compose up --build -d` |

---

## Branches Git (rappel monorepo)

| Branche | Contenu typique |
|---------|-----------------|
| **`dev`** | Intégration web + mobile (source de vérité après merge) |
| **`mobile`** | Branche de travail mobile — peut être **en retard** si non mise à jour depuis `dev` |
| **`web`** | Branche équipe web — **ne contient pas** automatiquement le dernier `mobile/` |

**Après un merge sur `dev` :** travailler depuis `dev` ou faire `git pull origin dev` puis merger dans sa branche `mobile`.

---

## Scénario de test d’interaction (une machine)

1. `docker compose up -d` (API + base partagées).
2. Mobile : `./scripts/run_dev.sh --emulator` — chauffeur ou agent.
3. Web (optionnel) : http://localhost:4200 — opérateur pour valider KYC après enrôlement mobile.

Web et mobile utilisent la **même API** et la **même base** : pas besoin de deux PC pour valider l’intégration.

---

## Voir aussi

- [`mobile/README.md`](../mobile/README.md) — config `API_BASE`, structure du projet
- [`mobile-classes-et-backend.md`](./mobile-classes-et-backend.md) — modèles Dart vs backend Java
- [`analyse-integration-mobile.md`](./analyse-integration-mobile.md) — périmètres web / mobile / shared
