import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/auth_provider.dart';
import '../providers/chauffeur_provider.dart';
import '../models/chauffeur_model.dart';
import '../theme/app_theme.dart';
import 'enrolement_screen.dart';

class DashboardAgentScreen extends ConsumerWidget {
  const DashboardAgentScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState      = ref.watch(authProvider);
    final chauffeurState = ref.watch(chauffeurProvider);

    return Scaffold(
      backgroundColor: AppColors.fond,
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Dashboard Agent', style: Theme.of(context).textTheme.headlineMedium),
            Text(
              authState.utilisateur?.configTenant.nomBureau ?? '',
              style: const TextStyle(color: AppColors.accent, fontSize: 11),
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: AppColors.texteMuet),
            onPressed: () => ref.read(chauffeurProvider.notifier).chargerTout(),
          ),
          IconButton(
            icon: const Icon(Icons.logout, color: AppColors.texteMuet),
            onPressed: () => ref.read(authProvider.notifier).logout(),
          ),
        ],
      ),

      floatingActionButton: FloatingActionButton.extended(
        onPressed: () async {
          await showModalBottomSheet(
            context: context,
            backgroundColor: Colors.transparent,
            isScrollControlled: true,
            builder: (_) => const EnrolementScreen(asSheet: true),
          );
          ref.read(chauffeurProvider.notifier).chargerTout();
        },
        backgroundColor: AppColors.accent,
        icon: const Icon(Icons.person_add, color: AppColors.texteBouton),
        label: Text('Enrôler',
            style: TextStyle(color: AppColors.texteBouton, fontWeight: FontWeight.bold)),
      ),

      body: chauffeurState.chargement
          ? const Center(child: CircularProgressIndicator(color: AppColors.accent))
          : chauffeurState.erreur != null
              ? _ErreurView(
                  message: chauffeurState.erreur!,
                  onRetry: () => ref.read(chauffeurProvider.notifier).chargerTout(),
                )
              : RefreshIndicator(
                  color: AppColors.accent,
                  backgroundColor: AppColors.surface,
                  onRefresh: () => ref.read(chauffeurProvider.notifier).chargerTout(),
                  child: ListView(
                    padding: const EdgeInsets.all(16),
                    children: [

                      // ── Stats asymétriques : 1 grande + 2 petites empilées ──
                      SizedBox(
                        height: 130,
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            Expanded(
                              flex: 3,
                              child: _StatHero(
                                titre: 'Chauffeurs actifs',
                                valeur: '${chauffeurState.chauffeurs.length}',
                                icon: Icons.local_shipping_outlined,
                              ),
                            ),
                            const SizedBox(width: 10),
                            Expanded(
                              flex: 2,
                              child: Column(
                                children: [
                                  Expanded(
                                    child: _StatMini(
                                      titre: 'KYC en attente',
                                      valeur: '${chauffeurState.kycEnAttente.length}',
                                      couleur: AppColors.accent,
                                    ),
                                  ),
                                  const SizedBox(height: 10),
                                  Expanded(
                                    child: _StatMini(
                                      titre: 'KYC validés',
                                      valeur: '${chauffeurState.chauffeurs.where((c) => c.kycValide).length}',
                                      couleur: AppColors.succes,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 24),

                      if (chauffeurState.succes != null)
                        Container(
                          margin: const EdgeInsets.only(bottom: 16),
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: AppColors.succes.withValues(alpha: 0.12),
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(color: AppColors.succes.withValues(alpha: 0.4)),
                          ),
                          child: Row(children: [
                            const Icon(Icons.check_circle, color: AppColors.succes, size: 18),
                            const SizedBox(width: 8),
                            Text(chauffeurState.succes!,
                                style: const TextStyle(color: AppColors.succes, fontSize: 13)),
                          ]),
                        ),

                      if (chauffeurState.kycEnAttente.isNotEmpty) ...[
                        Text('KYC en attente', style: Theme.of(context).textTheme.titleMedium),
                        const SizedBox(height: 10),
                        ...chauffeurState.kycEnAttente.map((c) => _KycCard(
                              chauffeur: c,
                              onValider: () => _confirmerValidation(context, ref, c.id, true),
                              onRejeter: () => _confirmerValidation(context, ref, c.id, false),
                            )),
                        const SizedBox(height: 20),
                      ],

                      Text('Mes chauffeurs', style: Theme.of(context).textTheme.titleMedium),
                      const SizedBox(height: 10),

                      if (chauffeurState.chauffeurs.isEmpty)
                        const _VideView(message: 'Aucun chauffeur enrôlé.\nAppuyez sur + pour en ajouter un.')
                      else
                        ...chauffeurState.chauffeurs.map((c) => _ChauffeurItem(chauffeur: c)),
                    ],
                  ),
                ),
    );
  }

  void _confirmerValidation(BuildContext context, WidgetRef ref, String chauffeurId, bool approuve) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.surface,
        title: Text(approuve ? 'Valider le KYC ?' : 'Rejeter le KYC ?',
            style: const TextStyle(color: AppColors.texte)),
        content: Text(
          approuve
              ? 'Le chauffeur pourra accéder à toutes les fonctionnalités.'
              : 'Le dossier sera marqué comme rejeté.',
          style: const TextStyle(color: AppColors.texteMuet),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Annuler', style: TextStyle(color: AppColors.texteMuet)),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(ctx);
              ref.read(chauffeurProvider.notifier).validerKyc(chauffeurId: chauffeurId, approuve: approuve);
            },
            style: ElevatedButton.styleFrom(
                backgroundColor: approuve ? AppColors.succes : AppColors.erreur),
            child: Text(approuve ? 'Valider' : 'Rejeter', style: const TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }
}

// ── Carte stat principale (grande) ──────────────────────────────
class _StatHero extends StatelessWidget {
  final String titre;
  final String valeur;
  final IconData icon;
  const _StatHero({required this.titre, required this.valeur, required this.icon});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.accentProfond.withValues(alpha: 0.18),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.accentProfond.withValues(alpha: 0.4)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Icon(icon, color: AppColors.accent, size: 26),
          Text(valeur,
              style: GoogleFonts.fraunces(fontSize: 34, fontWeight: FontWeight.w600, color: AppColors.texte)),
          Text(titre, style: const TextStyle(color: AppColors.texteMuet, fontSize: 12)),
        ],
      ),
    );
  }
}

class _StatMini extends StatelessWidget {
  final String titre;
  final String valeur;
  final Color couleur;
  const _StatMini({required this.titre, required this.valeur, required this.couleur});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.bordure),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(titre, style: const TextStyle(color: AppColors.texteMuet, fontSize: 11)),
          Text(valeur, style: TextStyle(color: couleur, fontSize: 20, fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }
}


class _KycCard extends StatelessWidget {
  final ChauffeurModel chauffeur;
  final VoidCallback onValider;
  final VoidCallback onRejeter;
  const _KycCard({required this.chauffeur, required this.onValider, required this.onRejeter});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.accent.withValues(alpha: 0.3)),
      ),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(children: [
          CircleAvatar(
            backgroundColor: AppColors.surfaceClaire,
            child: Text(chauffeur.prenom.isNotEmpty ? chauffeur.prenom[0] : '?',
                style: const TextStyle(color: AppColors.accent, fontWeight: FontWeight.bold)),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(chauffeur.nomComplet,
                  style: const TextStyle(color: AppColors.texte, fontWeight: FontWeight.bold)),
              Text(chauffeur.telephone,
                  style: const TextStyle(color: AppColors.texteMuet, fontSize: 12)),
            ]),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
            decoration: BoxDecoration(
              color: AppColors.accent.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(20),
            ),
            child: const Text('En attente',
                style: TextStyle(color: AppColors.accent, fontSize: 10, fontWeight: FontWeight.bold)),
          ),
        ]),
        const SizedBox(height: 12),
        Row(children: [
          Expanded(
            child: OutlinedButton(
              onPressed: onRejeter,
              style: OutlinedButton.styleFrom(
                foregroundColor: AppColors.erreur,
                side: const BorderSide(color: AppColors.erreur),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
              ),
              child: const Text('Rejeter'),
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: ElevatedButton(
              onPressed: onValider,
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.succes,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
              ),
              child: const Text('Valider', style: TextStyle(color: Colors.white)),
            ),
          ),
        ]),
      ]),
    );
  }
}

class _ChauffeurItem extends StatelessWidget {
  final ChauffeurModel chauffeur;
  const _ChauffeurItem({required this.chauffeur});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.bordure),
      ),
      child: Row(children: [
        CircleAvatar(
          backgroundColor: AppColors.surfaceClaire,
          child: Text(chauffeur.prenom.isNotEmpty ? chauffeur.prenom[0] : '?',
              style: const TextStyle(color: AppColors.accent, fontWeight: FontWeight.bold)),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(chauffeur.nomComplet,
                style: const TextStyle(color: AppColors.texte, fontWeight: FontWeight.bold, fontSize: 13)),
            Text(chauffeur.telephone, style: const TextStyle(color: AppColors.texteMuet, fontSize: 11)),
            Text(chauffeur.badgeKyc,
                style: TextStyle(
                    color: chauffeur.kycValide ? AppColors.succes : AppColors.texteMuet, fontSize: 11)),
          ]),
        ),
        const Icon(Icons.chevron_right, color: AppColors.texteMuet),
      ]),
    );
  }
}

class _VideView extends StatelessWidget {
  final String message;
  const _VideView({required this.message});
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 40),
        child: Column(children: [
          const Icon(Icons.people_outline, color: AppColors.bordure, size: 48),
          const SizedBox(height: 12),
          Text(message,
              textAlign: TextAlign.center, style: const TextStyle(color: AppColors.texteMuet, fontSize: 14)),
        ]),
      ),
    );
  }
}

class _ErreurView extends StatelessWidget {
  final String message;
  final VoidCallback onRetry;
  const _ErreurView({required this.message, required this.onRetry});
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
        const Icon(Icons.wifi_off, color: AppColors.erreur, size: 48),
        const SizedBox(height: 12),
        Text(message, textAlign: TextAlign.center, style: const TextStyle(color: AppColors.texteMuet, fontSize: 14)),
        const SizedBox(height: 20),
        ElevatedButton(
          onPressed: onRetry,
          style: ElevatedButton.styleFrom(backgroundColor: AppColors.accent),
          child: const Text('Réessayer', style: TextStyle(color: AppColors.texteBouton)),
        ),
      ]),
    );
  }
}
