import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:fretcorridor_mobile/main.dart';

void main() {
  testWidgets('affiche FretCorridor au démarrage (splash ou login)',
      (WidgetTester tester) async {
    await tester.pumpWidget(
      const ProviderScope(child: FretCorridorApp()),
    );
    await tester.pump();

    expect(find.text('FretCorridor'), findsOneWidget);
  });
}
