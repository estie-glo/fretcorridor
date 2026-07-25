import 'package:flutter_riverpod/legacy.dart';
import '../services/auth_service.dart';
import '../models/utilisateur_model.dart';
import 'dio_provider.dart';

// ── État de l'authentification ────────────────────────────────
class AuthState {
  final UtilisateurModel? utilisateur;
  final bool estConnecte;
  final bool chargement;
  final String? erreur;
  final int tentativesRestantes;

  const AuthState({
    this.utilisateur,
    this.estConnecte = false,
    this.chargement = false,
    this.erreur,
    this.tentativesRestantes = 3,
  });

  AuthState copyWith({
    UtilisateurModel? utilisateur,
    bool? estConnecte,
    bool? chargement,
    String? erreur,
    int? tentativesRestantes,
  }) {
    return AuthState(
      utilisateur: utilisateur ?? this.utilisateur,
      estConnecte: estConnecte ?? this.estConnecte,
      chargement: chargement ?? this.chargement,
      erreur: erreur,
      tentativesRestantes: tentativesRestantes ?? this.tentativesRestantes,
    );
  }
}

// ── Notifier (gère les actions) ───────────────────────────────
class AuthNotifier extends StateNotifier<AuthState> {
  final AuthService _authService;

  AuthNotifier(this._authService) : super(const AuthState()) {
    // Vérifier si déjà connecté au démarrage
    _verifierSession();
  }

  // Vérifier si un token valide existe au démarrage
  Future<void> _verifierSession() async {
    state = state.copyWith(chargement: true);
    try {
      final utilisateur = await _authService.recupererSessionLocale();
      if (utilisateur != null) {
        state = state.copyWith(
          utilisateur: utilisateur,
          estConnecte: true,
          chargement: false,
        );
      } else {
        state = state.copyWith(chargement: false);
      }
    } catch (e) {
      state = state.copyWith(chargement: false);
    }
  }

  // ── Login ─────────────────────────────────────────────────
  Future<void> login(String telephone, String codePin) async {
    state = state.copyWith(chargement: true, erreur: null);

    try {
      final utilisateur = await _authService.login(telephone, codePin);
      state = state.copyWith(
        utilisateur: utilisateur,
        estConnecte: true,
        chargement: false,
        erreur: null,
      );
    } catch (e) {
      final message = e.toString();

      if (message.contains('PIN_INCORRECT')) {
        final restantes = int.tryParse(
          message.split(':').last.replaceAll(')', '')) ?? 0;
        state = state.copyWith(
          chargement: false,
          erreur: 'PIN incorrect — $restantes essai(s) restant(s)',
          tentativesRestantes: restantes,
        );
      } else if (message.contains('COMPTE_BLOQUE')) {
        state = state.copyWith(
          chargement: false,
          erreur: 'Compte bloqué. Contactez votre agent.',
        );
      } else if (message.contains('UTILISATEUR_INTROUVABLE')) {
        state = state.copyWith(
          chargement: false,
          erreur: 'Numéro non reconnu. Contactez votre agent.',
        );
      } else {
        state = state.copyWith(
          chargement: false,
          erreur: 'Erreur de connexion. Vérifiez votre réseau.',
        );
      }
    }
  }

  // ── Logout ────────────────────────────────────────────────
  Future<void> logout() async {
    await _authService.logout();
    state = const AuthState();
  }
}

// ── Provider exposé à toute l'app ────────────────────────────
final authProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  final dio = ref.watch(dioProvider);
  return AuthNotifier(AuthService(dio));
});
