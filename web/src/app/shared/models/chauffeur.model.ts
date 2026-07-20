import {
  EntitySummary,
  getEntityDisplayLabel,
  parseEntityCollection,
  parseEntityRecord,
} from '../utils/collection-parser';

const CHAUFFEUR_COLLECTION_KEYS = ['content', 'data', 'items', 'chauffeurs', 'drivers'];
const CHAUFFEUR_ID_KEYS = ['id', 'chauffeurId', 'driverId'];
const CHAUFFEUR_LABEL_KEYS = ['nom', 'name', 'telephone', 'phone', 'matricule', 'code'];

export type ChauffeurSummary = EntitySummary;
export type ChauffeurDetail = Record<string, unknown>;

export function parseChauffeursResponse(response: unknown): ChauffeurSummary[] {
  return parseEntityCollection(response, {
    collectionKeys: CHAUFFEUR_COLLECTION_KEYS,
    idKeys: CHAUFFEUR_ID_KEYS,
  });
}

export function parseChauffeurDetailResponse(response: unknown): ChauffeurDetail | null {
  return parseEntityRecord(response);
}

export function getChauffeurDisplayLabel(chauffeur: ChauffeurSummary): string {
  return getEntityDisplayLabel(chauffeur, CHAUFFEUR_LABEL_KEYS);
}
