import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/notification_provider.dart';
import '../theme/app_theme.dart';
import '../widgets/brand_logo.dart';

class NotificationsScreen extends ConsumerStatefulWidget {
  const NotificationsScreen({super.key});

  @override
  ConsumerState<NotificationsScreen> createState() =>
      _NotificationsScreenState();
}

class _NotificationsScreenState extends ConsumerState<NotificationsScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() => ref.read(notificationProvider.notifier).charger());
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(notificationProvider);

    return Scaffold(
      backgroundColor: AppColors.fond,
      appBar: AppBar(
        title: const Row(
          children: [
            BrandLogo(),
            SizedBox(width: 10),
            Flexible(
                child: Text('Notifications', overflow: TextOverflow.ellipsis)),
          ],
        ),
        actions: [
          if (state.nonLues > 0)
            Padding(
              padding: const EdgeInsets.only(right: 12),
              child: Center(
                child: Chip(
                  label: Text('${state.nonLues}'),
                  backgroundColor:
                      AppColors.marqueOrange.withValues(alpha: 0.15),
                ),
              ),
            ),
        ],
      ),
      body: state.chargement
          ? const Center(child: CircularProgressIndicator())
          : state.erreur != null
              ? Center(child: Text(state.erreur!))
              : state.notifications.isEmpty
                  ? const Center(child: Text('Aucune notification'))
                  : RefreshIndicator(
                      onRefresh: () =>
                          ref.read(notificationProvider.notifier).charger(),
                      child: ListView.separated(
                        padding: const EdgeInsets.all(16),
                        itemCount: state.notifications.length,
                        separatorBuilder: (_, __) => const SizedBox(height: 8),
                        itemBuilder: (context, index) {
                          final n = state.notifications[index];
                          return _NotificationTile(
                            titre: n.titreFr.isNotEmpty ? n.titreFr : n.titreEn,
                            corps: n.corpsFr.isNotEmpty ? n.corpsFr : n.corpsEn,
                            lue: n.lue,
                            onTap: () => ref
                                .read(notificationProvider.notifier)
                                .marquerLue(n.id),
                          );
                        },
                      ),
                    ),
    );
  }
}

class _NotificationTile extends StatelessWidget {
  final String titre;
  final String corps;
  final bool lue;
  final VoidCallback onTap;

  const _NotificationTile({
    required this.titre,
    required this.corps,
    required this.lue,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      color: lue ? AppColors.surface : AppColors.surfaceClaire,
      borderRadius: BorderRadius.circular(12),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: AppColors.bordure),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(children: [
                Expanded(
                  child: Text(titre,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: lue ? FontWeight.w500 : FontWeight.w700,
                          )),
                ),
                if (!lue)
                  const Icon(Icons.circle, size: 8, color: AppColors.accent),
              ]),
              const SizedBox(height: 6),
              Text(corps, style: Theme.of(context).textTheme.bodySmall),
            ],
          ),
        ),
      ),
    );
  }
}
