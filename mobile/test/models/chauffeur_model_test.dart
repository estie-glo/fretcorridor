import 'package:flutter_test/flutter_test.dart';
import 'package:fretcorridor_mobile/models/chauffeur_model.dart';

void main() {
  test('ChauffeurModel.fromJson parse les champs principaux', () {
    final model = ChauffeurModel.fromJson({
      'id': 'abc-123',
      'nom': 'Mbarga',
      'prenom': 'Paul',
      'telephone': '+237699000001',
      'tenantId': 'BGFT_CM',
      'kycNiveau': 'NIVEAU_1',
      'statutKyc': 'VALIDE',
      'badgeKyc': 'KYC NIVEAU 1 validé ✅',
      'urlPhotoCNI': 'http://minio/cni.jpg',
    });

    expect(model.id, 'abc-123');
    expect(model.nomComplet, 'Paul Mbarga');
    expect(model.kycValide, isTrue);
    expect(model.urlPhotoCNI, isNotNull);
    expect(model.pinEnvoye, isNull); // absent du JSON → non applicable
  });

  test('ChauffeurModel.fromJson parse pinEnvoye à l\'enrôlement', () {
    final envoye = ChauffeurModel.fromJson({
      'id': 'abc-124',
      'nom': 'Mbarga',
      'prenom': 'Paul',
      'telephone': '+237699000001',
      'tenantId': 'BGFT_CM',
      'kycNiveau': 'NIVEAU_1',
      'statutKyc': 'EN_ATTENTE',
      'badgeKyc': 'KYC en attente ⏳',
      'pinEnvoye': true,
    });
    expect(envoye.pinEnvoye, isTrue);

    final echoue = ChauffeurModel.fromJson({
      'id': 'abc-125',
      'nom': 'Mbarga',
      'prenom': 'Paul',
      'telephone': '+237699000002',
      'tenantId': 'BGFT_CM',
      'kycNiveau': 'NIVEAU_1',
      'statutKyc': 'EN_ATTENTE',
      'badgeKyc': 'KYC en attente ⏳',
      'pinEnvoye': false,
    });
    expect(echoue.pinEnvoye, isFalse);
  });
}
