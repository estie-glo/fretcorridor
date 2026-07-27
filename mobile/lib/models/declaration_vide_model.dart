class DeclarationVideModel {
  final String idLocal;
  final String? missionId;
  final String axeId;
  final String axeNom;
  final double latitude;
  final double longitude;
  final String typeCamion;
  final double capaciteTonnes;
  final DateTime dateCreation;
  final DateTime? disponibleDe; // EF-MKT-01 — null = disponible immédiatement
  final bool synchronise;

  const DeclarationVideModel({
    required this.idLocal,
    this.missionId,
    required this.axeId,
    required this.axeNom,
    required this.latitude,
    required this.longitude,
    required this.typeCamion,
    required this.capaciteTonnes,
    required this.dateCreation,
    this.disponibleDe,
    this.synchronise = false,
  });

  Map<String, dynamic> toMap() {
    return {
      'id_local': idLocal,
      'mission_id': missionId,
      'axe_id': axeId,
      'axe_nom': axeNom,
      'latitude': latitude,
      'longitude': longitude,
      'type_camion': typeCamion,
      'capacite_tonnes': capaciteTonnes,
      'date_creation': dateCreation.toIso8601String(),
      'disponible_de': disponibleDe?.toIso8601String(),
      'synchronise': synchronise ? 1 : 0,
    };
  }

  factory DeclarationVideModel.fromMap(Map<String, dynamic> map) {
    return DeclarationVideModel(
      idLocal: map['id_local'],
      missionId: map['mission_id'],
      axeId: map['axe_id'],
      axeNom: map['axe_nom'],
      latitude: map['latitude'],
      longitude: map['longitude'],
      typeCamion: map['type_camion'],
      capaciteTonnes: map['capacite_tonnes'],
      dateCreation: DateTime.parse(map['date_creation']),
      disponibleDe: map['disponible_de'] != null
          ? DateTime.parse(map['disponible_de'])
          : null,
      synchronise: map['synchronise'] == 1,
    );
  }
}
