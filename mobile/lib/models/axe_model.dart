class AxeModel {
  final String id;
  final String nom;
  final String hubDepart;
  final String hubArrivee;
  final String etatActivation;
  final bool zoneSensible;

  const AxeModel({
    required this.id,
    required this.nom,
    required this.hubDepart,
    required this.hubArrivee,
    required this.etatActivation,
    required this.zoneSensible,
  });

  factory AxeModel.fromJson(Map<String, dynamic> json) {
    return AxeModel(
      id:              json['id'] ?? '',
      nom:             json['nom'] ?? '',
      hubDepart:       json['hubDepart'] ?? '',
      hubArrivee:      json['hubArrivee'] ?? '',
      etatActivation:  json['etatActivation'] ?? 'INACTIF',
      zoneSensible:    json['zoneSensible'] ?? false,
    );
  }

  bool get actif      => etatActivation == 'ACTIF';
  bool get verrouille  => etatActivation == 'VERROUILLE';
  bool get inactif     => etatActivation == 'INACTIF';
}
