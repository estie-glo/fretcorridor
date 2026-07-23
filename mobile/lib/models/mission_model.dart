class MissionModel {
  final String id;
  final String? axeId;
  final String? axeNom;
  final String? chauffeurNom;
  final double? latitude;
  final double? longitude;
  final String? typeCamion;
  final double? capaciteTonnes;
  final String statut;
  final bool zoneSensible;
  final String? dateDeclaration;

  const MissionModel({
    required this.id,
    this.axeId,
    this.axeNom,
    this.chauffeurNom,
    this.latitude,
    this.longitude,
    this.typeCamion,
    this.capaciteTonnes,
    required this.statut,
    this.zoneSensible = false,
    this.dateDeclaration,
  });

  factory MissionModel.fromJson(Map<String, dynamic> json) {
    return MissionModel(
      id: json['id']?.toString() ?? '',
      axeId: json['axeId']?.toString(),
      axeNom: json['axeNom'],
      chauffeurNom: json['chauffeurNom'],
      latitude: (json['latitude'] as num?)?.toDouble(),
      longitude: (json['longitude'] as num?)?.toDouble(),
      typeCamion: json['typeCamion'],
      capaciteTonnes: (json['capaciteTonnes'] as num?)?.toDouble(),
      statut: json['statut'] ?? '',
      zoneSensible: json['zoneSensible'] == true,
      dateDeclaration: json['dateDeclaration']?.toString(),
    );
  }
}
