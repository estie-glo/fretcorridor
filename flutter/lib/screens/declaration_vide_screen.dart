import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/axe_provider.dart';
import '../providers/connectivity_provider.dart';
import '../providers/declaration_vide_provider.dart';
import '../models/axe_model.dart';
import '../theme/app_theme.dart';

class DeclarationVideScreen extends ConsumerStatefulWidget {
  const DeclarationVideScreen({super.key});

  @override
  ConsumerState<DeclarationVideScreen> createState() => _DeclarationVideScreenState();
}

class _DeclarationVideScreenState extends ConsumerState<DeclarationVideScreen> {
  final _formKey = GlobalKey<FormState>();
  final _capaciteCtrl = TextEditingController();
  AxeModel? _axeSelectionne;
  String _typeCamionSelectionne = 'Semi-remorque';

  final List<String> _typesCamion = ['Semi-remorque', 'Porteur', 'Citerne', 'Plateau'];

  @override
  void initState() {
    super.initState();
    // Tente une synchro dès l'ouverture si on est en ligne
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(declarationVideProvider.notifier).synchroniserEnAttente();
    });
  }

  @override
  void dispose() {
    _capaciteCtrl.dispose();
    super.dispose();
  }

  Future<void> _declarer() async {
    if (!_formKey.currentState!.validate()) return;
    if (_axeSelectionne == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Sélectionnez un axe'), backgroundColor: AppColors.erreur),
      );
      return;
    }

    final succes = await ref.read(declarationVideProvider.notifier).declarer(
      axeId: _axeSelectionne!.id,
      axeNom: _axeSelectionne!.nom,
      typeCamion: _typeCamionSelectionne,
      capaciteTonnes: double.parse(_capaciteCtrl.text.trim()),
    );

    if (succes) {
      _capaciteCtrl.clear();
      setState(() => _axeSelectionne = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    final axeState = ref.watch(axeProvider);
    final declarationState = ref.watch(declarationVideProvider);
    final statutConnexion = ref.watch(connectivityProvider);

    ref.listen(connectivityProvider, (previous, next) {
      if (previous == StatutConnexion.horsLigne && next == StatutConnexion.enLigne) {
        ref.read(declarationVideProvider.notifier).synchroniserEnAttente();
      }
    });

    ref.listen(declarationVideProvider, (previous, next) {
      if (next.succes != null && next.succes != previous?.succes) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(next.succes!), backgroundColor: AppColors.succes),
        );
      }
      if (next.erreur != null && next.erreur != previous?.erreur) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(next.erreur!), backgroundColor: AppColors.erreur),
        );
      }
    });

    return Scaffold(
      backgroundColor: AppColors.fond,
      appBar: AppBar(title: const Text('Déclarer camion vide')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [

          // ── Indicateur de connexion ──────────────────────
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: BoxDecoration(
              color: statutConnexion == StatutConnexion.enLigne
                  ? AppColors.succes.withValues(alpha: 0.1)
                  : AppColors.erreur.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Row(
              children: [
                Icon(
                  statutConnexion == StatutConnexion.enLigne ? Icons.wifi : Icons.wifi_off,
                  size: 16,
                  color: statutConnexion == StatutConnexion.enLigne ? AppColors.succes : AppColors.erreur,
                ),
                const SizedBox(width: 8),
                Text(
                  statutConnexion == StatutConnexion.enLigne ? 'En ligne' : 'Hors ligne',
                  style: TextStyle(
                    color: statutConnexion == StatutConnexion.enLigne ? AppColors.succes : AppColors.erreur,
                    fontSize: 12, fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('AXE', style: TextStyle(fontSize: 11, letterSpacing: 1.1,
                    color: AppColors.texteMuet, fontWeight: FontWeight.w600)),
                const SizedBox(height: 8),
                axeState.chargement
                    ? const Center(child: CircularProgressIndicator(color: AppColors.accent))
                    : DropdownButtonFormField<AxeModel>(
                        initialValue: _axeSelectionne,
                        decoration: InputDecoration(
                          filled: true,
                          fillColor: AppColors.surface,
                          border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(10),
                              borderSide: const BorderSide(color: AppColors.bordure)),
                        ),
                        hint: const Text('Choisir un axe'),
                        items: axeState.axes
                            .where((a) => !a.inactif)
                            .map((a) => DropdownMenuItem<AxeModel>(
                                  value: a,
                                  child: Text('${a.nom}${a.verrouille ? " (verrouillé)" : ""}'),
                                ))
                            .toList(),
                        onChanged: (val) => setState(() => _axeSelectionne = val),
                      ),
                const SizedBox(height: 16),

                const Text('TYPE DE CAMION', style: TextStyle(fontSize: 11, letterSpacing: 1.1,
                    color: AppColors.texteMuet, fontWeight: FontWeight.w600)),
                const SizedBox(height: 8),
                DropdownButtonFormField<String>(
                  initialValue: _typeCamionSelectionne,
                  decoration: InputDecoration(
                    filled: true,
                    fillColor: AppColors.surface,
                    border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(10),
                        borderSide: const BorderSide(color: AppColors.bordure)),
                  ),
                  items: _typesCamion.map((t) => DropdownMenuItem<String>(value: t, child: Text(t))).toList(),
                  onChanged: (val) => setState(() => _typeCamionSelectionne = val!),
                ),
                const SizedBox(height: 16),

                const Text('CAPACITÉ (TONNES)', style: TextStyle(fontSize: 11, letterSpacing: 1.1,
                    color: AppColors.texteMuet, fontWeight: FontWeight.w600)),
                const SizedBox(height: 8),
                TextFormField(
                  controller: _capaciteCtrl,
                  keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  decoration: InputDecoration(
                    hintText: 'Ex : 25',
                    filled: true,
                    fillColor: AppColors.surface,
                    border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(10),
                        borderSide: const BorderSide(color: AppColors.bordure)),
                  ),
                  validator: (v) {
                    if (v == null || v.isEmpty) return 'Capacité obligatoire';
                    if (double.tryParse(v) == null) return 'Nombre invalide';
                    return null;
                  },
                ),
                const SizedBox(height: 28),

                SizedBox(
                  width: double.infinity,
                  height: 52,
                  child: ElevatedButton(
                    onPressed: declarationState.positionEnCours ? null : _declarer,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.accent,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                    child: declarationState.positionEnCours
                        ? const Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              SizedBox(height: 18, width: 18,
                                  child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2.5)),
                              SizedBox(width: 10),
                              Text('Localisation en cours...', style: TextStyle(color: Colors.white)),
                            ],
                          )
                        : const Text('Déclarer camion vide',
                            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white)),
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 32),
          Text('Mes déclarations', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 10),

          if (declarationState.declarations.isEmpty)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 20),
              child: Center(
                child: Text('Aucune déclaration pour le moment',
                    style: TextStyle(color: AppColors.texteMuet)),
              ),
            )
          else
            ...declarationState.declarations.map((d) => Container(
                  margin: const EdgeInsets.only(bottom: 8),
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppColors.surface,
                    borderRadius: BorderRadius.circular(10),
                    border: Border.all(color: AppColors.bordure),
                  ),
                  child: Row(
                    children: [
                      Icon(
                        d.synchronise ? Icons.check_circle : Icons.schedule,
                        color: d.synchronise ? AppColors.succes : AppColors.marqueOrange,
                        size: 18,
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(d.axeNom, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                            Text('${d.typeCamion} · ${d.capaciteTonnes}T',
                                style: const TextStyle(color: AppColors.texteMuet, fontSize: 12)),
                          ],
                        ),
                      ),
                      Text(
                        d.synchronise ? 'Synchronisé' : 'En attente',
                        style: TextStyle(
                          fontSize: 11,
                          color: d.synchronise ? AppColors.succes : AppColors.marqueOrange,
                        ),
                      ),
                    ],
                  ),
                )),
        ],
      ),
    );
  }
}
