import 'package:flutter_riverpod/flutter_riverpod.dart';
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

  // ── Déclarer un camion vide (offline-first) ───────────────
  Future<bool> declarer({
    required String axeId,
    required String axeNom,
    required String typeCamion,
    required double capaciteTonnes,
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

  // ── Envoi au serveur avec idempotence ─────────────────────
  Future<bool> _envoyerAuServeur(DeclarationVideModel d) async {
    try {
      await _dio.post('/missions/declare-vide',
        data: {
          'axeId': d.axeId,
          'latitude': d.latitude,
          'longitude': d.longitude,
          'typeCamion': d.typeCamion,
          'capaciteTonnes': d.capaciteTonnes,
        },
        options: Options(headers: {'X-Idempotency-Key': d.idLocal}),
      );
      await DeclarationLocalDb.marquerSynchronise(d.idLocal);
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
