# Mobile — où sont les classes et le lien avec `backend/target/`

> Dernière mise à jour : 2026-07-23

## Réponse courte

**Non** — le mobile **n'utilise pas** le dossier `backend/target/`.

`target/` contient uniquement les artefacts de compilation Maven (`.class`, JAR, rapports de tests). C'est un build local, **ignoré par Git**, jamais consommé par Flutter.

---

## Ce qu'est `backend/target/`

| Aspect | Détail |
|--------|--------|
| **Rôle** | Sortie de compilation Maven du backend Java |
| **Contenu** | Fichiers `.class`, JAR, rapports de tests |
| **Versionné ?** | Non — listé dans `.gitignore` (`backend/target/`) |
| **Utilisé par le mobile ?** | Non |

---

## Où sont les « classes » côté mobile

Le mobile Flutter possède **ses propres modèles Dart**, écrits à la main :

```
mobile/lib/models/
├── axe_model.dart
├── chauffeur_model.dart
├── declaration_vide_model.dart
├── mission_model.dart
├── notification_model.dart
├── position_pending_model.dart
└── utilisateur_model.dart
```

Ils mappent le **JSON** renvoyé par l'API via `fromJson()` / `toJson()`.

Exemple (`chauffeur_model.dart`) :

```dart
class ChauffeurModel {
  final String id;
  final String nom;
  // ...

  factory ChauffeurModel.fromJson(Map<String, dynamic> json) {
    return ChauffeurModel(
      id:  json['id'] ?? '',
      nom: json['nom'] ?? '',
      // ...
    );
  }
}
```

Il n'y a **pas de génération automatique** depuis les classes Java du backend.

---

## Où sont les classes backend consommées par le mobile (via HTTP)

Le mobile ne charge pas de classes Java : il appelle l'API REST (`/api/...`) et reçoit du JSON.

| Couche | Emplacement source | Rôle |
|--------|-------------------|------|
| **Endpoints mobile** | `backend/src/main/java/com/flysoft/fretcorridor/api/mobile/` | Chauffeur, missions, positions, camions… |
| **Endpoints partagés** | `backend/src/main/java/com/flysoft/fretcorridor/api/shared/` | Auth, axes, hubs, notifications, tracking |
| **DTOs JSON** | `backend/src/main/java/com/flysoft/fretcorridor/common/dto/` | `ChauffeurDto`, `AxeDto`, `MissionDto`… |
| **Entités / métier** | `backend/src/main/java/com/flysoft/fretcorridor/common/entity/` + `service/` | Persistance et logique |

### Controllers mobile (`api/mobile/`)

- `ChauffeurController.java` — profil chauffeur, KYC upload, `GET /chauffeurs/me`
- `MissionController.java` — déclaration vide, matchs
- `PositionController.java` — `POST /positions` (tracking S5)
- `CamionController.java`, `TransporteurController.java`

### Endpoints partagés utilisés par le mobile (`api/shared/`)

- `AuthController` — login, refresh JWT
- `AxeController` — liste des axes
- `HubController` — hubs
- `NotificationController` — notifications
- `TrackingController` — lecture tracking / ETA

---

## Schéma simplifié

```
Backend (Java)                         Mobile (Flutter)
─────────────────                      ──────────────────
src/.../api/mobile/*.java    ──HTTP──►  lib/models/*.dart
src/.../common/dto/*.java    ──JSON──►  lib/providers/*.dart
src/.../common/service/*.java          lib/services/*.dart

target/*.class  ← build Maven local, jamais utilisé par le mobile
```

---

## Règle de maintenance

Si un champ est modifié côté backend (DTO ou entité exposée en JSON), il faut **aligner manuellement** le modèle Dart correspondant dans `mobile/lib/models/`.

Pas de synchronisation automatique entre :

- `backend/.../common/dto/*.java`
- `mobile/lib/models/*_model.dart`

---

## Voir aussi

- [`backend-structure.md`](./backend-structure.md) — organisation `api/web`, `api/mobile`, `api/shared`, `common`
- [`analyse-integration-mobile.md`](./analyse-integration-mobile.md) — gouvernance monorepo web / mobile / shared
- [`mobile/README.md`](../mobile/README.md) — structure Flutter et configuration API
