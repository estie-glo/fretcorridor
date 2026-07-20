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
  return getEntityDisplayLabel(kyc, KYC_LABEL_KEYS);
}
