import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'screens/login_screen.dart';
import 'providers/auth_provider.dart';
import 'screens/dashboard_agent_screen.dart';
import 'screens/enrolement_screen.dart';
import 'theme/app_theme.dart';
import 'screens/axes_screen.dart';
import 'screens/declaration_vide_screen.dart';
import 'screens/profil_chauffeur_screen.dart';


void main() {
  runApp(
    // ProviderScope est obligatoire pour Riverpod
    const ProviderScope(
      child: FretCorridorApp(),
    ),
  );
}

class FretCorridorApp extends ConsumerWidget {
  const FretCorridorApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authProvider);

    return MaterialApp(
      title: 'FretCorridor',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.theme,

      // Navigation simple selon l'état d'auth
     routes: {
  '/dashboard-chauffeur': (context) => const PlaceholderDashboard(role: 'Chauffeur'),
  '/dashboard-agent': (context) => const DashboardAgentScreen(),
  '/enrolement': (context) => const EnrolementScreen(),
  '/profil-chauffeur': (context) => const ProfilChauffeurScreen(),
  '/dashboard-client': (context) => const PlaceholderDashboard(role: 'Client'),
  '/axes': (context) => const AxesScreen(),
  '/declaration-vide': (context) => const DeclarationVideScreen(),
  },
           // Page de démarrage
      home: authState.chargement
          ? const SplashScreen()
          : authState.estConnecte
              ? authState.utilisateur!.role == 'AGENT'
                  ? const DashboardAgentScreen()
                  : PlaceholderDashboard(
                      role: authState.utilisateur!.role,
                    )
              : const LoginScreen(),
    );
  }
}

// ── Splash Screen (pendant la vérification de session) ────────
class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      backgroundColor: Color(0xFF021526),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(color: Color(0xFFF59E0B)),
            SizedBox(height: 16),
            Text(
              'FretCorridor',
              style: TextStyle(
                color: Colors.white,
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Dashboard placeholder (à remplacer Sprint 2) ──────────────
class PlaceholderDashboard extends ConsumerWidget {
  final String role;
  const PlaceholderDashboard({super.key, required this.role});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authProvider);
    return Scaffold(
      backgroundColor: AppColors.fond,
      appBar: AppBar(
        title: Text(
          'Dashboard $role',
          style: const TextStyle(color: AppColors.texte),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout, color: AppColors.accent),
            onPressed: () => ref.read(authProvider.notifier).logout(),
          ),
        ],
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.check_circle, color: AppColors.succes, size: 64),
            const SizedBox(height: 16),
            Text(
              'Connecté en tant que $role',
              style: const TextStyle(color: AppColors.texte, fontSize: 18),
            ),
            const SizedBox(height: 8),
            Text(
              'Tenant : ${authState.utilisateur?.tenantId ?? ""}',
              style: const TextStyle(color: AppColors.texteMuet, fontSize: 14),
            ),
            const SizedBox(height: 8),
            Text(
              authState.utilisateur?.configTenant.nomBureau ?? '',
              style: const TextStyle(color: AppColors.accent, fontSize: 14),
            ),
            const SizedBox(height: 32),
            const Text(
              'Sprint 3 → Réseau & Axes',
              style: TextStyle(color: AppColors.texteMuet, fontSize: 12),
            ),
            const SizedBox(height: 20),
            ElevatedButton.icon(
              onPressed: () => Navigator.pushNamed(context, '/axes'),
              icon: const Icon(Icons.route),
              label: const Text('Voir les axes'),
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.accent,
                foregroundColor: Colors.white,
              ),
            ),
            const SizedBox(height: 12),
            ElevatedButton.icon(
              onPressed: () => Navigator.pushNamed(context, '/declaration-vide'),
              icon: const Icon(Icons.local_shipping_outlined),
              label: const Text('Déclarer camion vide'),
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.marqueOrange,
                foregroundColor: Colors.white,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
