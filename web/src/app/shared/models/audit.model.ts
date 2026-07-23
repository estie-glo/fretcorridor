export interface AuditEntry {
  id: string;
  action?: string;
  ressourceType?: string;
  ressourceId?: string;
  acteurId?: string;
  acteurRole?: string;
  avant?: string;
  apres?: string;
  horodatage?: string;
  raw: Record<string, unknown>;
}

function asRecord(value: unknown): Record<string, unknown> | null {
  return value !== null && typeof value === 'object' && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : null;
}

function asString(value: unknown): string | undefined {
  return typeof value === 'string' && value.trim() ? value.trim() : undefined;
}

export function parseAuditResponse(response: unknown): AuditEntry[] {
  if (!Array.isArray(response)) {
    return [];
  }
  return response
    .map(parseAuditItem)
    .filter((e): e is AuditEntry => e !== null);
}

function parseAuditItem(item: unknown): AuditEntry | null {
  const record = asRecord(item);
  if (!record) {
    return null;
  }
  const id = record['id'];
  if (id === undefined || id === null) {
    return null;
  }
  return {
    id: String(id),
    action: asString(record['action']),
    ressourceType: asString(record['ressourceType']),
    ressourceId: asString(record['ressourceId']),
    acteurId: asString(record['acteurId']),
    acteurRole: asString(record['acteurRole']),
    avant: asString(record['avant']),
    apres: asString(record['apres']),
    horodatage: asString(record['horodatage']),
    raw: record,
  };
}
