import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';

// ── Palette : blanc/bleu clair, lumineux et pro ──
class AppColors {
  static const fond          = Color(0xFFF7F9FC);
  static const surface       = Color(0xFFFFFFFF);
  static const surfaceClaire = Color(0xFFEAF1FB);
  static const bordure       = Color(0xFFE1E7F0);
  static const accent        = Color(0xFF2563EB); // bleu principal
  static const accentProfond = Color(0xFF1E40AF); // bleu foncé, touche secondaire
  static const marqueOrange  = Color(0xFFF59E0B); // orange Flysoft, accent ponctuel
  static const texte         = Color(0xFF111827);
  static const texteMuet     = Color(0xFF64748B);
  static const texteBouton   = Colors.white;
  static const succes        = Color(0xFF15803D);
  static const erreur        = Color(0xFFDC2626);
}

class AppTheme {
  static ThemeData get theme {
    final base = ThemeData.light();
    return base.copyWith(
      scaffoldBackgroundColor: AppColors.fond,
      colorScheme: base.colorScheme.copyWith(
        surface: AppColors.surface,
        primary: AppColors.accent,
        error: AppColors.erreur,
      ),
      textTheme: GoogleFonts.workSansTextTheme(base.textTheme).copyWith(
        headlineLarge: GoogleFonts.fraunces(
          fontSize: 28, fontWeight: FontWeight.w600, color: AppColors.texte,
        ),
        headlineMedium: GoogleFonts.fraunces(
          fontSize: 20, fontWeight: FontWeight.w600, color: AppColors.texte,
        ),
        titleMedium: GoogleFonts.fraunces(
          fontSize: 16, fontWeight: FontWeight.w600, color: AppColors.texte,
        ),
        bodyMedium: GoogleFonts.workSans(fontSize: 14, color: AppColors.texte),
        bodySmall: GoogleFonts.workSans(fontSize: 12, color: AppColors.texteMuet),
      ),
      appBarTheme: AppBarTheme(
        backgroundColor: AppColors.fond,
        elevation: 0,
        titleTextStyle: GoogleFonts.fraunces(
          fontSize: 18, fontWeight: FontWeight.w600, color: AppColors.texte,
        ),
        systemOverlayStyle: SystemUiOverlayStyle.dark,
      ),
    );
  }
}
