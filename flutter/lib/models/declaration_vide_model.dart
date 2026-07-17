class DeclarationVideModel {
  final String idLocal;         // UUID généré côté client (clé d'idempotence)
  final String axeId;
  final String axeNom;          // pour affichage offline
  final double latitude;
  final double longitude;
  final String typeCamion;
  final double capaciteTonnes;
  final DateTime dateCreation;
  final bool synchronise;

  const DeclarationVideModel({
    required this.idLocal,
    required this.axeId,
    required this.axeNom,
    required this.latitude,
    required this.longitude,
    required this.typeCamion,
    required this.capaciteTonnes,
    required this.dateCreation,
    this.synchronise = false,
  });

  Map<String, dynamic> toMap() {
    return {
      'id_local': idLocal,
      'axe_id': axeId,
      'axe_nom': axeNom,
      'latitude': latitude,
      'longitude': longitude,
      'type_camion': typeCamion,
      'capacite_tonnes': capaciteTonnes,
      'date_creation': dateCreation.toIso8601String(),
      'synchronise': synchronise ? 1 : 0,
    };
  }

  factory DeclarationVideModel.fromMap(Map<String, dynamic> map) {
    return DeclarationVideModel(
      idLocal: map['id_local'],
      axeId: map['axe_id'],
      axeNom: map['axe_nom'],
      latitude: map['latitude'],
      longitude: map['longitude'],
      typeCamion: map['type_camion'],
      capaciteTonnes: map['capacite_tonnes'],
      dateCreation: DateTime.parse(map['date_creation']),
      synchronise: map['synchronise'] == 1,
    );
  }

  DeclarationVideModel copierAvec({bool? synchronise}) {
    return DeclarationVideModel(
      idLocal: idLocal, axeId: axeId, axeNom: axeNom,
      latitude: latitude, longitude: longitude,
      typeCamion: typeCamion, capaciteTonnes: capaciteTonnes,
      dateCreation: dateCreation,
      synchronise: synchronise ?? this.synchronise,
    );
  }
}
