import "package:flutter_test/flutter_test.dart";
import "package:my_medical_history/main.dart";

void main() {
  testWidgets("App renders My Medical History dashboard", (WidgetTester tester) async {
    await tester.pumpWidget(const MyMedicalHistoryFlutterApp());
    expect(find.text("Clara Vance-Sterling"), findsOneWidget);
  });
}
