import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../models/utilisateur_model.dart';
import '../providers/dio_provider.dart';

class AuthService {
  final Dio _dio;
  final FlutterSecureStorage _storage;

  static const String _keyUtilisateur = 'utilisateur';

  AuthService(this._dio) : _storage = const FlutterSecureStorage();

  // ── LOGIN 
  Future<UtilisateurModel> login(String telephone, String codePin) async {
    final response = await _dio.post('/auth/login', data: {
      'telephone': telephone,
      'codePin': codePin,
    });

    final data = response.data;

    await _storage.write(key: keyAccessToken, value: data['accessToken']);
    await _storage.write(key: keyRefreshToken, value: data['refreshToken']);

    final utilisateur = UtilisateurModel.fromJson(data);
    await _storage.write(key: _keyUtilisateur, value: utilisateur.toJsonString());

    return utilisateur;
  }

  // ── RÉCUPÉRER SESSION LOCALE 
  Future<UtilisateurModel?> recupererSessionLocale() async {
    final token = await _storage.read(key: keyAccessToken);
    final userJson = await _storage.read(key: _keyUtilisateur);
    if (token == null || userJson == null) return null;
    return UtilisateurModel.fromJsonString(userJson);
  }

  // ── CHANGER LE PIN (forcé si pinTemporaire, ou volontaire) 
  Future<void> changerPin(String ancienPin, String nouveauPin) async {
    await _dio.put('/auth/changer-pin', data: {
      'ancienPin': ancienPin,
      'nouveauPin': nouveauPin,
    });

    // Mettre à jour la session locale : le PIN n'est plus temporaire
    final userJson = await _storage.read(key: _keyUtilisateur);
    if (userJson != null) {
      final utilisateur = UtilisateurModel.fromJsonString(userJson)
          .copyWith(pinTemporaire: false);
      await _storage.write(key: _keyUtilisateur, value: utilisateur.toJsonString());
    }
  }

  // ── LOGOUT 
  Future<void> logout() async {
    try {
      await _dio.post('/auth/logout');
    } catch (_) {
      // Même si l'appel échoue, on efface localement
    } finally {
      await _effacerSession();
    }
  }

  // ── EFFACER SESSION LOCALE 
  Future<void> _effacerSession() async {
    await _storage.delete(key: keyAccessToken);
    await _storage.delete(key: keyRefreshToken);
    await _storage.delete(key: _keyUtilisateur);
  }

  // ── METTRE À JOUR FCM TOKEN 
  Future<void> mettreAJourFcmToken(String fcmToken) async {
    await _dio.put('/auth/fcm-token', data: {'fcmToken': fcmToken});
  }

  // ── GETTER TOKEN (pour autres services) 
  Future<String?> getAccessToken() => _storage.read(key: keyAccessToken);
}

