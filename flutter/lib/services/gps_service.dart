import 'package:geolocator/geolocator.dart';

class GpsService {
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
}
