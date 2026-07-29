import 'package:flutter_test/flutter_test.dart';
import 'package:fretcorridor_mobile/utils/date_format.dart';

void main() {
  group('formatRelativeTime', () {
    test('retourne null si la date est absente ou invalide', () {
      expect(formatRelativeTime(null), isNull);
      expect(formatRelativeTime(''), isNull);
      expect(formatRelativeTime('pas-une-date'), isNull);
    });

    test('formate en minutes', () {
      final iso = DateTime.now().subtract(const Duration(minutes: 12)).toIso8601String();
      expect(formatRelativeTime(iso), 'il y a 12 min');
    });

    test('bascule en heures au-delà de 60 minutes', () {
      final iso = DateTime.now().subtract(const Duration(hours: 3)).toIso8601String();
      expect(formatRelativeTime(iso), 'il y a 3 h');
    });

    test('bascule en jours au-delà de 24 heures', () {
      final iso = DateTime.now().subtract(const Duration(hours: 50)).toIso8601String();
      expect(formatRelativeTime(iso), 'il y a 2 j');
    });
  });

  group('formatDateHeure', () {
    test('retourne null si la date est absente ou invalide', () {
      expect(formatDateHeure(null), isNull);
      expect(formatDateHeure('pas-une-date'), isNull);
    });

    test('formate une date ISO en date/heure locale lisible', () {
      final formatted = formatDateHeure('2026-07-21T10:23:00Z');
      expect(formatted, matches(RegExp(r'^\d{2}/\d{2}/2026 \d{2}:\d{2}$')));
    });
  });

  group('formatCoordonnees', () {
    test('retourne null si latitude ou longitude manque', () {
      expect(formatCoordonnees(null, 9.7679), isNull);
      expect(formatCoordonnees(4.0511, null), isNull);
    });

    test('formate les deux coordonnées avec 4 décimales', () {
      expect(formatCoordonnees(4.05111, 9.76789), '4.0511, 9.7679');
    });
  });
}
