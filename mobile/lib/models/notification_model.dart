class NotificationModel {
  final String id;
  final String canal;
  final String type;
  final String titreFr;
  final String titreEn;
  final String corpsFr;
  final String corpsEn;
  final bool lue;
  final String? dateCreation;

  const NotificationModel({
    required this.id,
    required this.canal,
    required this.type,
    required this.titreFr,
    required this.titreEn,
    required this.corpsFr,
    required this.corpsEn,
    required this.lue,
    this.dateCreation,
  });

  factory NotificationModel.fromJson(Map<String, dynamic> json) {
    return NotificationModel(
      id: json['id']?.toString() ?? '',
      canal: json['canal'] ?? 'IN_APP',
      type: json['type'] ?? '',
      titreFr: json['titreFr'] ?? '',
      titreEn: json['titreEn'] ?? '',
      corpsFr: json['corpsFr'] ?? '',
      corpsEn: json['corpsEn'] ?? '',
      lue: json['lue'] == true,
      dateCreation: json['dateCreation']?.toString(),
    );
  }
}
