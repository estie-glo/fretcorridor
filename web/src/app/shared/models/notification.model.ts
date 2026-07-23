export interface AppNotification {
  id: string;
  canal?: string;
  type?: string;
  titreFr?: string;
  titreEn?: string;
  corpsFr?: string;
  corpsEn?: string;
  ressourceType?: string;
  ressourceId?: string;
  lue: boolean;
  statutEnvoi?: string;
  dateCreation?: string;
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

function asBoolean(value: unknown): boolean {
  return value === true;
}

export function parseNotificationsResponse(response: unknown): AppNotification[] {
  if (!Array.isArray(response)) {
    return [];
  }
  return response
    .map(parseNotificationItem)
    .filter((n): n is AppNotification => n !== null);
}

export function parseNotificationItem(item: unknown): AppNotification | null {
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
    canal: asString(record['canal']),
    type: asString(record['type']),
    titreFr: asString(record['titreFr']),
    titreEn: asString(record['titreEn']),
    corpsFr: asString(record['corpsFr']),
    corpsEn: asString(record['corpsEn']),
    ressourceType: asString(record['ressourceType']),
    ressourceId: asString(record['ressourceId']),
    lue: asBoolean(record['lue']),
    statutEnvoi: asString(record['statutEnvoi']),
    dateCreation: asString(record['dateCreation']),
    raw: record,
  };
}

export function getNotificationTitle(n: AppNotification, lang: string): string {
  if (lang.startsWith('en')) {
    return n.titreEn || n.titreFr || n.type || n.id;
  }
  return n.titreFr || n.titreEn || n.type || n.id;
}

export function getNotificationBody(n: AppNotification, lang: string): string {
  if (lang.startsWith('en')) {
    return n.corpsEn || n.corpsFr || '';
  }
  return n.corpsFr || n.corpsEn || '';
}
