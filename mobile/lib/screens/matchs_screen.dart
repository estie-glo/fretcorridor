import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/matchs_provider.dart';
import '../theme/app_theme.dart';
import '../utils/date_format.dart';

class MatchsScreen extends ConsumerStatefulWidget {
  const MatchsScreen({super.key});

  @override
  ConsumerState<MatchsScreen> createState() => _MatchsScreenState();
}

class _MatchsScreenState extends ConsumerState<MatchsScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() => ref.read(matchsProvider.notifier).charger());
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(matchsProvider);

    return Scaffold(
      backgroundColor: AppColors.fond,
      appBar: AppBar(title: const Text('Matchs disponibles')),
      body: state.chargement
          ? const Center(child: CircularProgressIndicator())
          : state.erreur != null
              ? Center(child: Text(state.erreur!))
              : state.matchs.isEmpty
                  ? Center(
                      child: Padding(
                        padding: const EdgeInsets.all(24),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.search_off,
                                size: 48, color: AppColors.texteMuet),
                            const SizedBox(height: 12),
                            Text(
                              'Aucun match pour le moment',
                              style: Theme.of(context).textTheme.titleMedium,
                            ),
                            const SizedBox(height: 8),
                            Text(
                              'Le moteur de matching backhaul sera activé au sprint S9.',
                              textAlign: TextAlign.center,
                              style: Theme.of(context).textTheme.bodySmall,
                            ),
                          ],
                        ),
                      ),
                    )
                  : RefreshIndicator(
                      onRefresh: () =>
                          ref.read(matchsProvider.notifier).charger(),
                      child: ListView.separated(
                        padding: const EdgeInsets.all(16),
                        itemCount: state.matchs.length,
                        separatorBuilder: (_, __) => const SizedBox(height: 8),
                        itemBuilder: (context, index) {
                          final m = state.matchs[index];
                          final relatif = formatRelativeTime(m.dateDeclaration);
                          final position = formatCoordonnees(m.latitude, m.longitude);
                          return Card(
                            child: ListTile(
                              title: Text(m.axeNom ?? 'Axe'),
                              subtitle: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                      '${m.typeCamion ?? ''} · ${m.capaciteTonnes ?? ''} t · ${m.statut}'),
                                  const SizedBox(height: 4),
                                  Row(
                                    children: [
                                      const Icon(Icons.access_time,
                                          size: 13, color: AppColors.texteMuet),
                                      const SizedBox(width: 4),
                                      Text(
                                        relatif ?? 'Date inconnue',
                                        style: const TextStyle(
                                            fontSize: 12, color: AppColors.texteMuet),
                                      ),
                                    ],
                                  ),
                                  const SizedBox(height: 2),
                                  Row(
                                    children: [
                                      const Icon(Icons.place_outlined,
                                          size: 13, color: AppColors.texteMuet),
                                      const SizedBox(width: 4),
                                      Expanded(
                                        child: Text(
                                          position ?? 'Position non transmise',
                                          style: const TextStyle(
                                              fontSize: 12, color: AppColors.texteMuet),
                                          overflow: TextOverflow.ellipsis,
                                        ),
                                      ),
                                    ],
                                  ),
                                ],
                              ),
                              isThreeLine: true,
                              trailing: m.zoneSensible
                                  ? const Icon(Icons.warning_amber,
                                      color: AppColors.marqueOrange)
                                  : null,
                            ),
                          );
                        },
                      ),
                    ),
    );
  }
}
