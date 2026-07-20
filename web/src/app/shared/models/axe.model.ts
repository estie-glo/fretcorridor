export interface AxeSummary {
  id: string;
  raw: Record<string, unknown>;
}

export type AxeStatut = Record<string, unknown>;

const LABEL_KEYS = ['nom', 'name', 'libelle', 'label', 'code', 'reference'] as const;

export function getAxeDisplayLabel(axe: AxeSummary): string {
  for (const key of LABEL_KEYS) {
    const value = axe.raw[key];

    if (typeof value === 'string' && value.trim().length > 0) {
      return value.trim();
    }
  }

  return axe.id;
}

export function parseAxesResponse(response: unknown): AxeSummary[] {
  const items = extractAxeArray(response);

  return items
    .map(parseAxeItem)
    .filter((axe): axe is AxeSummary => axe !== null);
}

export function parseAxeStatutResponse(response: unknown): AxeStatut | null {
  if (!response || typeof response !== 'object' || Array.isArray(response)) {
    return null;
  }

  return response as AxeStatut;
}

function extractAxeArray(response: unknown): unknown[] {
  if (Array.isArray(response)) {
    return response;
  }

  if (!response || typeof response !== 'object') {
    return [];
  }

  const record = response as Record<string, unknown>;
  const collectionKeys = ['content', 'data', 'items', 'axes'];

  for (const key of collectionKeys) {
    const value = record[key];

    if (Array.isArray(value)) {
      return value;
    }
  }

  return [];
}

function parseAxeItem(item: unknown): AxeSummary | null {
  if (!item || typeof item !== 'object' || Array.isArray(item)) {
    return null;
  }

  const record = item as Record<string, unknown>;
  const idValue = record['id'] ?? record['axeId'];

  if (idValue === undefined || idValue === null) {
    return null;
  }

  return {
    id: String(idValue),
    raw: record,
  };
}
