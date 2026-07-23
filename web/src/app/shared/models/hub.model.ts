export interface Hub {
  id: string;
  nom: string;
  pays?: string;
  latitude: number;
  longitude: number;
}

function asRecord(value: unknown): Record<string, unknown> | null {
  return value !== null && typeof value === 'object' && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : null;
}

function asString(value: unknown): string | undefined {
  return typeof value === 'string' && value.trim() ? value.trim() : undefined;
}

function asNumber(value: unknown): number | undefined {
  return typeof value === 'number' && Number.isFinite(value) ? value : undefined;
}

export function parseHubsResponse(response: unknown): Hub[] {
  const items = Array.isArray(response)
    ? response
    : Array.isArray(asRecord(response)?.['content'])
      ? (asRecord(response)!['content'] as unknown[])
      : Array.isArray(asRecord(response)?.['data'])
        ? (asRecord(response)!['data'] as unknown[])
        : Array.isArray(asRecord(response)?.['hubs'])
          ? (asRecord(response)!['hubs'] as unknown[])
          : [];

  return items
    .map((item) => parseHubItem(item))
    .filter((hub): hub is Hub => hub !== null);
}

export function parseHubItem(item: unknown): Hub | null {
  const raw = asRecord(item);
  if (!raw) {
    return null;
  }

  const id = asString(raw['id']);
  const nom = asString(raw['nom']) ?? asString(raw['name']);
  const latitude = asNumber(raw['latitude']);
  const longitude = asNumber(raw['longitude']);

  if (!id || !nom || latitude === undefined || longitude === undefined) {
    return null;
  }

  return {
    id,
    nom,
    pays: asString(raw['pays']) ?? asString(raw['country']),
    latitude,
    longitude,
  };
}
