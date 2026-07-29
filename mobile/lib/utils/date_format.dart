/// Formatage lisible des dates et positions — sans dépendance externe,
/// cohérent avec l'équivalent web (relative-time.ts).

/// "il y a 12 min" — pour une date passée (ex. date de déclaration).
String? formatRelativeTime(String? iso) {
  if (iso == null || iso.isEmpty) return null;
  final date = DateTime.tryParse(iso);
  if (date == null) return null;

  final diff = DateTime.now().difference(date);
  final minutes = diff.inMinutes;

  if (minutes < 1) return "à l'instant";
  if (minutes < 60) return 'il y a $minutes min';
  final heures = diff.inHours;
  if (heures < 24) return 'il y a $heures h';
  final jours = diff.inDays;
  return 'il y a $jours j';
}

/// "21/07/2026 10:23" — date/heure absolue, heure locale.
String? formatDateHeure(String? iso) {
  if (iso == null || iso.isEmpty) return null;
  final date = DateTime.tryParse(iso);
  if (date == null) return null;
  final local = date.toLocal();
  String deux(int n) => n.toString().padLeft(2, '0');
  return '${deux(local.day)}/${deux(local.month)}/${local.year} '
      '${deux(local.hour)}:${deux(local.minute)}';
}

/// "4.0511, 9.7679" — coordonnées, 4 décimales (~10 m de précision).
String? formatCoordonnees(double? latitude, double? longitude) {
  if (latitude == null || longitude == null) return null;
  return '${latitude.toStringAsFixed(4)}, ${longitude.toStringAsFixed(4)}';
}
