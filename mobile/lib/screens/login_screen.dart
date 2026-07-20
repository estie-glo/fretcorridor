import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/auth_provider.dart';
import '../theme/app_theme.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  final _formKey       = GlobalKey<FormState>();
  final _telController = TextEditingController();
  final _pinController = TextEditingController();
  bool _pinVisible      = false;
  String _roleSelectionne = 'CHAUFFEUR';

  @override
  void dispose() {
    _telController.dispose();
    _pinController.dispose();
    super.dispose();
  }

  void _login() {
    if (_formKey.currentState!.validate()) {
      ref.read(authProvider.notifier).login(
        _telController.text.trim(),
        _pinController.text.trim(),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authProvider);

    ref.listen(authProvider, (previous, next) {
      if (next.estConnecte && next.utilisateur != null) {
        final role = next.utilisateur!.role;
        if (role == 'CHAUFFEUR') {
          Navigator.pushReplacementNamed(context, '/dashboard-chauffeur');
        } else if (role == 'AGENT') {
          Navigator.pushReplacementNamed(context, '/dashboard-agent');
        } else if (role == 'CLIENT') {
          Navigator.pushReplacementNamed(context, '/dashboard-client');
        }
      }
    });

    return Scaffold(
      backgroundColor: AppColors.fond,
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: 48),

                Center(
                  child: Image.asset('assets/images/flysoft_logo.png', height: 64),
                ),
                const SizedBox(height: 32),

                Text('FretCorridor', style: Theme.of(context).textTheme.headlineLarge),
                const SizedBox(height: 4),
                const Text('Réseau logistique CEMAC',
                    style: TextStyle(fontSize: 14, color: AppColors.texteMuet)),
                const SizedBox(height: 36),

                // ── Sélection rôle ───────────────────────
                Row(
                  children: ['CHAUFFEUR', 'AGENT'].map((role) {
                    final actif = _roleSelectionne == role;
                    return Expanded(
                      child: GestureDetector(
                        onTap: () => setState(() => _roleSelectionne = role),
                        child: Container(
                          margin: const EdgeInsets.only(right: 8),
                          padding: const EdgeInsets.symmetric(vertical: 12),
                          decoration: BoxDecoration(
                            color: actif ? AppColors.accent : AppColors.surface,
                            borderRadius: BorderRadius.circular(10),
                            border: Border.all(
                              color: actif ? AppColors.accent : AppColors.bordure,
                            ),
                          ),
                          child: Text(
                            role == 'CHAUFFEUR' ? '🚛  Chauffeur' : '👤  Agent',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 13,
                              color: actif ? AppColors.texteBouton : AppColors.texteMuet,
                            ),
                          ),
                        ),
                      ),
                    );
                  }).toList(),
                ),
                const SizedBox(height: 28),

                const Text('TÉLÉPHONE',
                    style: TextStyle(fontSize: 11, letterSpacing: 1.2,
                        color: AppColors.texteMuet, fontWeight: FontWeight.w600)),
                const SizedBox(height: 8),
                TextFormField(
                  controller: _telController,
                  keyboardType: TextInputType.phone,
                  style: const TextStyle(color: AppColors.texte, fontSize: 15),
                  decoration: InputDecoration(
                    hintText: '+237 6XX XXX XXX',
                    hintStyle: const TextStyle(color: Color(0xFF9CA3AF)),
                    filled: true,
                    fillColor: AppColors.surface,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10),
                      borderSide: const BorderSide(color: AppColors.bordure),
                    ),
                    enabledBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10),
                      borderSide: const BorderSide(color: AppColors.bordure),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10),
                      borderSide: const BorderSide(color: AppColors.accent),
                    ),
                    prefixIcon: const Icon(Icons.phone, color: AppColors.texteMuet),
                  ),
                  validator: (v) {
                    if (v == null || v.isEmpty) return 'Téléphone obligatoire';
                    if (!RegExp(r'^\+?[0-9]{9,15}$').hasMatch(v)) {
                      return 'Format invalide (ex: +237 6XX XXX XXX)';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 20),

                const Text('CODE PIN',
                    style: TextStyle(fontSize: 11, letterSpacing: 1.2,
                        color: AppColors.texteMuet, fontWeight: FontWeight.w600)),
                const SizedBox(height: 8),
                TextFormField(
                  controller: _pinController,
                  obscureText: !_pinVisible,
                  keyboardType: TextInputType.number,
                  maxLength: 6,
                  style: const TextStyle(color: AppColors.texte, fontSize: 20, letterSpacing: 8),
                  decoration: InputDecoration(
                    hintText: '• • • •',
                    hintStyle: const TextStyle(color: Color(0xFF9CA3AF)),
                    counterText: '',
                    filled: true,
                    fillColor: AppColors.surface,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10),
                      borderSide: const BorderSide(color: AppColors.bordure),
                    ),
                    enabledBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10),
                      borderSide: const BorderSide(color: AppColors.bordure),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(10),
                      borderSide: const BorderSide(color: AppColors.accent),
                    ),
                    prefixIcon: const Icon(Icons.lock, color: AppColors.texteMuet),
                    suffixIcon: IconButton(
                      icon: Icon(
                        _pinVisible ? Icons.visibility_off : Icons.visibility,
                        color: AppColors.texteMuet,
                      ),
                      onPressed: () => setState(() => _pinVisible = !_pinVisible),
                    ),
                  ),
                  validator: (v) {
                    if (v == null || v.isEmpty) return 'PIN obligatoire';
                    if (!RegExp(r'^[0-9]{4,6}$').hasMatch(v)) {
                      return 'Le PIN doit contenir 4 à 6 chiffres';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 12),

                if (authState.erreur != null)
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: AppColors.erreur.withValues(alpha: 0.08),
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: AppColors.erreur.withValues(alpha: 0.4)),
                    ),
                    child: Row(
                      children: [
                        const Icon(Icons.warning_amber, color: AppColors.erreur, size: 18),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(authState.erreur!,
                              style: const TextStyle(color: AppColors.erreur, fontSize: 13)),
                        ),
                      ],
                    ),
                  ),
                const SizedBox(height: 28),

                SizedBox(
                  width: double.infinity,
                  height: 52,
                  child: ElevatedButton(
                    onPressed: authState.chargement ? null : _login,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.accent,
                      disabledBackgroundColor: AppColors.accent.withValues(alpha: 0.5),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                    child: authState.chargement
                        ? const SizedBox(
                            height: 22, width: 22,
                            child: CircularProgressIndicator(
                                color: AppColors.texteBouton, strokeWidth: 2.5))
                        : Text('Se connecter',
                            style: TextStyle(
                                fontSize: 16, fontWeight: FontWeight.bold,
                                color: AppColors.texteBouton)),
                  ),
                ),
                const SizedBox(height: 20),

                Center(
                  child: Text(
                    'Pas de compte ? Contactez votre agent terrain.',
                    style: TextStyle(fontSize: 12, color: AppColors.texteMuet.withValues(alpha: 0.8)),
                  ),
                ),
                const SizedBox(height: 32),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
