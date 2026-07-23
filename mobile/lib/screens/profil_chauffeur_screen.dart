import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/profil_chauffeur_provider.dart';
import '../theme/app_theme.dart';

class ProfilChauffeurScreen extends ConsumerStatefulWidget {
  const ProfilChauffeurScreen({super.key});

  @override
  ConsumerState<ProfilChauffeurScreen> createState() =>
      _ProfilChauffeurScreenState();
}

class _ProfilChauffeurScreenState extends ConsumerState<ProfilChauffeurScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(
        () => ref.read(profilChauffeurProvider.notifier).chargerProfil());
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(profilChauffeurProvider);
    final chauffeur = state.chauffeur;

    return Scaffold(
      backgroundColor: AppColors.fond,
      appBar: AppBar(
        title: const Text('Mon profil'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: state.chargement
                ? null
                : () => ref.read(profilChauffeurProvider.notifier).chargerProfil(),
          ),
        ],
      ),
      body: state.chargement && chauffeur == null
          ? const Center(child: CircularProgressIndicator())
          : chauffeur == null
              ? _ErreurView(message: state.erreur ?? 'Profil indisponible')
              : ListView(
                  padding: const EdgeInsets.all(20),
                  children: [
                    if (state.erreur != null)
                      _Bandeau(text: state.erreur!, erreur: true),
                    if (state.succes != null)
                      _Bandeau(text: state.succes!, erreur: false),
                    Center(
                      child: Column(children: [
                        CircleAvatar(
                          radius: 40,
                          backgroundColor: AppColors.surfaceClaire,
                          child: Text(
                            chauffeur.prenom.isNotEmpty
                                ? chauffeur.prenom.substring(0, 1)
                                : '?',
                            style: const TextStyle(
                              color: AppColors.accent,
                              fontSize: 32,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                        const SizedBox(height: 12),
                        Text(chauffeur.nomComplet,
                            style: Theme.of(context).textTheme.headlineMedium),
                        const SizedBox(height: 4),
                        Text(chauffeur.telephone,
                            style: Theme.of(context).textTheme.bodySmall),
                        const SizedBox(height: 8),
                        _BadgeKyc(text: chauffeur.badgeKyc, statut: chauffeur.statutKyc),
                      ]),
                    ),
                    const SizedBox(height: 24),
                    _SectionTitre(titre: 'Informations'),
                    _InfoItem(
                        label: 'Bureau de fret',
                        valeur: _nomBureau(chauffeur.tenantId),
                        icon: Icons.business),
                    _InfoItem(
                        label: 'Tenant',
                        valeur: chauffeur.tenantId,
                        icon: Icons.domain),
                    const SizedBox(height: 16),
                    _SectionTitre(titre: 'Documents KYC'),
                    _DocumentItem(
                      titre: 'Carte Nationale d\'Identité',
                      uploaded: chauffeur.urlPhotoCNI != null,
                      loading: state.uploadEnCours,
                      onUpload: () => ref
                          .read(profilChauffeurProvider.notifier)
                          .uploaderDocument('CNI'),
                    ),
                    _DocumentItem(
                      titre: 'Permis de conduire',
                      uploaded: chauffeur.urlPhotoPermis != null,
                      loading: state.uploadEnCours,
                      onUpload: () => ref
                          .read(profilChauffeurProvider.notifier)
                          .uploaderDocument('PERMIS'),
                    ),
                  ],
                ),
    );
  }

  String _nomBureau(String tenantId) {
    return switch (tenantId) {
      'BGFT_CM' => 'BGFT Cameroun',
      'BNFT_TD' => 'BNFT Tchad',
      'BARC_RCA' => 'BARC RCA',
      _ => tenantId,
    };
  }
}

class _ErreurView extends StatelessWidget {
  final String message;
  const _ErreurView({required this.message});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Text(message, textAlign: TextAlign.center),
      ),
    );
  }
}

class _Bandeau extends StatelessWidget {
  final String text;
  final bool erreur;
  const _Bandeau({required this.text, required this.erreur});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: (erreur ? AppColors.erreur : AppColors.succes).withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(text,
          style: TextStyle(
              color: erreur ? AppColors.erreur : AppColors.succes, fontSize: 13)),
    );
  }
}

class _BadgeKyc extends StatelessWidget {
  final String text;
  final String statut;
  const _BadgeKyc({required this.text, required this.statut});

  @override
  Widget build(BuildContext context) {
    final color = switch (statut) {
      'VALIDE' => AppColors.succes,
      'EN_ATTENTE' => AppColors.marqueOrange,
      'REJETE' => AppColors.erreur,
      _ => AppColors.texteMuet,
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withValues(alpha: 0.35)),
      ),
      child: Text(text,
          style: TextStyle(color: color, fontSize: 12, fontWeight: FontWeight.bold)),
    );
  }
}

class _SectionTitre extends StatelessWidget {
  final String titre;
  const _SectionTitre({required this.titre});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(titre, style: Theme.of(context).textTheme.titleMedium),
    );
  }
}

class _InfoItem extends StatelessWidget {
  final String label;
  final String valeur;
  final IconData icon;
  const _InfoItem(
      {required this.label, required this.valeur, required this.icon});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AppColors.bordure),
      ),
      child: Row(children: [
        Icon(icon, color: AppColors.texteMuet, size: 18),
        const SizedBox(width: 12),
        Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(label.toUpperCase(),
              style: Theme.of(context).textTheme.bodySmall),
          Text(valeur, style: Theme.of(context).textTheme.bodyMedium),
        ]),
      ]),
    );
  }
}

class _DocumentItem extends StatelessWidget {
  final String titre;
  final bool uploaded;
  final bool loading;
  final VoidCallback onUpload;
  const _DocumentItem({
    required this.titre,
    required this.uploaded,
    required this.loading,
    required this.onUpload,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(
          color: uploaded ? AppColors.succes : AppColors.bordure,
        ),
      ),
      child: Row(children: [
        Icon(
          uploaded ? Icons.check_circle : Icons.upload_file,
          color: uploaded ? AppColors.succes : AppColors.texteMuet,
        ),
        const SizedBox(width: 12),
        Expanded(child: Text(titre)),
        if (!uploaded)
          TextButton(
            onPressed: loading ? null : onUpload,
            child: loading
                ? const SizedBox(
                    width: 16,
                    height: 16,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Text('Photographier'),
          )
        else
          const Text('Uploadé ✅',
              style: TextStyle(color: AppColors.succes, fontSize: 12)),
      ]),
    );
  }
}
