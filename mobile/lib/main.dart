import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'screens/login_screen.dart';
import 'providers/auth_provider.dart';
import 'screens/dashboard_agent_screen.dart';
import 'screens/dashboard_chauffeur_screen.dart';
import 'screens/enrolement_screen.dart';
import 'theme/app_theme.dart';
import 'screens/axes_screen.dart';
import 'screens/declaration_vide_screen.dart';
import 'screens/profil_chauffeur_screen.dart';
import 'screens/notifications_screen.dart';
import 'screens/matchs_screen.dart';
import 'screens/changer_pin_screen.dart';

void main() {
  runApp(
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
      routes: {
        '/dashboard-chauffeur': (context) => const DashboardChauffeurScreen(),
        '/dashboard-agent': (context) => const DashboardAgentScreen(),
        '/enrolement': (context) => const EnrolementScreen(),
        '/profil-chauffeur': (context) => const ProfilChauffeurScreen(),
        '/dashboard-client': (context) => const PlaceholderDashboard(role: 'Client'),
        '/axes': (context) => const AxesScreen(),
        '/declaration-vide': (context) => const DeclarationVideScreen(),
        '/notifications': (context) => const NotificationsScreen(),
        '/matchs': (context) => const MatchsScreen(),
        '/changer-pin': (context) => const ChangerPinScreen(),
      },
      home: authState.chargement
          ? const SplashScreen()
          : authState.estConnecte
              ? (authState.utilisateur!.pinTemporaire
                  ? const ChangerPinScreen()
                  : _homeForRole(authState.utilisateur!.role))
              : const LoginScreen(),
    );
  }

  Widget _homeForRole(String role) {
    return switch (role) {
      'AGENT' => const DashboardAgentScreen(),
      'CHAUFFEUR' => const DashboardChauffeurScreen(),
      _ => PlaceholderDashboard(role: role),
    };
  }
}

class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      backgroundColor: AppColors.fond,
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(color: AppColors.accent),
            SizedBox(height: 16),
            Text(
              'FretCorridor',
              style: TextStyle(
                color: AppColors.texte,
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

class PlaceholderDashboard extends ConsumerWidget {
  final String role;
  const PlaceholderDashboard({super.key, required this.role});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authProvider);
    return Scaffold(
      backgroundColor: AppColors.fond,
      appBar: AppBar(
        title: Text('Dashboard $role'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () => ref.read(authProvider.notifier).logout(),
          ),
        ],
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.construction, color: AppColors.texteMuet, size: 64),
            const SizedBox(height: 16),
            Text('Rôle $role — écran à venir',
                style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 8),
            Text(
              'Tenant : ${authState.utilisateur?.tenantId ?? ""}',
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
        ),
      ),
    );
  }
}
