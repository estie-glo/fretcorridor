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
  const nom = typeof chauffeur.raw['nom'] === 'string' ? chauffeur.raw['nom'].trim() : '';
  const prenom = typeof chauffeur.raw['prenom'] === 'string' ? chauffeur.raw['prenom'].trim() : '';
  const fullName = [prenom, nom].filter(Boolean).join(' ').trim();

  if (fullName) {
    return fullName;
  }

  return getEntityDisplayLabel(chauffeur, CHAUFFEUR_LABEL_KEYS);
}

export function getChauffeurDisplayMeta(chauffeur: ChauffeurSummary): string {
  const phone = typeof chauffeur.raw['telephone'] === 'string' ? chauffeur.raw['telephone'].trim() : '';
  const badge = typeof chauffeur.raw['badgeKyc'] === 'string' ? chauffeur.raw['badgeKyc'].trim() : '';
  const statut = typeof chauffeur.raw['statutKyc'] === 'string' ? chauffeur.raw['statutKyc'].trim() : '';

  return phone || badge || statut || '';
}
