import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:uuid/uuid.dart';
import '../models/position_pending_model.dart';
import '../services/declaration_local_db.dart';
import '../services/gps_service.dart';
import 'connectivity_provider.dart';
import 'dio_provider.dart';

class TrackingState {
  final String? missionIdActive;
  final bool envoiEnCours;
  final int positionsEnAttente;
  final String? erreur;
  final String? succes;

  const TrackingState({
    this.missionIdActive,
    this.envoiEnCours = false,
    this.positionsEnAttente = 0,
    this.erreur,
    this.succes,
  });

  TrackingState copyWith({
    String? missionIdActive,
    bool? envoiEnCours,
    int? positionsEnAttente,
    String? erreur,
    String? succes,
  }) {
    return TrackingState(
      missionIdActive: missionIdActive ?? this.missionIdActive,
      envoiEnCours: envoiEnCours ?? this.envoiEnCours,
      positionsEnAttente: positionsEnAttente ?? this.positionsEnAttente,
      erreur: erreur,
      succes: succes,
    );
  }
}

class TrackingNotifier extends StateNotifier<TrackingState> {
  final Dio _dio;
  final Ref _ref;
  static const _uuid = Uuid();

  TrackingNotifier(this._dio, this._ref) : super(const TrackingState()) {
    _initialiser();
  }

  Future<void> _initialiser() async {
    final missionId = await DeclarationLocalDb.derniereMissionIdSynchronisee();
    final enAttente = await DeclarationLocalDb.getPositionsNonSynchronisees();
    state = state.copyWith(
      missionIdActive: missionId,
      positionsEnAttente: enAttente.length,
    );
  }

  Future<bool> capturerEtEnvoyerPosition() async {
    final missionId = state.missionIdActive;
    if (missionId == null) {
      state = state.copyWith(
        erreur: 'Aucune mission active — déclarez d\'abord un camion vide.',
      );
      return false;
    }

    state = state.copyWith(envoiEnCours: true, erreur: null, succes: null);

    final position = await GpsService.positionActuelle();
    if (position == null) {
      state = state.copyWith(
        envoiEnCours: false,
        erreur: 'GPS indisponible. Activez la localisation.',
      );
      return false;
    }

    final pending = PositionPendingModel(
      idLocal: _uuid.v4(),
      missionId: missionId,
      latitude: position.latitude,
      longitude: position.longitude,
      recordedAt: DateTime.now(),
      vitesseKmh: position.speed >= 0 ? position.speed * 3.6 : null,
      precisionMetres: position.accuracy,
    );

    await DeclarationLocalDb.ajouterPosition(pending);
    await _rafraichirCompteurs();

    final enLigne = _ref.read(connectivityProvider) == StatutConnexion.enLigne;
    if (enLigne) {
      final ok = await synchroniserEnAttente();
      state = state.copyWith(
        envoiEnCours: false,
        succes: ok
            ? 'Position envoyée ✅'
            : 'Enregistrée localement — sync en attente 🔄',
      );
    } else {
      state = state.copyWith(
        envoiEnCours: false,
        succes: 'Hors ligne — position en attente de sync 📴',
      );
    }
    return true;
  }

  Future<bool> synchroniserEnAttente() async {
    final enAttente = await DeclarationLocalDb.getPositionsNonSynchronisees();
    if (enAttente.isEmpty) {
      await _rafraichirCompteurs();
      return true;
    }

    final parMission = <String, List<PositionPendingModel>>{};
    for (final p in enAttente) {
      parMission.putIfAbsent(p.missionId, () => []).add(p);
    }

    var ok = true;
    for (final entry in parMission.entries) {
      try {
        await _dio.post('/positions', data: {
          'missionId': entry.key,
          'positions': entry.value.map((p) => p.toApiJson()).toList(),
        });
        await DeclarationLocalDb.marquerPositionsSynchronisees(
          entry.value.map((p) => p.idLocal).toList(),
        );
      } on DioException {
        ok = false;
      }
    }

    await _rafraichirCompteurs();
    return ok;
  }

  Future<void> _rafraichirCompteurs() async {
    final missionId = await DeclarationLocalDb.derniereMissionIdSynchronisee();
    final enAttente = await DeclarationLocalDb.getPositionsNonSynchronisees();
    state = state.copyWith(
      missionIdActive: missionId,
      positionsEnAttente: enAttente.length,
    );
  }
}

final trackingProvider =
    StateNotifierProvider<TrackingNotifier, TrackingState>((ref) {
  return TrackingNotifier(ref.watch(dioProvider), ref);
});
