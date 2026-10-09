import 'package:flutter/material.dart';
import '../models/medical_models.dart';
import '../theme/clinical_theme.dart';

class StatusPillBadge extends StatelessWidget {
  final String statusType;
  final String label;

  const StatusPillBadge({
    super.key,
    required this.statusType,
    required this.label,
  });

  @override
  Widget build(BuildContext context) {
    Color bg;
    Color fg;
    switch (statusType.toUpperCase()) {
      case 'NORMAL':
      case 'ADHERENT':
      case 'COMPLETED':
        bg = ClinicalColors.badgeNormalBg;
        fg = ClinicalColors.badgeNormalText;
        break;
      case 'PENDING':
      case 'REFILL':
      case 'WARNING':
        bg = ClinicalColors.badgePendingBg;
        fg = ClinicalColors.badgePendingText;
        break;
      case 'CRITICAL':
      case 'ALLERGY':
      case 'EMERGENCY':
        bg = ClinicalColors.badgeCriticalBg;
        fg = ClinicalColors.badgeCriticalText;
        break;
      default:
        bg = ClinicalColors.badgeOceanicBg;
        fg = ClinicalColors.badgeOceanicText;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 6,
            height: 6,
            decoration: BoxDecoration(color: fg, shape: BoxShape.circle),
          ),
          const SizedBox(width: 5),
          Text(
            label,
            style: ClinicalTypography.labelSm.copyWith(color: fg),
          ),
        ],
      ),
    );
  }
}

/// 24x24dp visual box inside 48x48dp touch target, 6px radius.
/// Unchecked: #E2E8F0 border. Checked: #10B981 fill with crisp white checkmark.
class AdherenceCheckbox extends StatelessWidget {
  final bool checked;
  final VoidCallback onChanged;

  const AdherenceCheckbox({
    super.key,
    required this.checked,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onChanged,
      borderRadius: BorderRadius.circular(24),
      child: SizedBox(
        width: 48,
        height: 48,
        child: Center(
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            width: 24,
            height: 24,
            decoration: BoxDecoration(
              color: checked ? ClinicalColors.tertiaryEmerald : Colors.white,
              borderRadius: BorderRadius.circular(6),
              border: Border.all(
                color: checked
                    ? ClinicalColors.tertiaryEmerald
                    : ClinicalColors.borderDivider,
                width: 1.5,
              ),
            ),
            child: checked
                ? const Icon(Icons.check, size: 16, color: Colors.white)
                : null,
          ),
        ),
      ),
    );
  }
}

/// Contained pill segment container (#F1F5F9) with sliding #FFFFFF elevation pill indicator.
class SegmentedTimeframeSelector extends StatelessWidget {
  final String selected;
  final ValueChanged<String> onSelect;
  static const List<String> options = ['7D', '1M', '6M', '1Y'];

  const SegmentedTimeframeSelector({
    super.key,
    required this.selected,
    required this.onSelect,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 44,
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: ClinicalColors.neutralChipSurface,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: ClinicalColors.borderDivider),
      ),
      child: Row(
        children: options.map((tf) {
          final isSelected = tf == selected;
          return Expanded(
            child: GestureDetector(
              onTap: () => onSelect(tf),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 200),
                decoration: BoxDecoration(
                  color: isSelected ? Colors.white : Colors.transparent,
                  borderRadius: BorderRadius.circular(999),
                  boxShadow: isSelected
                      ? [
                          const BoxShadow(
                            color: Color(0x140F172A),
                            blurRadius: 4,
                            offset: Offset(0, 1),
                          )
                        ]
                      : null,
                ),
                alignment: Alignment.center,
                child: Text(
                  tf,
                  style: ClinicalTypography.labelMd.copyWith(
                    color: isSelected
                        ? ClinicalColors.primaryTeal
                        : ClinicalColors.neutralSlate,
                    fontWeight: isSelected ? FontWeight.w700 : FontWeight.w600,
                  ),
                ),
              ),
            ),
          );
        }).toList(),
      ),
    );
  }
}

/// CustomPainter smooth cubic-bezier Vital Signs chart
class BiometricTrendPainter extends CustomPainter {
  final List<double> points;

  BiometricTrendPainter({required this.points});

  @override
  void paint(Canvas canvas, Size size) {
    if (points.length < 2) return;
    final gridPaint = Paint()
      ..color = ClinicalColors.borderDivider
      ..strokeWidth = 1;

    for (int i = 0; i <= 3; i++) {
      final y = size.height * (i / 3);
      canvas.drawLine(Offset(0, y), Offset(size.width, y), gridPaint);
    }

    final minVal = points.reduce((a, b) => a < b ? a : b) - 4;
    final maxVal = points.reduce((a, b) => a > b ? a : b) + 4;
    final span = (maxVal - minVal).clamp(1.0, 1000.0);
    final stepX = size.width / (points.length - 1);

    final coords = <Offset>[];
    for (int i = 0; i < points.length; i++) {
      final normY = 1.0 - ((points[i] - minVal) / span);
      coords.add(Offset(i * stepX, (normY * (size.height - 16)) + 8));
    }

    final linePath = Path()..moveTo(coords.first.dx, coords.first.dy);
    for (int i = 1; i < coords.length; i++) {
      final prev = coords[i - 1];
      final curr = coords[i];
      final midX = (prev.dx + curr.dx) / 2;
      linePath.cubicTo(midX, prev.dy, midX, curr.dy, curr.dx, curr.dy);
    }

    final fillPath = Path()
      ..addPath(linePath, Offset.zero)
      ..lineTo(coords.last.dx, size.height)
      ..lineTo(coords.first.dx, size.height)
      ..close();

    final fillPaint = Paint()
      ..shader = LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: [
          ClinicalColors.primaryTeal.withValues(alpha: 0.28),
          ClinicalColors.tertiaryEmerald.withValues(alpha: 0.02),
        ],
      ).createShader(Rect.fromLTWH(0, 0, size.width, size.height));

    canvas.drawPath(fillPath, fillPaint);

    final strokePaint = Paint()
      ..color = ClinicalColors.primaryTeal
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3
      ..strokeCap = StrokeCap.round;

    canvas.drawPath(linePath, strokePaint);

    for (int i = 0; i < coords.length; i++) {
      canvas.drawCircle(coords[i], 5, Paint()..color = Colors.white);
      canvas.drawCircle(
        coords[i],
        3.5,
        Paint()
          ..color = i == coords.length - 1
              ? ClinicalColors.tertiaryEmerald
              : ClinicalColors.primaryTeal,
      );
    }
  }

  @override
  bool shouldRepaint(covariant BiometricTrendPainter oldDelegate) =>
      oldDelegate.points != points;
}

class MedicalRecordCardWidget extends StatelessWidget {
  final MedicalRecordModel record;
  final VoidCallback onTap;

  const MedicalRecordCardWidget({
    super.key,
    required this.record,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: ClinicalColors.surfaceWhite,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: ClinicalColors.borderDivider),
          boxShadow: const [
            BoxShadow(
              color: Color(0x0A0F172A),
              blurRadius: 3,
              offset: Offset(0, 1),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        record.category.toUpperCase(),
                        style: ClinicalTypography.labelSm.copyWith(
                          color: ClinicalColors.primaryTeal,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        record.title,
                        style: ClinicalTypography.titleMd,
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 8),
                StatusPillBadge(
                  statusType: record.status,
                  label: record.statusBadgeText,
                ),
              ],
            ),
            const SizedBox(height: 12),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              decoration: BoxDecoration(
                color: ClinicalColors.neutralChipSurface,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                record.keyMetricsSummary,
                style: ClinicalTypography.labelMd,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              record.summary,
              style: ClinicalTypography.bodyMd,
              maxLines: 3,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 12),
            const Divider(height: 1, color: ClinicalColors.metadataRule),
            const SizedBox(height: 10),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Row(
                    children: [
                      const Icon(
                        Icons.person_outline,
                        size: 15,
                        color: ClinicalColors.neutralSlate,
                      ),
                      const SizedBox(width: 6),
                      Expanded(
                        child: Text(
                          record.treatingPhysician,
                          style: ClinicalTypography.bodySm.copyWith(
                            color: ClinicalColors.slateLabelDark,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                ),
                Text(
                  record.dateFormatted,
                  style: ClinicalTypography.labelSm,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
