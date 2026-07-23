import {
  EntitySummary,
  getEntityDisplayLabel,
  parseEntityCollection,
} from '../utils/collection-parser';

const KYC_COLLECTION_KEYS = ['content', 'data', 'items', 'kyc', 'dossiers'];
const KYC_ID_KEYS = ['id', 'kycId', 'dossierId'];
const KYC_LABEL_KEYS = ['nom', 'name', 'telephone', 'phone', 'libelle'];

export type KycSummary = EntitySummary;

export function parseKycPendingResponse(response: unknown): KycSummary[] {
  return parseEntityCollection(response, {
    collectionKeys: KYC_COLLECTION_KEYS,
    idKeys: KYC_ID_KEYS,
  });
}

export function getKycDisplayLabel(kyc: KycSummary): string {
  const nom = typeof kyc.raw['nom'] === 'string' ? kyc.raw['nom'].trim() : '';
  const prenom = typeof kyc.raw['prenom'] === 'string' ? kyc.raw['prenom'].trim() : '';
  const fullName = [prenom, nom].filter(Boolean).join(' ').trim();

  if (fullName) {
    return fullName;
  }

  return getEntityDisplayLabel(kyc, KYC_LABEL_KEYS);
}

export function getKycDisplayMeta(kyc: KycSummary): string {
  const phone = typeof kyc.raw['telephone'] === 'string' ? kyc.raw['telephone'].trim() : '';
  const statut = typeof kyc.raw['statutKyc'] === 'string' ? kyc.raw['statutKyc'].trim() : '';
  const badge = typeof kyc.raw['badgeKyc'] === 'string' ? kyc.raw['badgeKyc'].trim() : '';

  return phone || badge || statut || '';
}
