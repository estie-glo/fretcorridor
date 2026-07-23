export interface MissionSummary {
  id: string;
  axeId?: string;
  axeNom?: string;
  chauffeurId?: string;
  chauffeurNom?: string;
  statut?: string;
  typeCamion?: string;
  capaciteTonnes?: number;
  latitude?: number;
  longitude?: number;
  zoneSensible?: boolean;
  dateDeclaration?: string;
  raw: Record<string, unknown>;
}

export interface TrackingPoint {
  latitude: number;
  longitude: number;
  recordedAt?: string;
  vitesseKmh?: number;
}

export interface TrackingInfo {
  missionId: string;
  statutMission?: string;
  zoneSensible?: boolean;
  lastPosition?: TrackingPoint | null;
  points: TrackingPoint[];
}

export interface EtaInfo {
  missionId: string;
  etaMinutes?: number;
  etaAt?: string;
  distanceRestanteKm?: number;
  statutCalcul?: string;
}

function asRecord(value: unknown): Record<string, unknown> | null {
  return value !== null && typeof value === 'object' && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : null;
}

function asString(value: unknown): string | undefined {
  return typeof value === 'string' && value.trim() ? value : undefined;
}

function asNumber(value: unknown): number | undefined {
  return typeof value === 'number' && Number.isFinite(value) ? value : undefined;
}

function asBoolean(value: unknown): boolean | undefined {
  return typeof value === 'boolean' ? value : undefined;
}

export function parseMissionsResponse(response: unknown): MissionSummary[] {
  const items = Array.isArray(response)
    ? response
    : Array.isArray(asRecord(response)?.['content'])
      ? (asRecord(response)!['content'] as unknown[])
      : Array.isArray(asRecord(response)?.['data'])
        ? (asRecord(response)!['data'] as unknown[])
        : [];

  return items
    .map((item) => parseMissionItem(item))
    .filter((item): item is MissionSummary => item !== null);
}

export function parseMissionItem(item: unknown): MissionSummary | null {
  const raw = asRecord(item);
  if (!raw) {
    return null;
  }

  const id = asString(raw['id']);
  if (!id) {
    return null;
  }

  return {
    id,
    axeId: asString(raw['axeId']),
    axeNom: asString(raw['axeNom']),
    chauffeurId: asString(raw['chauffeurId']),
    chauffeurNom: asString(raw['chauffeurNom']),
    statut: asString(raw['statut']),
    typeCamion: asString(raw['typeCamion']),
    capaciteTonnes: asNumber(raw['capaciteTonnes']),
    latitude: asNumber(raw['latitude']),
    longitude: asNumber(raw['longitude']),
    zoneSensible: asBoolean(raw['zoneSensible']),
    dateDeclaration: asString(raw['dateDeclaration']),
    raw,
  };
}

export function parseTrackingResponse(response: unknown): TrackingInfo | null {
  const raw = asRecord(response);
  if (!raw) {
    return null;
  }

  const missionId = asString(raw['missionId']);
  if (!missionId) {
    return null;
  }

  const pointsRaw = Array.isArray(raw['points']) ? (raw['points'] as unknown[]) : [];
  const points = pointsRaw
    .map((p) => parsePoint(p))
    .filter((p): p is TrackingPoint => p !== null);

  return {
    missionId,
    statutMission: asString(raw['statutMission']),
    zoneSensible: asBoolean(raw['zoneSensible']),
    lastPosition: parsePoint(raw['lastPosition']),
    points,
  };
}

export function parseEtaResponse(response: unknown): EtaInfo | null {
  const raw = asRecord(response);
  if (!raw) {
    return null;
  }

  const missionId = asString(raw['missionId']);
  if (!missionId) {
    return null;
  }

  return {
    missionId,
    etaMinutes: asNumber(raw['etaMinutes']),
    etaAt: asString(raw['etaAt']),
    distanceRestanteKm: asNumber(raw['distanceRestanteKm']),
    statutCalcul: asString(raw['statutCalcul']),
  };
}

function parsePoint(value: unknown): TrackingPoint | null {
  const raw = asRecord(value);
  if (!raw) {
    return null;
  }
  const latitude = asNumber(raw['latitude']);
  const longitude = asNumber(raw['longitude']);
  if (latitude === undefined || longitude === undefined) {
    return null;
  }
  return {
    latitude,
    longitude,
    recordedAt: asString(raw['recordedAt']),
    vitesseKmh: asNumber(raw['vitesseKmh']),
  };
}

export function getMissionDisplayLabel(mission: MissionSummary): string {
  return mission.axeNom || mission.chauffeurNom || mission.statut || mission.id;
}
