export type AxeEtatActivation = 'ACTIF' | 'VERROUILLE' | 'INACTIF';

export interface AxeSummary {
  id: string;
  nom?: string;
  hubDepart?: string;
  hubArrivee?: string;
  hubDepartId?: string;
  hubArriveeId?: string;
  hubDepartLatitude?: number;
  hubDepartLongitude?: number;
  hubArriveeLatitude?: number;
  hubArriveeLongitude?: number;
  /** Dérivé serveur des 3 flags GEO (compat carte). */
  etatActivation?: AxeEtatActivation | string;
  visibiliteActive?: boolean;
  matchingActif?: boolean;
  financementActif?: boolean;
  zoneSensible?: boolean;
  raw: Record<string, unknown>;
}

export type AxeStatut = Record<string, unknown>;

const LABEL_KEYS = ['nom', 'name', 'libelle', 'label', 'code', 'reference'] as const;

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

function asBoolean(value: unknown): boolean | undefined {
  return typeof value === 'boolean' ? value : undefined;
}

export function getAxeDisplayLabel(axe: AxeSummary): string {
  if (axe.nom) {
    return axe.nom;
  }

  for (const key of LABEL_KEYS) {
    const value = axe.raw[key];

    if (typeof value === 'string' && value.trim().length > 0) {
      return value.trim();
    }
  }

  return axe.id;
}

/** Ligne secondaire : trajet ou état d'activation. */
export function getAxeDisplayMeta(axe: AxeSummary): string {
  const depart = axe.hubDepart ?? '';
  const arrivee = axe.hubArrivee ?? '';

  if (depart && arrivee) {
    return `${depart} → ${arrivee}`;
  }

  return axe.etatActivation?.trim() || '';
}

export function hasAxeGeo(axe: AxeSummary): boolean {
  return (
    axe.hubDepartLatitude !== undefined &&
    axe.hubDepartLongitude !== undefined &&
    axe.hubArriveeLatitude !== undefined &&
    axe.hubArriveeLongitude !== undefined
  );
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

  const record = asRecord(response);
  if (!record) {
    return [];
  }

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
  const record = asRecord(item);
  if (!record) {
    return null;
  }

  const idValue = record['id'] ?? record['axeId'];
  if (idValue === undefined || idValue === null) {
    return null;
  }

  const etat = asString(record['etatActivation']);

  return {
    id: String(idValue),
    nom: asString(record['nom']) ?? asString(record['name']),
    hubDepart: asString(record['hubDepart']),
    hubArrivee: asString(record['hubArrivee']),
    hubDepartId: asString(record['hubDepartId']),
    hubArriveeId: asString(record['hubArriveeId']),
    hubDepartLatitude: asNumber(record['hubDepartLatitude']),
    hubDepartLongitude: asNumber(record['hubDepartLongitude']),
    hubArriveeLatitude: asNumber(record['hubArriveeLatitude']),
    hubArriveeLongitude: asNumber(record['hubArriveeLongitude']),
    etatActivation: etat,
    visibiliteActive: asBoolean(record['visibiliteActive']),
    matchingActif: asBoolean(record['matchingActif']),
    financementActif: asBoolean(record['financementActif']),
    zoneSensible: asBoolean(record['zoneSensible']),
    raw: record,
  };
}
