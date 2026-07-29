class ChauffeurModel {
  final String id;
  final String nom;
  final String prenom;
  final String telephone;
  final String tenantId;
  final String kycNiveau;
  final String statutKyc;
  final String? urlPhotoCNI;
  final String? urlPhotoPermis;
  final String? dateEnrolement;
  final String? dateValidationKyc;
  final String? agentNom;
  final String badgeKyc;
  final bool? pinEnvoye; // renseigné uniquement juste après l'enrôlement

  const ChauffeurModel({
    required this.id,
    required this.nom,
    required this.prenom,
    required this.telephone,
    required this.tenantId,
    required this.kycNiveau,
    required this.statutKyc,
    this.urlPhotoCNI,
    this.urlPhotoPermis,
    this.dateEnrolement,
    this.dateValidationKyc,
    this.agentNom,
    required this.badgeKyc,
    this.pinEnvoye,
  });

  factory ChauffeurModel.fromJson(Map<String, dynamic> json) {
    return ChauffeurModel(
      id:               json['id'] ?? '',
      nom:              json['nom'] ?? '',
      prenom:           json['prenom'] ?? '',
      telephone:        json['telephone'] ?? '',
      tenantId:         json['tenantId'] ?? '',
      kycNiveau:        json['kycNiveau'] ?? 'NIVEAU_0',
      statutKyc:        json['statutKyc'] ?? 'EN_ATTENTE',
      urlPhotoCNI:      json['urlPhotoCNI'],
      urlPhotoPermis:   json['urlPhotoPermis'],
      dateEnrolement:   json['dateEnrolement'],
      dateValidationKyc: json['dateValidationKyc'],
      agentNom:         json['agentNom'],
      badgeKyc:         json['badgeKyc'] ?? '',
      pinEnvoye:        json['pinEnvoye'],
    );
  }

  // Helpers
  bool get kycValide   => statutKyc == 'VALIDE';
  bool get kycEnAttente => statutKyc == 'EN_ATTENTE';
  bool get kycEnCours  => statutKyc == 'EN_COURS';
  bool get kycRejete   => statutKyc == 'REJETE';
  String get nomComplet => '$prenom $nom';
}
