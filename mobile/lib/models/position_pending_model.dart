class PositionPendingModel {
  final String idLocal;
  final String missionId;
  final double latitude;
  final double longitude;
  final DateTime recordedAt;
  final double? vitesseKmh;
  final double? precisionMetres;
  final bool synchronise;

  const PositionPendingModel({
    required this.idLocal,
    required this.missionId,
    required this.latitude,
    required this.longitude,
    required this.recordedAt,
    this.vitesseKmh,
    this.precisionMetres,
    this.synchronise = false,
  });

  Map<String, dynamic> toMap() {
    return {
      'id_local': idLocal,
      'mission_id': missionId,
      'latitude': latitude,
      'longitude': longitude,
      'recorded_at': recordedAt.toIso8601String(),
      'vitesse_kmh': vitesseKmh,
      'precision_metres': precisionMetres,
      'synchronise': synchronise ? 1 : 0,
    };
  }

  factory PositionPendingModel.fromMap(Map<String, dynamic> map) {
    return PositionPendingModel(
      idLocal: map['id_local'],
      missionId: map['mission_id'],
      latitude: map['latitude'],
      longitude: map['longitude'],
      recordedAt: DateTime.parse(map['recorded_at']),
      vitesseKmh: map['vitesse_kmh'],
      precisionMetres: map['precision_metres'],
      synchronise: map['synchronise'] == 1,
    );
  }

  Map<String, dynamic> toApiJson() {
    return {
      'latitude': latitude,
      'longitude': longitude,
      'recordedAt': recordedAt.toIso8601String(),
      if (vitesseKmh != null) 'vitesseKmh': vitesseKmh,
      if (precisionMetres != null) 'precisionMetres': precisionMetres,
    };
  }
}
