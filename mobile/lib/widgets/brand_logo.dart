import 'package:flutter/material.dart';

/// Petite marque FretCorridor pour les barres de titre — même logo que
/// l'écran de connexion et le splash, à taille réduite. Équivalent mobile
/// du `<app-brand-logo size="sm">` du web (présence de marque persistante
/// sur chaque page, pas seulement à la connexion).
class BrandLogo extends StatelessWidget {
  final double height;

  const BrandLogo({super.key, this.height = 22});

  @override
  Widget build(BuildContext context) {
    return Image.asset('assets/images/fretcorridor_logo.png', height: height);
  }
}
