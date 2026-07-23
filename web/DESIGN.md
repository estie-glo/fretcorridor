# Design

## Visual Theme

Product UI logistique — clair, dense utile, identité FretCorridor (blanc / rouge `#d40f16` / noir). Accent rouge réservé aux actions et états actifs.

## Color Palette

| Token | Valeur | Usage |
|-------|--------|--------|
| `--fc-bg` | `#f5f5f6` | Fond page |
| `--fc-surface` | `#ffffff` | Panneaux, header |
| `--fc-text` | `#0a0a0a` | Texte principal |
| `--fc-muted` | `#52525b` | Secondaire |
| `--fc-primary` | `#d40f16` | CTA, sélection, accents |
| `--fc-success` | `#067647` | Succès |
| `--fc-danger` | `#b42318` | Erreurs |

## Typography

Famille unique : **Montserrat** (500–800). Échelle produit fixe (pas de clamp fluide). Titres page ~1.375rem, labels 0.8125rem.

## Components

Classes globales `fc-*` dans `src/styles.scss` : page, split master-detail, panel, list, dl, btn, empty, banner, skeleton.

## Layout

Shell sticky (logo + tenant + actions) + nav horizontale + contenu max ~72rem. Master-detail 20rem / 1fr, empilé sous 860px.

## Responsive

Breakpoints : **860px** (split master-detail empilé), **720px** (nav scroll horizontal, paddings réduits), **480px** (typographie compacte, actions pleine largeur).

- Tokens : `--fc-page-padding-x/y`, `--fc-touch-min` (44px), `--fc-bp-*` dans `styles.scss`
- Nav mobile : défilement horizontal avec snap, badge notifs conservé
- Split mobile : liste master scrollable (max ~40vh), détail en dessous
- Cartes / cartes Leaflet : hauteurs en `dvh` adaptées au viewport mobile
- Safe areas : `env(safe-area-inset-*)` sur shell, login et toasts

## Spacing

Échelle `--fc-space-1` … `--fc-space-8` (0.25rem → 2rem).
