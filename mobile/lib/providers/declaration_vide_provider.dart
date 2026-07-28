import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:dio/dio.dart';
import 'package:uuid/uuid.dart';
import '../models/declaration_vide_model.dart';
import '../services/declaration_local_db.dart';
import '../services/gps_service.dart';
import 'dio_provider.dart';
import 'connectivity_provider.dart';

class DeclarationVideState {
  final List<DeclarationVideModel> declarations;
  final bool chargement;
  final bool positionEnCours;
  final String? erreur;
  final String? succes;

  const DeclarationVideState({
    this.declarations = const [],
    this.chargement = false,
    this.positionEnCours = false,
    this.erreur,
    this.succes,
  });

  DeclarationVideState copyWith({
    List<DeclarationVideModel>? declarations,
    bool? chargement,
    bool? positionEnCours,
    String? erreur,
    String? succes,
  }) {
    return DeclarationVideState(
      declarations: declarations ?? this.declarations,
      chargement: chargement ?? this.chargement,
      positionEnCours: positionEnCours ?? this.positionEnCours,
      erreur: erreur,
      succes: succes,
    );
  }
}

class DeclarationVideNotifier extends StateNotifier<DeclarationVideState> {
  final Dio _dio;
  final Ref _ref;
  static const _uuid = Uuid();

  DeclarationVideNotifier(this._dio, this._ref) : super(const DeclarationVideState()) {
    _chargerLocal();
  }

  Future<void> _chargerLocal() async {
    final declarations = await DeclarationLocalDb.getToutes();
    state = state.copyWith(declarations: declarations);
  }

  // ── Recharger depuis le serveur (source de vérité) ─────────
  // Corrige le fait que la liste locale seule ne reflète que CET appareil :
  // après une désinstallation, ou sur un autre téléphone/compte partagé,
  // la vraie liste vient de GET /missions/mes-declarations.
  Future<void> chargerDepuisServeur() async {
    state = state.copyWith(chargement: true, erreur: null);
    try {
      final response = await _dio.get('/missions/mes-declarations');
      final items = (response.data as List);

      for (final item in items) {
        final missionId = item['id']?.toString();
        if (missionId == null) continue;

        final dejaLocal = state.declarations.any((d) => d.missionId == missionId);
        if (dejaLocal) continue; // déjà présente localement, rien à dupliquer

        final nouvelle = DeclarationVideModel(
          idLocal: _uuid.v4(),
          missionId: missionId,
          axeId: item['axeId']?.toString() ?? '',
          axeNom: item['axeNom']?.toString() ?? '',
          latitude: (item['latitude'] as num?)?.toDouble() ?? 0,
          longitude: (item['longitude'] as num?)?.toDouble() ?? 0,
          typeCamion: item['typeCamion']?.toString() ?? '',
          capaciteTonnes: (item['capaciteTonnes'] as num?)?.toDouble() ?? 0,
          dateCreation: item['dateDeclaration'] != null
              ? DateTime.parse(item['dateDeclaration'])
              : DateTime.now(),
          disponibleDe: item['disponibleDe'] != null
              ? DateTime.parse(item['disponibleDe'])
              : null,
          synchronise: true,
        );
        await DeclarationLocalDb.ajouter(nouvelle);
      }

      await _chargerLocal();
      state = state.copyWith(chargement: false);
    } on DioException catch (e) {
      state = state.copyWith(
        chargement: false,
        erreur: 'Impossible de charger depuis le serveur : ${e.response?.data ?? e.message}',
      );
    }
  }

  // ── Déclarer un camion vide (offline-first) ───────────────
  // EF-MKT-01 : disponibleDe optionnel — null = disponible immédiatement
  Future<bool> declarer({
    required String axeId,
    required String axeNom,
    required String typeCamion,
    required double capaciteTonnes,
    DateTime? disponibleDe,
  }) async {
    state = state.copyWith(positionEnCours: true, erreur: null, succes: null);

    final position = await GpsService.positionActuelle();
    if (position == null) {
      state = state.copyWith(
        positionEnCours: false,
        erreur: 'Impossible de récupérer la position GPS. Vérifiez que la localisation est activée.',
      );
      return false;
    }

    final declaration = DeclarationVideModel(
      idLocal: _uuid.v4(),
      axeId: axeId,
      axeNom: axeNom,
      latitude: position.latitude,
      longitude: position.longitude,
      typeCamion: typeCamion,
      capaciteTonnes: capaciteTonnes,
      dateCreation: DateTime.now(),
      disponibleDe: disponibleDe,
    );

    // Sauvegarde locale immédiate — jamais de perte de donnée (ENF-OFF-01)
    await DeclarationLocalDb.ajouter(declaration);
    state = state.copyWith(positionEnCours: false);
    await _chargerLocal();

    final statutConnexion = _ref.read(connectivityProvider);
    if (statutConnexion == StatutConnexion.enLigne) {
      final envoyee = await _envoyerAuServeur(declaration);
      if (envoyee) {
        state = state.copyWith(succes: 'Déclaration envoyée ✅');
      } else {
        state = state.copyWith(succes: 'Enregistrée localement — sera synchronisée 🔄');
      }
    } else {
      state = state.copyWith(succes: 'Hors ligne — déclaration en attente de synchronisation 📴');
    }

    return true;
  }

  // ── Modifier une déclaration déjà synchronisée ─────────────
  Future<bool> modifier({
    required String idLocal,
    required String? missionId,
    String? typeCamion,
    double? capaciteTonnes,
    DateTime? disponibleDe,
  }) async {
    if (missionId == null) {
      state = state.copyWith(
        erreur: 'Cette déclaration n\'est pas encore synchronisée avec le serveur.',
      );
      return false;
    }
    try {
      await _dio.put('/missions/mes-declarations/$missionId', data: {
        if (typeCamion != null) 'typeCamion': typeCamion,
        if (capaciteTonnes != null) 'capaciteTonnes': capaciteTonnes,
        if (disponibleDe != null) 'disponibleDe': disponibleDe.toIso8601String(),
      });
      await DeclarationLocalDb.mettreAJour(
        idLocal,
        typeCamion: typeCamion,
        capaciteTonnes: capaciteTonnes,
        disponibleDe: disponibleDe,
      );
      state = state.copyWith(succes: 'Déclaration modifiée ✅');
      await _chargerLocal();
      return true;
    } on DioException catch (e) {
      state = state.copyWith(
        erreur: 'Modification impossible : ${e.response?.data ?? e.message}',
      );
      return false;
    }
  }

  // ── Supprimer une déclaration ──────────────────────────────
  Future<bool> supprimer({required String idLocal, required String? missionId}) async {
    if (missionId != null) {
      try {
        await _dio.delete('/missions/mes-declarations/$missionId');
      } on DioException catch (e) {
        state = state.copyWith(
          erreur: 'Suppression impossible : ${e.response?.data ?? e.message}',
        );
        return false;
      }
    }
    await DeclarationLocalDb.supprimer(idLocal);
    state = state.copyWith(succes: 'Déclaration supprimée 🗑️');
    await _chargerLocal();
    return true;
  }

  // ── Envoi au serveur avec idempotence ─────────────────────
  Future<bool> _envoyerAuServeur(DeclarationVideModel d) async {
    try {
      final response = await _dio.post('/missions/declare-vide',
        data: {
          'axeId': d.axeId,
          'latitude': d.latitude,
          'longitude': d.longitude,
          'typeCamion': d.typeCamion,
          'capaciteTonnes': d.capaciteTonnes,
          if (d.disponibleDe != null) 'disponibleDe': d.disponibleDe!.toIso8601String(),
        },
        options: Options(headers: {'X-Idempotency-Key': d.idLocal}),
      );
      final missionId = response.data['id']?.toString();
      await DeclarationLocalDb.marquerSynchronise(d.idLocal, missionId: missionId);
      await _chargerLocal();
      return true;
    } on DioException {
      return false;
    }
  }

  // ── Synchroniser toutes les déclarations en attente ───────
  Future<void> synchroniserEnAttente() async {
    final enAttente = await DeclarationLocalDb.getNonSynchronisees();
    for (final d in enAttente) {
      await _envoyerAuServeur(d);
    }
  }
}

final declarationVideProvider =
    StateNotifierProvider<DeclarationVideNotifier, DeclarationVideState>((ref) {
  final dio = ref.watch(dioProvider);
  return DeclarationVideNotifier(dio, ref);
});
