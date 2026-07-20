export interface EntitySummary {
  id: string;
  raw: Record<string, unknown>;
}

const DEFAULT_COLLECTION_KEYS = [
  'content',
  'data',
  'items',
  'results',
  'kyc',
  'chauffeurs',
  'drivers',
];

const DEFAULT_ID_KEYS = ['id'];
const DEFAULT_LABEL_KEYS = ['nom', 'name', 'libelle', 'label', 'telephone', 'phone', 'code'];

export function extractCollection(
  response: unknown,
  collectionKeys: string[] = DEFAULT_COLLECTION_KEYS,
): unknown[] {
  if (Array.isArray(response)) {
    return response;
  }

  if (!response || typeof response !== 'object') {
    return [];
  }

  const record = response as Record<string, unknown>;

  for (const key of collectionKeys) {
    const value = record[key];

    if (Array.isArray(value)) {
      return value;
    }
  }

  return [];
}

export function parseEntityItem(
  item: unknown,
  idKeys: string[] = DEFAULT_ID_KEYS,
): EntitySummary | null {
  if (!item || typeof item !== 'object' || Array.isArray(item)) {
    return null;
  }

  const record = item as Record<string, unknown>;

  for (const key of idKeys) {
    const value = record[key];

    if (value !== undefined && value !== null) {
      return {
        id: String(value),
        raw: record,
      };
    }
  }

  return null;
}

export function parseEntityCollection(
  response: unknown,
  options?: {
    collectionKeys?: string[];
    idKeys?: string[];
  },
): EntitySummary[] {
  const items = extractCollection(response, options?.collectionKeys);
  const idKeys = options?.idKeys ?? DEFAULT_ID_KEYS;

  return items
    .map((item) => parseEntityItem(item, idKeys))
    .filter((entity): entity is EntitySummary => entity !== null);
}

export function getEntityDisplayLabel(
  entity: EntitySummary,
  labelKeys: string[] = DEFAULT_LABEL_KEYS,
): string {
  for (const key of labelKeys) {
    const value = entity.raw[key];

    if (typeof value === 'string' && value.trim().length > 0) {
      return value.trim();
    }
  }

  return entity.id;
}

export function parseEntityRecord(response: unknown): Record<string, unknown> | null {
  if (!response || typeof response !== 'object' || Array.isArray(response)) {
    return null;
  }

  return response as Record<string, unknown>;
}
