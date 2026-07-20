import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'dart:async';

enum StatutConnexion { enLigne, horsLigne, inconnu }

class ConnectivityNotifier extends StateNotifier<StatutConnexion> {
  final Connectivity _connectivity = Connectivity();
  StreamSubscription? _subscription;

  ConnectivityNotifier() : super(StatutConnexion.inconnu) {
    _verifierInitial();
    _subscription = _connectivity.onConnectivityChanged.listen((results) {
      _mettreAJourStatut(results);
    });
  }

  Future<void> _verifierInitial() async {
    final results = await _connectivity.checkConnectivity();
    _mettreAJourStatut(results);
  }

  void _mettreAJourStatut(List<ConnectivityResult> results) {
    final connecte = results.any((r) => r != ConnectivityResult.none);
    state = connecte ? StatutConnexion.enLigne : StatutConnexion.horsLigne;
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }
}

final connectivityProvider = StateNotifierProvider<ConnectivityNotifier, StatutConnexion>((ref) {
  return ConnectivityNotifier();
});
