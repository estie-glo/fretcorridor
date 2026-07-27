import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/axe_provider.dart';
import '../providers/connectivity_provider.dart';
import '../providers/declaration_vide_provider.dart';
import '../models/axe_model.dart';
import '../models/declaration_vide_model.dart';
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
  bool _disponibleMaintenant = true;
  DateTime? _disponibleDeChoisi; // EF-MKT-01

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
      disponibleDe: _disponibleMaintenant ? null : _disponibleDeChoisi,
    );

    if (succes) {
      _capaciteCtrl.clear();
      setState(() {
        _axeSelectionne = null;
        _disponibleMaintenant = true;
        _disponibleDeChoisi = null;
      });
    }
  }

  Future<void> _choisirDateDisponibilite() async {
    final date = await showDatePicker(
      context: context,
      initialDate: _disponibleDeChoisi ?? DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 30)),
    );
    if (date == null || !mounted) return;

    final heure = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(_disponibleDeChoisi ?? DateTime.now()),
    );
    if (heure == null) return;

    setState(() {
      _disponibleDeChoisi = DateTime(date.year, date.month, date.day, heure.hour, heure.minute);
    });
  }

  void _ouvrirDetail(DeclarationVideModel d) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (_) => _DetailDeclarationSheet(declaration: d),
    );
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
                const SizedBox(height: 16),

                // ── Disponibilité (EF-MKT-01) ─────────────────
                const Text('DISPONIBILITÉ', style: TextStyle(fontSize: 11, letterSpacing: 1.1,
                    color: AppColors.texteMuet, fontWeight: FontWeight.w600)),
                const SizedBox(height: 8),
                Container(
                  decoration: BoxDecoration(
                    color: AppColors.surface,
                    borderRadius: BorderRadius.circular(10),
                    border: Border.all(color: AppColors.bordure),
                  ),
                  child: Column(
                    children: [
                      SwitchListTile(
                        value: _disponibleMaintenant,
                        onChanged: (val) => setState(() {
                          _disponibleMaintenant = val;
                          if (val) _disponibleDeChoisi = null;
                        }),
                        activeThumbColor: AppColors.accent,
                        title: const Text('Disponible maintenant', style: TextStyle(fontSize: 14)),
                        contentPadding: const EdgeInsets.symmetric(horizontal: 14),
                      ),
                      if (!_disponibleMaintenant)
                        Padding(
                          padding: const EdgeInsets.fromLTRB(14, 0, 14, 14),
                          child: OutlinedButton.icon(
                            onPressed: _choisirDateDisponibilite,
                            icon: const Icon(Icons.event, size: 18),
                            label: Text(
                              _disponibleDeChoisi == null
                                  ? 'Choisir une date et une heure'
                                  : 'Disponible le ${_disponibleDeChoisi!.day.toString().padLeft(2, '0')}/'
                                    '${_disponibleDeChoisi!.month.toString().padLeft(2, '0')} '
                                    'à ${_disponibleDeChoisi!.hour.toString().padLeft(2, '0')}:'
                                    '${_disponibleDeChoisi!.minute.toString().padLeft(2, '0')}',
                            ),
                          ),
                        ),
                    ],
                  ),
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
          const SizedBox(height: 4),
          const Text('Appuyez sur une déclaration pour la modifier ou la supprimer',
              style: TextStyle(color: AppColors.texteMuet, fontSize: 11)),
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
            ...declarationState.declarations.map((d) => InkWell(
                  onTap: () => _ouvrirDetail(d),
                  borderRadius: BorderRadius.circular(10),
                  child: Container(
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
                        const SizedBox(width: 6),
                        const Icon(Icons.chevron_right, size: 18, color: AppColors.texteMuet),
                      ],
                    ),
                  ),
                )),
        ],
      ),
    );
  }
}

// ── Bottom sheet détail / modifier / supprimer ──────────────────
class _DetailDeclarationSheet extends ConsumerStatefulWidget {
  final DeclarationVideModel declaration;
  const _DetailDeclarationSheet({required this.declaration});

  @override
  ConsumerState<_DetailDeclarationSheet> createState() => _DetailDeclarationSheetState();
}

class _DetailDeclarationSheetState extends ConsumerState<_DetailDeclarationSheet> {
  late TextEditingController _capaciteCtrl;
  late String _typeCamionSelectionne;
  DateTime? _disponibleDeChoisi;
  bool _modeEdition = false;

  final List<String> _typesCamion = ['Semi-remorque', 'Porteur', 'Citerne', 'Plateau'];

  @override
  void initState() {
    super.initState();
    _capaciteCtrl = TextEditingController(text: widget.declaration.capaciteTonnes.toString());
    _typeCamionSelectionne = _typesCamion.contains(widget.declaration.typeCamion)
        ? widget.declaration.typeCamion
        : _typesCamion.first;
    _disponibleDeChoisi = widget.declaration.disponibleDe;
  }

  Future<void> _choisirDateDisponibilite() async {
    final date = await showDatePicker(
      context: context,
      initialDate: _disponibleDeChoisi ?? DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 30)),
    );
    if (date == null || !mounted) return;

    final heure = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(_disponibleDeChoisi ?? DateTime.now()),
    );
    if (heure == null) return;

    setState(() {
      _disponibleDeChoisi = DateTime(date.year, date.month, date.day, heure.hour, heure.minute);
    });
  }

  @override
  void dispose() {
    _capaciteCtrl.dispose();
    super.dispose();
  }

  Future<void> _enregistrerModification() async {
    final nouvelleCapacite = double.tryParse(_capaciteCtrl.text.trim());
    if (nouvelleCapacite == null) return;

    final succes = await ref.read(declarationVideProvider.notifier).modifier(
      idLocal: widget.declaration.idLocal,
      missionId: widget.declaration.missionId,
      typeCamion: _typeCamionSelectionne,
      capaciteTonnes: nouvelleCapacite,
      disponibleDe: _disponibleDeChoisi,
    );

    if (succes && mounted) Navigator.pop(context);
  }

  Future<void> _confirmerSuppression() async {
    final confirme = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Supprimer cette déclaration ?'),
        content: const Text('Cette action est irréversible.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Annuler')),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('Supprimer', style: TextStyle(color: AppColors.erreur)),
          ),
        ],
      ),
    );

    if (confirme == true) {
      final succes = await ref.read(declarationVideProvider.notifier).supprimer(
        idLocal: widget.declaration.idLocal,
        missionId: widget.declaration.missionId,
      );
      if (succes && mounted) Navigator.pop(context);
    }
  }

  @override
  Widget build(BuildContext context) {
    final d = widget.declaration;
    return Container(
      padding: EdgeInsets.only(
        left: 20, right: 20, top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
      ),
      decoration: const BoxDecoration(
        color: AppColors.fond,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Center(
            child: Container(
              width: 40, height: 4,
              decoration: BoxDecoration(color: AppColors.bordure, borderRadius: BorderRadius.circular(2)),
            ),
          ),
          const SizedBox(height: 16),
          Text(d.axeNom, style: Theme.of(context).textTheme.headlineMedium),
          const SizedBox(height: 4),
          Text(
            d.synchronise ? 'Synchronisé ✅' : 'En attente de synchronisation 🔄',
            style: TextStyle(color: d.synchronise ? AppColors.succes : AppColors.marqueOrange, fontSize: 13),
          ),
          const SizedBox(height: 20),

          if (_modeEdition) ...[
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
              items: _typesCamion
                  .map((t) => DropdownMenuItem<String>(value: t, child: Text(t)))
                  .toList(),
              onChanged: (val) => setState(() => _typeCamionSelectionne = val!),
            ),
            const SizedBox(height: 16),

            const Text('CAPACITÉ (TONNES)', style: TextStyle(fontSize: 11, letterSpacing: 1.1,
                color: AppColors.texteMuet, fontWeight: FontWeight.w600)),
            const SizedBox(height: 8),
            TextField(
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
            ),
            const SizedBox(height: 16),

            const Text('DISPONIBILITÉ', style: TextStyle(fontSize: 11, letterSpacing: 1.1,
                color: AppColors.texteMuet, fontWeight: FontWeight.w600)),
            const SizedBox(height: 8),
            OutlinedButton.icon(
              onPressed: _choisirDateDisponibilite,
              icon: const Icon(Icons.event, size: 18),
              label: Text(
                _disponibleDeChoisi == null
                    ? 'Disponible maintenant'
                    : 'Disponible le ${_disponibleDeChoisi!.day.toString().padLeft(2, '0')}/'
                      '${_disponibleDeChoisi!.month.toString().padLeft(2, '0')} '
                      'à ${_disponibleDeChoisi!.hour.toString().padLeft(2, '0')}:'
                      '${_disponibleDeChoisi!.minute.toString().padLeft(2, '0')}',
              ),
            ),
            if (_disponibleDeChoisi != null)
              Align(
                alignment: Alignment.centerLeft,
                child: TextButton(
                  onPressed: () => setState(() => _disponibleDeChoisi = null),
                  child: const Text('Revenir à "disponible maintenant"', style: TextStyle(fontSize: 12)),
                ),
              ),
            const SizedBox(height: 24),

            Row(
              children: [
                Expanded(
                  child: SizedBox(
                    height: 52,
                    child: OutlinedButton(
                      onPressed: () => setState(() => _modeEdition = false),
                      style: OutlinedButton.styleFrom(
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                      child: const Text('Annuler'),
                    ),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: SizedBox(
                    height: 52,
                    child: ElevatedButton(
                      onPressed: _enregistrerModification,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.accent,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                      child: const Text('Enregistrer',
                          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white)),
                    ),
                  ),
                ),
              ],
            ),
          ] else ...[
            Text('Type de camion : ${d.typeCamion}', style: const TextStyle(fontSize: 14)),
            const SizedBox(height: 8),
            Text('Capacité : ${d.capaciteTonnes} T', style: const TextStyle(fontSize: 14)),
            const SizedBox(height: 28),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: d.missionId == null ? null : () => setState(() => _modeEdition = true),
                    icon: const Icon(Icons.edit, size: 18),
                    label: const Text('Modifier'),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _confirmerSuppression,
                    style: OutlinedButton.styleFrom(
                        foregroundColor: AppColors.erreur,
                        side: const BorderSide(color: AppColors.erreur)),
                    icon: const Icon(Icons.delete_outline, size: 18),
                    label: const Text('Supprimer'),
                  ),
                ),
              ],
            ),
            if (d.missionId == null) ...[
              const SizedBox(height: 10),
              const Text('La modification sera possible une fois la déclaration synchronisée.',
                  style: TextStyle(color: AppColors.texteMuet, fontSize: 11)),
            ],
          ],
        ],
      ),
    );
  }
}
