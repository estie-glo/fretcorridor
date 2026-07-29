import 'package:flutter_riverpod/legacy.dart';
import 'dio_provider.dart';
import 'package:dio/dio.dart';
import '../models/chauffeur_model.dart';

// ── Dio configuré avec JWT 

// ── État de la liste des chauffeurs 
class ChauffeurState {
  final List<ChauffeurModel> chauffeurs;
  final List<ChauffeurModel> kycEnAttente;
  final bool chargement;
  final String? erreur;
  final String? succes;

  const ChauffeurState({
    this.chauffeurs = const [],
    this.kycEnAttente = const [],
    this.chargement = false,
    this.erreur,
    this.succes,
  });

  ChauffeurState copyWith({
    List<ChauffeurModel>? chauffeurs,
    List<ChauffeurModel>? kycEnAttente,
    bool? chargement,
    String? erreur,
    String? succes,
  }) {
    return ChauffeurState(
      chauffeurs:    chauffeurs    ?? this.chauffeurs,
      kycEnAttente:  kycEnAttente  ?? this.kycEnAttente,
      chargement:    chargement    ?? this.chargement,
      erreur:        erreur,
      succes:        succes,
    );
  }
}

// ── Notifier 
class ChauffeurNotifier extends StateNotifier<ChauffeurState> {
  final Dio _dio;

  ChauffeurNotifier(this._dio) : super(const ChauffeurState()) {
    chargerTout();
  }

  // ── Charger chauffeurs + KYC en attente 
  Future<void> chargerTout() async {
    state = state.copyWith(chargement: true);
    try {
      final resChauffeurs = await _dio.get('/chauffeurs');
      final resKyc        = await _dio.get('/admin/kyc/en-attente');

      state = state.copyWith(
        chargement: false,
        chauffeurs: (resChauffeurs.data as List)
            .map((e) => ChauffeurModel.fromJson(e))
            .toList(),
        kycEnAttente: (resKyc.data as List)
            .map((e) => ChauffeurModel.fromJson(e))
            .toList(),
      );
    } on DioException catch (e) {
      state = state.copyWith(
        chargement: false,
        erreur: 'Erreur réseau : ${e.message}',
      );
    }
  }

  // ── Enrôler un chauffeur ─ retourne le chauffeur créé (pinEnvoye inclus) ou null si échec
  Future<ChauffeurModel?> enroler({
    required String nom,
    required String prenom,
    required String telephone,
    required String codePin,
    String? numeroCNI,
  }) async {
    state = state.copyWith(chargement: true, succes: null);
    try {
      final response = await _dio.post('/chauffeurs', data: {
        'nom':            nom,
        'prenom':         prenom,
        'telephone':      telephone,
        'codePinInitial': codePin,
        if (numeroCNI != null && numeroCNI.isNotEmpty) 'numeroCNI': numeroCNI,
      });

      final nouveauChauffeur = ChauffeurModel.fromJson(response.data);

      // Ajouter à la liste locale immédiatement
      state = state.copyWith(
        chargement: false,
        chauffeurs: [...state.chauffeurs, nouveauChauffeur],
        succes: '$prenom $nom enrôlé avec succès ✅',
      );
      return nouveauChauffeur;
   } on DioException catch (e) {
  String msg = "Erreur lors de l'enrôlement";

  if (e.response?.data is Map<String, dynamic>) {
    final data = e.response!.data as Map<String, dynamic>;
    final erreur = data["message"]?.toString();

    switch (erreur) {
      case "TELEPHONE_DEJA_UTILISE":
        msg = "Ce numéro de téléphone est déjà utilisé.";
        break;

      case "AGENT_INTROUVABLE":
        msg = "Agent introuvable.";
        break;

      case "ACCES_REFUSE":
        msg = "Accès refusé.";
        break;

      default:
        if (erreur != null && erreur.isNotEmpty) {
          msg = erreur;
        }
    }
  } else if (e.message != null) {
    msg = e.message!;
  }

  state = state.copyWith(
    chargement: false,
    erreur: msg,
  );

  return null;
}
  }

  // ── Valider un KYC 
  Future<void> validerKyc({
    required String chauffeurId,
    required bool approuve,
    String commentaire = '',
    String niveau = 'NIVEAU_1',
  }) async {
    try {
      final response = await _dio.put(
        '/admin/kyc/$chauffeurId/valider',
        data: {
          'approuve':      approuve,
          'commentaire':   commentaire,
          'nouveauNiveau': niveau,
        },
      );

      final chauffeurMaj = ChauffeurModel.fromJson(response.data);

      // Mettre à jour la liste et retirer du KYC en attente
      state = state.copyWith(
        chauffeurs: state.chauffeurs.map((c) =>
            c.id == chauffeurId ? chauffeurMaj : c).toList(),
        kycEnAttente: state.kycEnAttente
            .where((c) => c.id != chauffeurId)
            .toList(),
        succes: approuve ? 'KYC validé ✅' : 'KYC rejeté',
      );
    } on DioException catch (e) {
      state = state.copyWith(erreur: 'Erreur : ${e.message}');
    }
  }
}

// ── Provider exposé 
final chauffeurProvider =
    StateNotifierProvider<ChauffeurNotifier, ChauffeurState>((ref) {
  final dio = ref.watch(dioProvider);
  return ChauffeurNotifier(dio);
});
