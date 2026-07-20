import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/chauffeur_model.dart';
import '../providers/auth_provider.dart';

class ProfilChauffeurScreen extends ConsumerWidget {
  final ChauffeurModel? chauffeur;
  const ProfilChauffeurScreen({super.key, this.chauffeur});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authProvider);

    // Données simulées si pas de chauffeur injecté
    final nom     = chauffeur?.nomComplet ?? 'Moussa Abdoulaye';
    final tel     = chauffeur?.telephone ?? '+237 699 000 001';
    final statut  = chauffeur?.statutKyc ?? 'VALIDE';
    final niveau  = chauffeur?.kycNiveau ?? 'NIVEAU_1';
    final badge   = chauffeur?.badgeKyc ?? 'KYC NIVEAU 1 validé ✅';
    final tenant  = chauffeur?.tenantId ?? authState.utilisateur?.tenantId ?? 'BGFT_CM';

    return Scaffold(
      backgroundColor: const Color(0xFF021526),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0A2540),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.pop(context),
        ),
        title: const Text('Mon profil',
            style: TextStyle(color: Colors.white, fontSize: 16)),
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [

          //  Avatar + nom 
          Center(
            child: Column(children: [
              CircleAvatar(
                radius: 40,
                backgroundColor: const Color(0xFF0A2540),
                child: Text(
                  nom.substring(0, 1),
                  style: const TextStyle(color: Color(0xFFF59E0B),
                      fontSize: 32, fontWeight: FontWeight.bold),
                ),
              ),
              const SizedBox(height: 12),
              Text(nom,
                  style: const TextStyle(color: Colors.white,
                      fontSize: 20, fontWeight: FontWeight.bold)),
              const SizedBox(height: 4),
              Text(tel,
                  style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 13)),
              const SizedBox(height: 8),
              // Badge KYC
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
                decoration: BoxDecoration(
                  color: _couleurStatut(statut).withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: _couleurStatut(statut).withValues(alpha: 0.4)),
                ),
                child: Text(badge,
                    style: TextStyle(color: _couleurStatut(statut),
                        fontSize: 12, fontWeight: FontWeight.bold)),
              ),
            ]),
          ),
          const SizedBox(height: 28),

          // ── Informations
          _SectionTitre(titre: 'Informations'),
          _InfoItem(label: 'Téléphone', valeur: tel, icon: Icons.phone),
          _InfoItem(label: 'Bureau de fret', valeur: _nomBureau(tenant), icon: Icons.business),
          _InfoItem(label: 'Tenant', valeur: tenant, icon: Icons.domain),
          const SizedBox(height: 20),

          // ── Statut KYC 
          _SectionTitre(titre: 'Statut KYC'),
          _KycNiveauCard(niveau: niveau, statut: statut),
          const SizedBox(height: 20),

          // ── Documents 
          _SectionTitre(titre: 'Documents'),
          _DocumentItem(
            titre: 'Carte Nationale d\'Identité',
            uploaded: chauffeur?.urlPhotoCNI != null,
            onUpload: () {}, // à brancher image_picker
          ),
          _DocumentItem(
            titre: 'Permis de conduire',
            uploaded: chauffeur?.urlPhotoPermis != null,
            onUpload: () {},
          ),
        ],
      ),
    );
  }

  Color _couleurStatut(String statut) {
    return switch (statut) {
      'VALIDE'     => const Color(0xFF10B981),
      'EN_ATTENTE' => const Color(0xFFF59E0B),
      'EN_COURS'   => const Color(0xFF3B82F6),
      'REJETE'     => const Color(0xFFEF4444),
      _            => const Color(0xFF94A3B8),
    };
  }

  String _nomBureau(String tenantId) {
    return switch (tenantId) {
      'BGFT_CM'  => 'BGFT Cameroun',
      'BNFT_TD'  => 'BNFT Tchad',
      'BARC_RCA' => 'BARC RCA',
      _          => tenantId,
    };
  }
}

// ── Widgets utilitaires ───────────────────────────────────────

class _SectionTitre extends StatelessWidget {
  final String titre;
  const _SectionTitre({required this.titre});
  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Text(titre,
          style: const TextStyle(color: Color(0xFFF59E0B),
              fontSize: 13, fontWeight: FontWeight.bold)),
    );
  }
}

class _InfoItem extends StatelessWidget {
  final String label;
  final String valeur;
  final IconData icon;
  const _InfoItem({required this.label, required this.valeur, required this.icon});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: const Color(0xFF0A2540),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: const Color(0xFF1E3A5F)),
      ),
      child: Row(children: [
        Icon(icon, color: const Color(0xFF94A3B8), size: 18),
        const SizedBox(width: 12),
        Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(label,
              style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 10,
                  letterSpacing: 0.8)),
          const SizedBox(height: 2),
          Text(valeur,
              style: const TextStyle(color: Colors.white, fontSize: 13,
                  fontWeight: FontWeight.w500)),
        ]),
      ]),
    );
  }
}

class _KycNiveauCard extends StatelessWidget {
  final String niveau;
  final String statut;
  const _KycNiveauCard({required this.niveau, required this.statut});

  @override
  Widget build(BuildContext context) {
    final niveaux = ['NIVEAU_0', 'NIVEAU_1', 'NIVEAU_2'];
    final indexActuel = niveaux.indexOf(niveau);

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF0A2540),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: const Color(0xFF1E3A5F)),
      ),
      child: Column(children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: niveaux.asMap().entries.map((e) {
            final atteint = e.key <= indexActuel;
            return Column(children: [
              CircleAvatar(
                radius: 16,
                backgroundColor: atteint
                    ? const Color(0xFF10B981)
                    : const Color(0xFF1E3A5F),
                child: Text('${e.key}',
                    style: TextStyle(
                        color: atteint ? Colors.white : const Color(0xFF94A3B8),
                        fontSize: 12, fontWeight: FontWeight.bold)),
              ),
              const SizedBox(height: 4),
              Text(e.key == 0 ? 'Aucun' : e.key == 1 ? 'Identité' : 'Complet',
                  style: TextStyle(
                      color: atteint ? const Color(0xFF10B981) : const Color(0xFF4B5563),
                      fontSize: 10)),
            ]);
          }).toList(),
        ),
        const SizedBox(height: 12),
        LinearProgressIndicator(
          value: indexActuel / 2,
          backgroundColor: const Color(0xFF1E3A5F),
          color: const Color(0xFF10B981),
          minHeight: 6,
          borderRadius: BorderRadius.circular(3),
        ),
      ]),
    );
  }
}

class _DocumentItem extends StatelessWidget {
  final String titre;
  final bool uploaded;
  final VoidCallback onUpload;
  const _DocumentItem({required this.titre, required this.uploaded, required this.onUpload});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: const Color(0xFF0A2540),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(
          color: uploaded
              ? const Color(0xFF10B981).withValues(alpha: 0.4)
              : const Color(0xFF1E3A5F),
        ),
      ),
      child: Row(children: [
        Icon(
          uploaded ? Icons.check_circle : Icons.upload_file,
          color: uploaded ? const Color(0xFF10B981) : const Color(0xFF94A3B8),
          size: 20,
        ),
        const SizedBox(width: 12),
        Expanded(child: Text(titre,
            style: const TextStyle(color: Colors.white, fontSize: 13))),
        if (!uploaded)
          TextButton(
            onPressed: onUpload,
            child: const Text('Uploader',
                style: TextStyle(color: Color(0xFFF59E0B), fontSize: 12)),
          )
        else
          const Text('Uploadé ✅',
              style: TextStyle(color: Color(0xFF10B981), fontSize: 12)),
      ]),
    );
  }
}
