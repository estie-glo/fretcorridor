# Documentation FretCorridor

Ce dossier centralise la documentation transverse du monorepo (hors README propres à chaque module).

## Contenu recommandé

| Type | Exemples | Audience |
|------|----------|----------|
| **Architecture** | Diagrammes C4, flux auth, modèle multi-tenant | Développeurs |
| **API** | Spécifications OpenAPI, exemples curl, codes d'erreur | Front + mobile + intégrateurs |
| **Décisions** | ADR (Architecture Decision Records) | Équipe technique |
| **Déploiement** | Runbooks, variables d'environnement, CI/CD | DevOps |
| **Métier** | Glossaire corridor, rôles (chauffeur, agent, chargeur) | Product + dev |

## Conventions

- Fichiers en **Markdown** (`.md`), nommage kebab-case : `auth-flow.md`, `adr-001-multi-tenant.md`.
- Diagrammes : préférer **Mermaid** inline ou exports SVG/PNG dans `docs/assets/`.
- Versions datées dans le titre ou un en-tête : `> Dernière mise à jour : 2026-07-20`.

## Fichiers existants

- `CONTEXT.md` — contexte technique du portail web (Angular), maintenu pour reprise rapide par IDE/agents.

## Ne pas déposer ici

- Code source (`backend/`, `mobile/`, `web/`)
- Secrets (`.env`, clés JWT, mots de passe)
- Artefacts de build (`target/`, `dist/`, `build/`)
