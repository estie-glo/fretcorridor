import 'dart:convert';

class ConfigTenant {
  final String tenantId;
  final String nomBureau;
  final String langue;
  final String devise;
  final List<String> axesDisponibles;

  const ConfigTenant({
    required this.tenantId,
    required this.nomBureau,
    required this.langue,
    required this.devise,
    required this.axesDisponibles,
  });

  factory ConfigTenant.fromJson(Map<String, dynamic> json) {
    return ConfigTenant(
      tenantId: json['tenantId'] ?? '',
      nomBureau: json['nomBureau'] ?? '',
      langue: json['langue'] ?? 'fr',
      devise: json['devise'] ?? 'FCFA',
      axesDisponibles: List<String>.from(json['axesDisponibles'] ?? []),
    );
  }

  Map<String, dynamic> toJson() => {
    'tenantId': tenantId,
    'nomBureau': nomBureau,
    'langue': langue,
    'devise': devise,
    'axesDisponibles': axesDisponibles,
  };
}

class UtilisateurModel {
  final String accessToken;
  final String refreshToken;
  final String role;
  final String tenantId;
  final ConfigTenant configTenant;
  final bool pinTemporaire;

  const UtilisateurModel({
    required this.accessToken,
    required this.refreshToken,
    required this.role,
    required this.tenantId,
    required this.configTenant,
    this.pinTemporaire = false,
  });

  UtilisateurModel copyWith({bool? pinTemporaire}) {
    return UtilisateurModel(
      accessToken: accessToken,
      refreshToken: refreshToken,
      role: role,
      tenantId: tenantId,
      configTenant: configTenant,
      pinTemporaire: pinTemporaire ?? this.pinTemporaire,
    );
  }

  // Depuis la réponse API
  factory UtilisateurModel.fromJson(Map<String, dynamic> json) {
    return UtilisateurModel(
      accessToken:  json['accessToken'] ?? '',
      refreshToken: json['refreshToken'] ?? '',
      role:         json['role'] ?? '',
      tenantId:     json['tenantId'] ?? '',
      configTenant: ConfigTenant.fromJson(json['configTenant'] ?? {}),
      pinTemporaire: json['pinTemporaire'] ?? false,
    );
  }

  // Pour le stockage local (SecureStorage)
  String toJsonString() => jsonEncode({
    'accessToken':  accessToken,
    'refreshToken': refreshToken,
    'role':         role,
    'tenantId':     tenantId,
    'configTenant': configTenant.toJson(),
    'pinTemporaire': pinTemporaire,
  });

  factory UtilisateurModel.fromJsonString(String jsonString) {
    return UtilisateurModel.fromJson(jsonDecode(jsonString));
  }

  // Helpers
  bool get estChauffeur => role == 'CHAUFFEUR';
  bool get estAgent     => role == 'AGENT';
  bool get estClient    => role == 'CLIENT';
}
