import 'dart:async';
import 'dart:io' show Platform;

import 'package:geolocator/geolocator.dart';

class GpsService {
  static StreamSubscription<Position>? _abonnementFlux;

  static Future<bool> verifierPermissions() async {
    bool serviceActive = await Geolocator.isLocationServiceEnabled();
    if (!serviceActive) return false;

    LocationPermission permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.denied) return false;
    }
    if (permission == LocationPermission.deniedForever) return false;

    return true;
  }

  // Tente un fix GPS frais, avec repli sur la dernière position connue
  // (ENF-OFF-01 : zéro perte de donnée même sans fix GPS rapide)
  static Future<Position?> positionActuelle() async {
    final autorise = await verifierPermissions();
    if (!autorise) return null;

    try {
      return await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(
          accuracy: LocationAccuracy.high,
          timeLimit: Duration(seconds: 15),
        ),
      );
    } catch (e) {
      // Pas de fix frais (fréquent hors-ligne/indoor) → dernière position connue
      try {
        final derniere = await Geolocator.getLastKnownPosition();
        return derniere;
      } catch (_) {
        return null;
      }
    }
  }

  /// Demande la permission "toujours" (arrière-plan), en plus de "pendant
  /// l'utilisation" déjà couverte par [verifierPermissions]. Sans elle, le
  /// suivi continu s'interrompt dès que l'app passe en arrière-plan sur
  /// certaines versions d'Android/iOS. Échoue silencieusement si refusée —
  /// le suivi reste fonctionnel app ouverte, juste pas en tâche de fond.
  static Future<void> demanderPermissionArrierePlan() async {
    final permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.whileInUse) {
      await Geolocator.requestPermission();
    }
  }

  static LocationSettings _parametresSuiviContinu() {
    if (Platform.isAndroid) {
      return AndroidSettings(
        accuracy: LocationAccuracy.high,
        distanceFilter: 100, // mètres — limite le volume réseau/batterie
        intervalDuration: const Duration(seconds: 30),
        // Service de premier plan Android : maintient le suivi actif quand
        // l'app est minimisée, avec une notification persistante visible
        // par le chauffeur (transparence — pas de géolocalisation cachée).
        foregroundNotificationConfig: const ForegroundNotificationConfig(
          notificationTitle: 'Suivi de mission actif',
          notificationText:
              'FretCorridor transmet votre position pendant la mission en cours.',
        ),
      );
    }
    if (Platform.isIOS) {
      return AppleSettings(
        accuracy: LocationAccuracy.high,
        distanceFilter: 100,
        pauseLocationUpdatesAutomatically: false,
        allowBackgroundLocationUpdates: true,
        showBackgroundLocationIndicator: true,
      );
    }
    return const LocationSettings(accuracy: LocationAccuracy.high, distanceFilter: 100);
  }

  /// Démarre un flux continu de positions (EF-TRK-01 — tâche de fond réelle).
  /// Un seul flux actif à la fois : un appel annule d'abord tout flux en
  /// cours. L'appelant doit appeler [arreterFlux] à la fin de la mission ou
  /// à la déconnexion.
  static Future<bool> demarrerFlux(
    void Function(Position) onPosition, {
    void Function(Object)? onErreur,
  }) async {
    final autorise = await verifierPermissions();
    if (!autorise) return false;

    await demanderPermissionArrierePlan();
    await arreterFlux();

    _abonnementFlux = Geolocator.getPositionStream(
      locationSettings: _parametresSuiviContinu(),
    ).listen(onPosition, onError: onErreur);

    return true;
  }

  static Future<void> arreterFlux() async {
    await _abonnementFlux?.cancel();
    _abonnementFlux = null;
  }

  static bool get suiviEnCours => _abonnementFlux != null;
}
