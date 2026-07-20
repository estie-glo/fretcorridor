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

Mettre à jour l'URL de l'API dans les services (`lib/services/`) ou via le script `update_ip.sh` pour pointer vers la machine de dev.

## Structure

```
mobile/
├── lib/
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
- Dashboards chauffeur / agent
- Axes corridor, profil chauffeur
- Déclarations vide, GPS, mode offline (SQLite)

## Build release

```bash
flutter build apk        # Android
flutter build ios        # iOS (macOS requis)
```
