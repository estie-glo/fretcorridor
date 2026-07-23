# FretCorridor — Mobile

Application **Flutter** pour chauffeurs et agents terrain (Android / iOS).

## Stack

- Flutter 3.x, Dart
- Riverpod (state management)
- Dio (HTTP + intercepteur refresh JWT)
- flutter_secure_storage (tokens)

## Prérequis

- Flutter SDK (`flutter doctor`)
- Android Studio / Xcode selon la cible
- Backend API en cours d'exécution (voir `../backend/README.md`)

## Démarrage rapide

```bash
cd mobile
flutter pub get
flutter run              # émulateur / appareil par défaut
flutter run -d chrome    # test rapide web
```

## Configuration réseau

L'URL de l'API est configurée **à la compilation** via `--dart-define=API_BASE=…`  
(voir `lib/core/config/api_config.dart`).

| Contexte | Commande |
|----------|----------|
| **Appareil physique** (WiFi = machine dev) | `./scripts/run_dev.sh` |
| **Émulateur Android** | `./scripts/run_dev.sh --emulator` |
| **Valeur manuelle** | `flutter run --dart-define=API_BASE=http://192.168.x.x:8080/api` |
| **Défaut sans define** | `http://127.0.0.1:8080/api` (simulateur iOS / desktop) |

```bash
cd mobile
chmod +x scripts/run_dev.sh scripts/update_ip.sh   # une seule fois
./scripts/run_dev.sh                               # détecte l'IP et lance l'app
./scripts/run_dev.sh --emulator                    # 10.0.2.2 → host local
```

> `update_ip.sh` est déprécié (redirige vers `run_dev.sh`). Ne plus éditer `dio_provider.dart` à la main.

Le backend doit écouter sur `0.0.0.0:8080` (voir `../backend/README.md`).

### Build release

```bash
flutter build apk --dart-define=API_BASE=https://api.votre-domaine.com/api
```

## Structure

```
mobile/
├── lib/
│   ├── core/config/  # ApiConfig (dart-define API_BASE)
│   ├── main.dart
│   ├── models/       # DTOs (utilisateur, axe, chauffeur…)
│   ├── providers/    # Riverpod
│   ├── screens/      # UI (login, dashboards, déclarations…)
│   ├── services/     # Auth, GPS, SQLite local
│   └── theme/
├── android/
├── ios/
└── pubspec.yaml
```

## Fonctionnalités implémentées

- Authentification téléphone + PIN, refresh automatique
- Dashboard agent : enrôlement, modération KYC
- Dashboard chauffeur : axes, déclaration vide, profil KYC (upload MinIO), notifications, matchs (stub), tracking GPS (S5)
- Déclarations vide & positions GPS : mode offline-first (SQLite + sync)

## Build release

```bash
flutter build apk        # Android
flutter build ios        # iOS (macOS requis)
```
