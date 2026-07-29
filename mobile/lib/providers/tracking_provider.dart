import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:geolocator/geolocator.dart';
import 'package:uuid/uuid.dart';
import '../models/position_pending_model.dart';
import '../services/declaration_local_db.dart';
import '../services/gps_service.dart';
import 'connectivity_provider.dart';
import 'dio_provider.dart';

class TrackingState {
  final String? missionIdActive;
  final bool envoiEnCours;
  final bool suiviAutoActif;
  final int positionsEnAttente;
  final String? erreur;
  final String? succes;

  const TrackingState({
    this.missionIdActive,
    this.envoiEnCours = false,
    this.suiviAutoActif = false,
    this.positionsEnAttente = 0,
    this.erreur,
    this.succes,
  });

  TrackingState copyWith({
    String? missionIdActive,
    bool? envoiEnCours,
    bool? suiviAutoActif,
    int? positionsEnAttente,
    String? erreur,
    String? succes,
  }) {
    return TrackingState(
      missionIdActive: missionIdActive ?? this.missionIdActive,
      envoiEnCours: envoiEnCours ?? this.envoiEnCours,
      suiviAutoActif: suiviAutoActif ?? this.suiviAutoActif,
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
    await _rafraichirCompteurs();
  }

  // ── Capture manuelle ponctuelle (bouton "Envoyer ma position") ────────
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

    await _traiterPosition(position, missionId);
    state = state.copyWith(envoiEnCours: false);
    return true;
  }

  // ── Suivi continu en tâche de fond (EF-TRK-01) ─────────────────────────
  // Démarre un flux GPS qui continue même app minimisée (service de premier
  // plan Android avec notification persistante ; background updates iOS).
  Future<void> demarrerSuiviAuto() async {
    final missionId = state.missionIdActive;
    if (missionId == null || state.suiviAutoActif) {
      return;
    }

    final demarre = await GpsService.demarrerFlux(
      (position) => _traiterPosition(position, missionId),
      onErreur: (_) => state = state.copyWith(
        erreur: 'Suivi GPS interrompu. Réactivez la localisation.',
        suiviAutoActif: false,
      ),
    );

    state = state.copyWith(suiviAutoActif: demarre);
  }

  Future<void> arreterSuiviAuto() async {
    await GpsService.arreterFlux();
    state = state.copyWith(suiviAutoActif: false);
  }

  // Appelé après une déclaration (ou sa suppression) pour resynchroniser
  // l'état "mission active" et démarrer/arrêter le suivi en conséquence.
  Future<void> rafraichirEtDemarrer() async {
    await _rafraichirCompteurs();
    if (state.missionIdActive != null) {
      await demarrerSuiviAuto();
    } else {
      await arreterSuiviAuto();
    }
  }

  // ── Traitement commun capture manuelle / flux continu ──────────────────
  Future<void> _traiterPosition(Position position, String missionId) async {
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
        succes: ok
            ? 'Position envoyée ✅'
            : 'Enregistrée localement — sync en attente 🔄',
      );
    } else {
      state = state.copyWith(succes: 'Hors ligne — position en attente de sync 📴');
    }
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

  @override
  void dispose() {
    // Sécurité : ne jamais laisser un flux GPS tourner sans notifier vivant
    // pour recevoir ses callbacks (évite un crash "used after dispose").
    GpsService.arreterFlux();
    super.dispose();
  }
}

final trackingProvider =
    StateNotifierProvider<TrackingNotifier, TrackingState>((ref) {
  return TrackingNotifier(ref.watch(dioProvider), ref);
});
