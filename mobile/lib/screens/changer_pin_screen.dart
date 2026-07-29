import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/auth_provider.dart';
import '../theme/app_theme.dart';

/// EF-IDA-03 (extension) — écran de changement de PIN obligatoire, affiché
/// après la première connexion lorsque le PIN a été fixé par un agent/admin
/// (pinTemporaire = true). Le retour arrière est désactivé : impossible de
/// continuer à utiliser l'app tant que le PIN n'a pas été changé.
class ChangerPinScreen extends ConsumerStatefulWidget {
  const ChangerPinScreen({super.key});

  @override
  ConsumerState<ChangerPinScreen> createState() => _ChangerPinScreenState();
}

class _ChangerPinScreenState extends ConsumerState<ChangerPinScreen> {
  final _formKey = GlobalKey<FormState>();
  final _ancienCtrl = TextEditingController();
  final _nouveauCtrl = TextEditingController();
  final _confirmationCtrl = TextEditingController();
  bool _pinVisible = false;

  @override
  void dispose() {
    _ancienCtrl.dispose();
    _nouveauCtrl.dispose();
    _confirmationCtrl.dispose();
    super.dispose();
  }

  Future<void> _valider() async {
    if (!_formKey.currentState!.validate()) return;

    final succes = await ref.read(authProvider.notifier).changerPin(
          _ancienCtrl.text.trim(),
          _nouveauCtrl.text.trim(),
        );

    if (succes && mounted) {
      final role = ref.read(authProvider).utilisateur?.role;
      switch (role) {
        case 'CHAUFFEUR':
          Navigator.pushReplacementNamed(context, '/dashboard-chauffeur');
          break;
        case 'AGENT':
          Navigator.pushReplacementNamed(context, '/dashboard-agent');
          break;
        default:
          Navigator.pushReplacementNamed(context, '/dashboard-client');
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authProvider);

    return PopScope(
      canPop: false, // changement obligatoire — pas de retour arrière
      child: Scaffold(
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
                  Text('Nouveau PIN requis',
                      style: Theme.of(context).textTheme.headlineMedium),
                  const SizedBox(height: 8),
                  const Text(
                    'Votre PIN vous a été communiqué par votre agent. '
                    'Choisissez-en un nouveau, connu de vous seul, avant '
                    'de continuer.',
                    style: TextStyle(fontSize: 13, color: AppColors.texteMuet),
                  ),
                  const SizedBox(height: 28),

                  _label('PIN ACTUEL (reçu par l\'agent ou par SMS)'),
                  _champPin(_ancienCtrl, 'Ex : 1234'),
                  const SizedBox(height: 20),

                  _label('NOUVEAU PIN (4-6 chiffres)'),
                  _champPin(_nouveauCtrl, 'Choisissez un nouveau PIN'),
                  const SizedBox(height: 20),

                  _label('CONFIRMER LE NOUVEAU PIN'),
                  TextFormField(
                    controller: _confirmationCtrl,
                    obscureText: !_pinVisible,
                    keyboardType: TextInputType.number,
                    maxLength: 6,
                    style: const TextStyle(color: AppColors.texte, fontSize: 16),
                    decoration: _decorationPin('Répétez le nouveau PIN'),
                    validator: (v) {
                      if (v != _nouveauCtrl.text) return 'Les PIN ne correspondent pas';
                      return null;
                    },
                  ),
                  const SizedBox(height: 8),

                  Row(
                    children: [
                      Checkbox(
                        value: _pinVisible,
                        activeColor: AppColors.accent,
                        onChanged: (v) => setState(() => _pinVisible = v ?? false),
                      ),
                      const Text('Afficher les PIN',
                          style: TextStyle(fontSize: 12, color: AppColors.texteMuet)),
                    ],
                  ),

                  if (authState.erreur != null) ...[
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: AppColors.erreur.withValues(alpha: 0.08),
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(color: AppColors.erreur.withValues(alpha: 0.4)),
                      ),
                      child: Row(children: [
                        const Icon(Icons.warning_amber, color: AppColors.erreur, size: 18),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(authState.erreur!,
                              style: const TextStyle(color: AppColors.erreur, fontSize: 13)),
                        ),
                      ]),
                    ),
                    const SizedBox(height: 16),
                  ],

                  const SizedBox(height: 12),
                  SizedBox(
                    width: double.infinity,
                    height: 52,
                    child: ElevatedButton(
                      onPressed: authState.chargement ? null : _valider,
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
                          : Text('Valider le nouveau PIN',
                              style: TextStyle(
                                  fontSize: 16, fontWeight: FontWeight.bold,
                                  color: AppColors.texteBouton)),
                    ),
                  ),
                  const SizedBox(height: 32),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _label(String texte) => Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: Text(texte,
            style: const TextStyle(
                fontSize: 11, letterSpacing: 1.0,
                color: AppColors.texteMuet, fontWeight: FontWeight.w600)),
      );

  Widget _champPin(TextEditingController controller, String hint) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: TextFormField(
        controller: controller,
        obscureText: !_pinVisible,
        keyboardType: TextInputType.number,
        maxLength: 6,
        style: const TextStyle(color: AppColors.texte, fontSize: 16),
        decoration: _decorationPin(hint),
        validator: (v) {
          if (v == null || v.isEmpty) return 'PIN obligatoire';
          if (!RegExp(r'^[0-9]{4,6}$').hasMatch(v)) return 'PIN : 4 à 6 chiffres';
          return null;
        },
      ),
    );
  }

  InputDecoration _decorationPin(String hint) => InputDecoration(
        hintText: hint,
        hintStyle: const TextStyle(color: Color(0xFF9CA3AF), fontSize: 13),
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
        prefixIcon: const Icon(Icons.lock_outline, color: AppColors.texteMuet),
      );
}
