export interface RecordEntry {
  key: string;
  value: string;
}

export function toRecordEntries(record: Record<string, unknown> | null): RecordEntry[] {
  if (!record) {
    return [];
  }

  return Object.entries(record)
    .map(([key, value]) => ({
      key,
      value: formatRecordValue(value),
    }))
    .sort((left, right) => left.key.localeCompare(right.key));
}

function formatRecordValue(value: unknown): string {
  if (value === null || value === undefined) {
    return '—';
  }

  if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
    return String(value);
  }

  try {
    return JSON.stringify(value);
  } catch {
    return String(value);
  }
}
