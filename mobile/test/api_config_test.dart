import 'package:flutter_test/flutter_test.dart';
import 'package:fretcorridor_mobile/core/config/api_config.dart';

void main() {
  group('ApiConfig', () {
    test('baseUrl se termine par /api', () {
      expect(ApiConfig.baseUrl.endsWith('/api'), isTrue);
    });

    test('baseUrl est une URL http(s) valide', () {
      final uri = Uri.tryParse(ApiConfig.baseUrl);
      expect(uri, isNotNull);
      expect(uri!.hasScheme, isTrue);
      expect(uri.scheme == 'http' || uri.scheme == 'https', isTrue);
    });
  });
}
