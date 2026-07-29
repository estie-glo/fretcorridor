import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/auth_provider.dart';
import '../providers/connectivity_provider.dart';
import '../providers/notification_provider.dart';
import '../providers/tracking_provider.dart';
import '../theme/app_theme.dart';

class DashboardChauffeurScreen extends ConsumerStatefulWidget {
  const DashboardChauffeurScreen({super.key});

  @override
  ConsumerState<DashboardChauffeurScreen> createState() =>
      _DashboardChauffeurScreenState();
}

class _DashboardChauffeurScreenState
    extends ConsumerState<DashboardChauffeurScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() {
      ref.read(notificationProvider.notifier).charger();
      ref.read(trackingProvider.notifier).rafraichirEtDemarrer();
    });
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authProvider);
    final notifState = ref.watch(notificationProvider);
    final trackingState = ref.watch(trackingProvider);
    final connexion = ref.watch(connectivityProvider);

    ref.listen(connectivityProvider, (prev, next) {
      if (prev != StatutConnexion.enLigne && next == StatutConnexion.enLigne) {
        ref.read(trackingProvider.notifier).synchroniserEnAttente();
      }
    });

    return Scaffold(
      backgroundColor: AppColors.fond,
      appBar: AppBar(
        title: const Text('Espace chauffeur'),
        actions: [
          Stack(
            children: [
              IconButton(
                icon: const Icon(Icons.notifications_outlined),
                onPressed: () => Navigator.pushNamed(context, '/notifications'),
              ),
              if (notifState.nonLues > 0)
                Positioned(
                  right: 8,
                  top: 8,
                  child: Container(
                    padding: const EdgeInsets.all(4),
                    decoration: const BoxDecoration(
                      color: AppColors.marqueOrange,
                      shape: BoxShape.circle,
                    ),
                    constraints:
                        const BoxConstraints(minWidth: 16, minHeight: 16),
                    child: Text(
                      '${notifState.nonLues}',
                      style: const TextStyle(color: Colors.white, fontSize: 10),
                      textAlign: TextAlign.center,
                    ),
                  ),
                ),
            ],
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () => ref.read(authProvider.notifier).logout(),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text(
            authState.utilisateur?.configTenant.nomBureau ?? 'FretCorridor',
            style: Theme.of(context).textTheme.headlineMedium,
          ),
          const SizedBox(height: 4),
          Text(
            'Tenant : ${authState.utilisateur?.tenantId ?? ''}',
            style: Theme.of(context).textTheme.bodySmall,
          ),
          if (connexion == StatutConnexion.horsLigne) ...[
            const SizedBox(height: 12),
            const _BandeauOffline(),
          ],
          const SizedBox(height: 24),
          _ActionCard(
            icon: Icons.route,
            titre: 'Axes corridor',
            sousTitre: 'Consulter le réseau et les états d\'activation',
            onTap: () => Navigator.pushNamed(context, '/axes'),
          ),
          _ActionCard(
            icon: Icons.local_shipping_outlined,
            titre: 'Déclarer camion vide',
            sousTitre: 'Offline-first avec synchronisation automatique',
            onTap: () => Navigator.pushNamed(context, '/declaration-vide'),
          ),
          _ActionCard(
            icon: Icons.person_outline,
            titre: 'Mon profil & KYC',
            sousTitre: 'Documents CNI / permis via MinIO',
            onTap: () => Navigator.pushNamed(context, '/profil-chauffeur'),
          ),
          _ActionCard(
            icon: Icons.handshake_outlined,
            titre: 'Matchs disponibles',
            sousTitre: 'Offres de fret (stub S9)',
            onTap: () => Navigator.pushNamed(context, '/matchs'),
          ),
          const SizedBox(height: 8),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Tracking GPS (S5)',
                      style: Theme.of(context).textTheme.titleMedium),
                  const SizedBox(height: 8),
                  Text(
                    trackingState.missionIdActive != null
                        ? 'Mission active : ${trackingState.missionIdActive!.substring(0, 8)}…'
                        : 'Déclarez un camion vide pour activer le suivi.',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                  if (trackingState.missionIdActive != null) ...[
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        Icon(
                          trackingState.suiviAutoActif
                              ? Icons.gps_fixed
                              : Icons.gps_off,
                          size: 16,
                          color: trackingState.suiviAutoActif
                              ? AppColors.succes
                              : AppColors.texteMuet,
                        ),
                        const SizedBox(width: 6),
                        Expanded(
                          child: Text(
                            trackingState.suiviAutoActif
                                ? 'Suivi automatique actif — votre position est transmise pendant la mission'
                                : 'Suivi automatique désactivé',
                            style: TextStyle(
                              fontSize: 12,
                              color: trackingState.suiviAutoActif
                                  ? AppColors.succes
                                  : AppColors.texteMuet,
                            ),
                          ),
                        ),
                        Switch(
                          value: trackingState.suiviAutoActif,
                          onChanged: (actif) => actif
                              ? ref.read(trackingProvider.notifier).demarrerSuiviAuto()
                              : ref.read(trackingProvider.notifier).arreterSuiviAuto(),
                        ),
                      ],
                    ),
                  ],
                  if (trackingState.positionsEnAttente > 0) ...[
                    const SizedBox(height: 4),
                    Text(
                      '${trackingState.positionsEnAttente} position(s) en attente de sync',
                      style: const TextStyle(
                          color: AppColors.marqueOrange, fontSize: 12),
                    ),
                  ],
                  if (trackingState.succes != null) ...[
                    const SizedBox(height: 8),
                    Text(trackingState.succes!,
                        style: const TextStyle(
                            color: AppColors.succes, fontSize: 12)),
                  ],
                  if (trackingState.erreur != null) ...[
                    const SizedBox(height: 8),
                    Text(trackingState.erreur!,
                        style: const TextStyle(
                            color: AppColors.erreur, fontSize: 12)),
                  ],
                  const SizedBox(height: 12),
                  ElevatedButton.icon(
                    onPressed: trackingState.envoiEnCours
                        ? null
                        : () => ref
                            .read(trackingProvider.notifier)
                            .capturerEtEnvoyerPosition(),
                    icon: trackingState.envoiEnCours
                        ? const SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.my_location),
                    label: const Text('Forcer un envoi maintenant'),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _BandeauOffline extends StatelessWidget {
  const _BandeauOffline();

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.marqueOrange.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(8),
      ),
      child: const Row(children: [
        Icon(Icons.wifi_off, color: AppColors.marqueOrange, size: 18),
        SizedBox(width: 8),
        Expanded(
          child: Text(
            'Mode hors ligne — vos actions seront synchronisées au retour réseau.',
            style: TextStyle(fontSize: 12),
          ),
        ),
      ]),
    );
  }
}

class _ActionCard extends StatelessWidget {
  final IconData icon;
  final String titre;
  final String sousTitre;
  final VoidCallback onTap;

  const _ActionCard({
    required this.icon,
    required this.titre,
    required this.sousTitre,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: Icon(icon, color: AppColors.accent),
        title: Text(titre),
        subtitle: Text(sousTitre),
        trailing: const Icon(Icons.chevron_right),
        onTap: onTap,
      ),
    );
  }
}
