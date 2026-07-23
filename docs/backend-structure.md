# Organisation backend — séparation web / mobile

> Dernière mise à jour : 2026-07-21  
> Suite des sprints : [`ROADMAP.md`](./ROADMAP.md)

## Pourquoi cette structure ?

Le monorepo a trois clients (`web/`, `mobile/`, `backend/`). Pour éviter que les équipes écrivent toutes les APIs dans le même dossier `controller/`, la couche HTTP est découpée :

```
api.shared  →  endpoints communs (auth, GEO axes, hubs, tracking lecture…)
api.web     →  portail Angular
api.mobile  →  app Flutter
common      →  métier, persistance, sécurité (partagé)
```

Ce n’est **pas** deux backends : une seule app Spring Boot sur le port `8080`.

## Convention pour les prochains sprints

| Besoin | Où coder |
|--------|----------|
| Écran Angular a besoin d’un nouvel endpoint | `api/web/` (+ service dans `common/service` si métier nouveau) |
| Écran Flutter a besoin d’un nouvel endpoint | `api/mobile/` |
| Endpoint utile aux deux | `api/shared/` |
| Nouvelle table / règle métier | `common/entity` + `repository` + `service` |

## Déjà livré (ne pas re-planifier)

- Hubs `GET /api/hubs`, axes enrichis, `PATCH /api/axes/{id}/activation`
- Missions bureau `GET /api/missions`, `GET /api/missions/{id}`
- Tracking / ETA lecture `GET /api/missions/{id}/tracking|eta`
- Missions mobile : `POST /declare-vide`, `GET /matchs`, `POST /positions` (S5 écriture)
- Chauffeur mobile : `GET /chauffeurs/me`, `POST /kyc/documents`

## Suite (ordre strict — voir ROADMAP)

Sprint courant : **MVP Phase 1 clôturé** (S8 fait). Prochaine : Phase 2 S9 matching.  
S5 mobile (`POST /positions`) différé.
