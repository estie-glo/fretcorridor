# Documentation FretCorridor

Ce dossier centralise la documentation transverse du monorepo (hors README propres à chaque module).

> Dernière mise à jour : **2026-07-28**

## Index (fichiers à jour)

| Fichier | Contenu |
|---------|---------|
| [`ROADMAP.md`](./ROADMAP.md) | Roadmap sprints S1→S15 (pilotage ; màj à chaque évolution) |
| [`CONTEXT.md`](./CONTEXT.md) | Contexte technique monorepo, branches, endpoints, comptes démo |
| [`backend-structure.md`](./backend-structure.md) | Packages Spring `api.web` / `api.mobile` / `api.shared` / `common` |
| [`analyse-integration-mobile.md`](./analyse-integration-mobile.md) | Gouvernance web / mobile / shared, plan démo |
| [`mobile-classes-et-backend.md`](./mobile-classes-et-backend.md) | Modèles Dart vs classes Java / `backend/target/` |
| [`guide-demarrage-mobile-apres-merge-dev.md`](./guide-demarrage-mobile-apres-merge-dev.md) | Checklist Flutter après merge sur `dev` (`pull` + API) |

## Branches

| Branche | Usage |
|---------|--------|
| `dev` | Intégration commune |
| `web` | Travail équipe web |
| `mobile` | Travail équipe mobile |

Toujours : `git fetch` puis `git pull origin <branche>` — un simple `checkout` ne récupère pas le remote.

## Conventions

- Fichiers en **Markdown** (`.md`), nommage kebab-case.
- Diagrammes : **Mermaid** inline ou exports dans `docs/assets/`.
- En-tête daté : `> Dernière mise à jour : AAAA-MM-JJ`.

## Ne pas déposer ici

- Code source (`backend/`, `mobile/`, `web/`)
- Secrets (`.env`, clés JWT, mots de passe)
- Artefacts de build (`target/`, `dist/`, `build/`)
- Skills / outils locaux agent (`.claude/`)
