import 'package:flutter/material.dart';

/// Modern Clinical Calm Design System Colors & Typography
class ClinicalColors {
  // Core Swatches
  static const Color primaryTeal = Color(0xFF0D9488);
  static const Color m3PrimaryTeal = Color(0xFF00685F);
  static const Color primaryContainer = Color(0xFF008378);
  static const Color primaryFixed = Color(0xFF89F5E7);

  static const Color secondaryOceanic = Color(0xFF0284C7);
  static const Color secondaryContainer = Color(0xFF5BB8FE);

  static const Color tertiaryEmerald = Color(0xFF10B981);
  static const Color neutralSlate = Color(0xFF64748B);
  static const Color slateLabelDark = Color(0xFF334155);
  static const Color onSurfaceDarkNavy = Color(0xFF0B1C30);

  // Extended Semantic & Alert Roles
  static const Color warningAmber = Color(0xFFF59E0B);
  static const Color criticalRed = Color(0xFFEF4444);

  // Canvas & Surface Elevation
  static const Color canvasBase = Color(0xFFF8FAFC);
  static const Color surfaceWhite = Color(0xFFFFFFFF);
  static const Color neutralChipSurface = Color(0xFFF1F5F9);
  static const Color borderDivider = Color(0xFFE2E8F0);
  static const Color inputBorder = Color(0xFFCBD5E1);
  static const Color metadataRule = Color(0xFFF1F5F9);

  // Emergency Accentuation
  static const Color emergencyBorderTint = Color(0xFFFECACA);
  static const Color emergencySurfaceTint = Color(0xFFFFF5F5);
  static const Color emergencyAmbientGlow = Color(0x14EF4444);

  // Status Pill Badges
  static const Color badgeNormalBg = Color(0xFFD1FAE5);
  static const Color badgeNormalText = Color(0xFF065F46);

  static const Color badgePendingBg = Color(0xFFFEF3C7);
  static const Color badgePendingText = Color(0xFF92400E);

  static const Color badgeCriticalBg = Color(0xFFFEE2E2);
  static const Color badgeCriticalText = Color(0xFF991B1B);

  static const Color badgeOceanicBg = Color(0xFFE0F2FE);
  static const Color badgeOceanicText = Color(0xFF075985);
}

class ClinicalTypography {
  static const String displayFont = 'PlusJakartaSans';
  static const String bodyFont = 'Inter';

  static const TextStyle displayLg = TextStyle(
    fontFamily: displayFont,
    fontSize: 32,
    fontWeight: FontWeight.w700,
    height: 40 / 32,
    color: ClinicalColors.onSurfaceDarkNavy,
  );

  static const TextStyle headlineLg = TextStyle(
    fontFamily: displayFont,
    fontSize: 26,
    fontWeight: FontWeight.w600,
    height: 34 / 26,
    color: ClinicalColors.onSurfaceDarkNavy,
  );

  static const TextStyle headlineMd = TextStyle(
    fontFamily: displayFont,
    fontSize: 22,
    fontWeight: FontWeight.w600,
    height: 28 / 22,
    color: ClinicalColors.onSurfaceDarkNavy,
  );

  static const TextStyle headlineSm = TextStyle(
    fontFamily: displayFont,
    fontSize: 18,
    fontWeight: FontWeight.w600,
    height: 24 / 18,
    color: ClinicalColors.onSurfaceDarkNavy,
  );

  static const TextStyle titleMd = TextStyle(
    fontFamily: displayFont,
    fontSize: 16,
    fontWeight: FontWeight.w600,
    height: 22 / 16,
    color: ClinicalColors.onSurfaceDarkNavy,
  );

  static const TextStyle metricNumeral = TextStyle(
    fontFamily: displayFont,
    fontSize: 28,
    fontWeight: FontWeight.w700,
    height: 32 / 28,
    color: ClinicalColors.onSurfaceDarkNavy,
  );

  static const TextStyle bodyLg = TextStyle(
    fontFamily: bodyFont,
    fontSize: 16,
    fontWeight: FontWeight.w400,
    height: 24 / 16,
    color: ClinicalColors.slateLabelDark,
  );

  static const TextStyle bodyMd = TextStyle(
    fontFamily: bodyFont,
    fontSize: 14,
    fontWeight: FontWeight.w400,
    height: 20 / 14,
    color: ClinicalColors.neutralSlate,
  );

  static const TextStyle bodySm = TextStyle(
    fontFamily: bodyFont,
    fontSize: 12,
    fontWeight: FontWeight.w400,
    height: 16 / 12,
    color: ClinicalColors.neutralSlate,
  );

  static const TextStyle labelMd = TextStyle(
    fontFamily: bodyFont,
    fontSize: 13,
    fontWeight: FontWeight.w600,
    height: 16 / 13,
    letterSpacing: 0.26,
    color: ClinicalColors.slateLabelDark,
  );

  static const TextStyle labelSm = TextStyle(
    fontFamily: bodyFont,
    fontSize: 11,
    fontWeight: FontWeight.w600,
    height: 14 / 11,
    letterSpacing: 0.22,
    color: ClinicalColors.neutralSlate,
  );
}

class ClinicalTheme {
  static ThemeData get lightTheme {
    return ThemeData(
      useMaterial3: true,
      scaffoldBackgroundColor: ClinicalColors.canvasBase,
      colorScheme: ColorScheme.fromSeed(
        seedColor: ClinicalColors.primaryTeal,
        primary: ClinicalColors.primaryTeal,
        onPrimary: Colors.white,
        secondary: ClinicalColors.secondaryOceanic,
        onSecondary: Colors.white,
        tertiary: ClinicalColors.tertiaryEmerald,
        error: ClinicalColors.criticalRed,
        surface: ClinicalColors.surfaceWhite,
        onSurface: ClinicalColors.onSurfaceDarkNavy,
      ),
      textTheme: const TextTheme(
        displayLarge: ClinicalTypography.displayLg,
        headlineLarge: ClinicalTypography.headlineLg,
        headlineMedium: ClinicalTypography.headlineMd,
        headlineSmall: ClinicalTypography.headlineSm,
        titleMedium: ClinicalTypography.titleMd,
        bodyLarge: ClinicalTypography.bodyLg,
        bodyMedium: ClinicalTypography.bodyMd,
        bodySmall: ClinicalTypography.bodySm,
        labelMedium: ClinicalTypography.labelMd,
        labelSmall: ClinicalTypography.labelSm,
      ),
    );
  }
}
