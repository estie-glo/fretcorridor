export interface TenantConfig {
  nom?: string;
  name?: string;
  libelle?: string;
  code?: string;
  couleurPrimaire?: string;
  primaryColor?: string;
  couleur?: string;
  brandColor?: string;
  [key: string]: unknown;
}

const DISPLAY_NAME_KEYS = ['nom', 'name', 'libelle', 'code'] as const;
const PRIMARY_COLOR_KEYS = [
  'couleurPrimaire',
  'primaryColor',
  'couleur',
  'brandColor',
  'themeColor',
] as const;

const DEFAULT_PRIMARY = '#0f7a4a';

export function getTenantDisplayName(config: TenantConfig | null): string {
  if (!config) {
    return '';
  }

  for (const key of DISPLAY_NAME_KEYS) {
    const value = config[key];

    if (typeof value === 'string' && value.trim().length > 0) {
      return value.trim();
    }
  }

  return '';
}

export function getTenantPrimaryColor(config: TenantConfig | null): string | null {
  if (!config) {
    return null;
  }

  for (const key of PRIMARY_COLOR_KEYS) {
    const value = config[key];

    if (typeof value === 'string' && isSupportedCssColor(value)) {
      return value.trim();
    }
  }

  return null;
}

export function getResolvedPrimaryColor(config: TenantConfig | null): string {
  return getTenantPrimaryColor(config) ?? DEFAULT_PRIMARY;
}

function isSupportedCssColor(value: string): boolean {
  const trimmed = value.trim();

  if (/^#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})$/i.test(trimmed)) {
    return true;
  }

  return /^(rgb|hsl|oklch|lab|lch)\(/i.test(trimmed);
}
