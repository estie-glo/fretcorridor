import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';

// ── Palette FretCorridor : blanc/rouge, cohérente avec le web (--fc-primary) ──
class AppColors {
  static const fond          = Color(0xFFF5F5F6);
  static const surface       = Color(0xFFFFFFFF);
  static const surfaceClaire = Color(0xFFFBEAEA);
  static const bordure       = Color(0xFFE4E4E7);
  static const accent        = Color(0xFFD40F16); // rouge de marque FretCorridor
  static const accentProfond = Color(0xFFB80D13); // rouge foncé (hover/pressed)
  static const marqueOrange  = Color(0xFFF59E0B); // accent ponctuel (zones sensibles)
  static const texte         = Color(0xFF0A0A0A);
  static const texteMuet     = Color(0xFF52525B);
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
