import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl_phone_field/intl_phone_field.dart';
import '../providers/chauffeur_provider.dart';
import '../theme/app_theme.dart';

class EnrolementScreen extends ConsumerStatefulWidget {
  final bool asSheet;
  const EnrolementScreen({super.key, this.asSheet = false});

  @override
  ConsumerState<EnrolementScreen> createState() => _EnrolementScreenState();
}

class _EnrolementScreenState extends ConsumerState<EnrolementScreen> {
  final _formKey    = GlobalKey<FormState>();
  final _nomCtrl    = TextEditingController();
  final _prenomCtrl = TextEditingController();
  final _pinCtrl    = TextEditingController();
  final _cniCtrl    = TextEditingController();
  String _telephoneComplet = '';

  @override
  void dispose() {
    _nomCtrl.dispose(); _prenomCtrl.dispose();
    _pinCtrl.dispose(); _cniCtrl.dispose();
    super.dispose();
  }

  Future<void> _enroler() async {
    if (!_formKey.currentState!.validate()) return;
    if (_telephoneComplet.isEmpty) return;

    final succes = await ref.read(chauffeurProvider.notifier).enroler(
      nom:       _nomCtrl.text.trim(),
      prenom:    _prenomCtrl.text.trim(),
      telephone: _telephoneComplet,
      codePin:   _pinCtrl.text.trim(),
      numeroCNI: _cniCtrl.text.trim(),
    );

    if (succes && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Chauffeur enrôlé avec succès ✅'),
          backgroundColor: AppColors.succes,
        ),
      );
      Navigator.pop(context);
    }
  }

  @override
  Widget build(BuildContext context) {
    final chauffeurState = ref.watch(chauffeurProvider);
    final chargement     = chauffeurState.chargement;
    final erreur         = chauffeurState.erreur;

    final contenu = SingleChildScrollView(
      padding: EdgeInsets.only(
        left: 20, right: 20, top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
      ),
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [

            if (widget.asSheet) ...[
              Center(
                child: Container(
                  width: 40, height: 4,
                  decoration: BoxDecoration(
                    color: AppColors.bordure,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('Enrôler un chauffeur', style: Theme.of(context).textTheme.headlineMedium),
                  IconButton(
                    icon: const Icon(Icons.close, color: AppColors.texteMuet),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
              const SizedBox(height: 16),
            ],

            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: AppColors.accentProfond.withValues(alpha: 0.4)),
              ),
              child: const Row(children: [
                Icon(Icons.info_outline, color: AppColors.accentProfond, size: 18),
                SizedBox(width: 10),
                Expanded(child: Text(
                  'Le chauffeur pourra se connecter avec ce numéro et ce PIN.',
                  style: TextStyle(color: AppColors.texteMuet, fontSize: 12),
                )),
              ]),
            ),
            const SizedBox(height: 24),

            _buildLabel('PRÉNOM'),
            _buildField(controller: _prenomCtrl, hint: 'Ex : Moussa', icon: Icons.person,
                validator: (v) => v!.isEmpty ? 'Prénom obligatoire' : null),
            const SizedBox(height: 16),

            _buildLabel('NOM'),
            _buildField(controller: _nomCtrl, hint: 'Ex : Abdoulaye', icon: Icons.person_outline,
                validator: (v) => v!.isEmpty ? 'Nom obligatoire' : null),
            const SizedBox(height: 16),

            _buildLabel('TÉLÉPHONE'),
            IntlPhoneField(
              initialCountryCode: 'CM',
              style: const TextStyle(color: AppColors.texte, fontSize: 14),
              dropdownTextStyle: const TextStyle(color: AppColors.texte),
              decoration: InputDecoration(
                hintText: '6XX XXX XXX',
                hintStyle: const TextStyle(color: Color(0xFF6B6357)),
                filled: true,
                fillColor: AppColors.surface,
                border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(10),
                    borderSide: const BorderSide(color: AppColors.bordure)),
                enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(10),
                    borderSide: const BorderSide(color: AppColors.bordure)),
                focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(10),
                    borderSide: const BorderSide(color: AppColors.accent)),
              ),
              dropdownIconPosition: IconPosition.trailing,
              flagsButtonPadding: const EdgeInsets.only(left: 12),
              onChanged: (phone) {
                _telephoneComplet = phone.completeNumber;
              },
              validator: (phone) {
                if (phone == null || phone.number.isEmpty) return 'Téléphone obligatoire';
                return null;
              },
            ),
            const SizedBox(height: 16),

            _buildLabel('CODE PIN INITIAL (4-6 chiffres)'),
            _buildField(
              controller: _pinCtrl, hint: 'Ex : 1234', icon: Icons.lock,
              keyboardType: TextInputType.number, obscure: true, maxLength: 6,
              validator: (v) {
                if (v!.isEmpty) return 'PIN obligatoire';
                if (!RegExp(r'^[0-9]{4,6}$').hasMatch(v)) return 'PIN : 4 à 6 chiffres';
                return null;
              },
            ),
            const SizedBox(height: 16),

            _buildLabel('NUMÉRO CNI (optionnel)'),
            _buildField(controller: _cniCtrl, hint: 'Ex : 123456789', icon: Icons.credit_card, required: false),
            const SizedBox(height: 28),

            if (erreur != null) ...[
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.erreur.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: AppColors.erreur.withValues(alpha: 0.4)),
                ),
                child: Row(children: [
                  const Icon(Icons.error_outline, color: AppColors.erreur, size: 18),
                  const SizedBox(width: 8),
                  Expanded(child: Text(erreur, style: const TextStyle(color: AppColors.erreur, fontSize: 12))),
                ]),
              ),
              const SizedBox(height: 16),
            ],

            SizedBox(
              width: double.infinity,
              height: 52,
              child: ElevatedButton(
                onPressed: chargement ? null : _enroler,
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.accent,
                  disabledBackgroundColor: AppColors.accent.withValues(alpha: 0.5),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                child: chargement
                    ? const SizedBox(height: 22, width: 22,
                        child: CircularProgressIndicator(color: AppColors.texteBouton, strokeWidth: 2.5))
                    : Text('Enrôler le chauffeur',
                        style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: AppColors.texteBouton)),
              ),
            ),
          ],
        ),
      ),
    );

    if (!widget.asSheet) {
      return Scaffold(
        backgroundColor: AppColors.fond,
        appBar: AppBar(
          leading: IconButton(
            icon: const Icon(Icons.arrow_back, color: AppColors.texte),
            onPressed: () => Navigator.pop(context),
          ),
          title: const Text('Enrôler un chauffeur'),
        ),
        body: contenu,
      );
    }

    return Container(
      decoration: const BoxDecoration(
        color: AppColors.texteBouton,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: contenu,
    );
  }

  Widget _buildLabel(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(text,
          style: const TextStyle(fontSize: 11, letterSpacing: 1.1,
              color: AppColors.texteMuet, fontWeight: FontWeight.w600)),
    );
  }

  Widget _buildField({
    required TextEditingController controller,
    required String hint,
    required IconData icon,
    TextInputType keyboardType = TextInputType.text,
    bool obscure = false,
    int? maxLength,
    bool required = true,
    String? Function(String?)? validator,
  }) {
    return TextFormField(
      controller: controller,
      keyboardType: keyboardType,
      obscureText: obscure,
      maxLength: maxLength,
      style: const TextStyle(color: AppColors.texte, fontSize: 14),
      decoration: InputDecoration(
        hintText: hint,
        hintStyle: const TextStyle(color: Color(0xFF6B6357)),
        counterText: '',
        filled: true,
        fillColor: AppColors.surface,
        prefixIcon: Icon(icon, color: AppColors.texteMuet, size: 20),
        border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(10),
            borderSide: const BorderSide(color: AppColors.bordure)),
        enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(10),
            borderSide: const BorderSide(color: AppColors.bordure)),
        focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(10),
            borderSide: const BorderSide(color: AppColors.accent)),
        errorBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(10),
            borderSide: const BorderSide(color: AppColors.erreur)),
      ),
      validator: required ? (validator ?? (v) => v!.isEmpty ? 'Champ obligatoire' : null) : null,
    );
  }
}
