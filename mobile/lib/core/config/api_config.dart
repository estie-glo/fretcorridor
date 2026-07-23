/// Configuration réseau de l'app (compile-time via `--dart-define`).
///
/// Exemples :
/// ```bash
/// # Émulateur Android → machine hôte
/// flutter run --dart-define=API_BASE=http://10.0.2.2:8080/api
///
/// # Appareil physique (même WiFi que le backend)
/// flutter run --dart-define=API_BASE=http://192.168.1.53:8080/api
///
/// # Release
/// flutter build apk --dart-define=API_BASE=https://api.example.com/api
/// ```
class ApiConfig {
  ApiConfig._();

  /// URL de base incluant le préfixe `/api`.
  static const String baseUrl = String.fromEnvironment(
    'API_BASE',
    defaultValue: 'http://127.0.0.1:8080/api',
  );
}
