import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/axe_provider.dart';
import '../models/axe_model.dart';
import '../theme/app_theme.dart';

class AxesScreen extends ConsumerWidget {
  const AxesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final axeState = ref.watch(axeProvider);

    return Scaffold(
      backgroundColor: AppColors.fond,
      appBar: AppBar(
        title: const Text('Axes disponibles'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: AppColors.texteMuet),
            onPressed: () => ref.read(axeProvider.notifier).chargerAxes(),
          ),
        ],
      ),
      body: axeState.chargement
          ? const Center(child: CircularProgressIndicator(color: AppColors.accent))
          : axeState.erreur != null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.wifi_off, color: AppColors.erreur, size: 48),
                      const SizedBox(height: 12),
                      Text(axeState.erreur!,
                          style: const TextStyle(color: AppColors.texteMuet)),
                      const SizedBox(height: 20),
                      ElevatedButton(
                        onPressed: () => ref.read(axeProvider.notifier).chargerAxes(),
                        style: ElevatedButton.styleFrom(backgroundColor: AppColors.accent),
                        child: const Text('Réessayer', style: TextStyle(color: Colors.white)),
                      ),
                    ],
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: axeState.axes.length,
                  itemBuilder: (context, index) => _AxeCard(axe: axeState.axes[index]),
                ),
    );
  }
}

class _AxeCard extends StatelessWidget {
  final AxeModel axe;
  const _AxeCard({required this.axe});

  Color get _couleurEtat {
    if (axe.actif) return AppColors.succes;
    if (axe.verrouille) return AppColors.marqueOrange;
    return AppColors.texteMuet;
  }

  String get _libelleEtat {
    if (axe.actif) return 'Actif';
    if (axe.verrouille) return 'Verrouillé';
    return 'Inactif';
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(
          color: axe.zoneSensible
              ? AppColors.erreur.withValues(alpha: 0.4)
              : AppColors.bordure,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(axe.nom,
                    style: Theme.of(context).textTheme.titleMedium),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: _couleurEtat.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(_libelleEtat,
                    style: TextStyle(color: _couleurEtat, fontSize: 11, fontWeight: FontWeight.bold)),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              const Icon(Icons.trip_origin, size: 14, color: AppColors.texteMuet),
              const SizedBox(width: 6),
              Text(axe.hubDepart, style: const TextStyle(color: AppColors.texteMuet, fontSize: 13)),
              const SizedBox(width: 8),
              const Icon(Icons.arrow_forward, size: 14, color: AppColors.texteMuet),
              const SizedBox(width: 8),
              const Icon(Icons.place, size: 14, color: AppColors.texteMuet),
              const SizedBox(width: 6),
              Text(axe.hubArrivee, style: const TextStyle(color: AppColors.texteMuet, fontSize: 13)),
            ],
          ),
          if (axe.zoneSensible) ...[
            const SizedBox(height: 10),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: AppColors.erreur.withValues(alpha: 0.08),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  const Icon(Icons.warning_amber, size: 16, color: AppColors.erreur),
                  const SizedBox(width: 6),
                  const Expanded(
                    child: Text(
                      'Zone sensible — traçabilité renforcée',
                      style: TextStyle(color: AppColors.erreur, fontSize: 12, fontWeight: FontWeight.w600),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }
}
