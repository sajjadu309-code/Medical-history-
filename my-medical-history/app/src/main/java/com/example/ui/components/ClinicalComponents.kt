package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EmergencyProfileEntity
import com.example.data.local.MedicalRecordEntity
import com.example.ui.VitalTimeframe
import com.example.ui.theme.BadgeCriticalBg
import com.example.ui.theme.BadgeCriticalText
import com.example.ui.theme.BadgeNormalBg
import com.example.ui.theme.BadgeNormalText
import com.example.ui.theme.BadgeOceanicBg
import com.example.ui.theme.BadgeOceanicText
import com.example.ui.theme.BadgePendingBg
import com.example.ui.theme.BadgePendingText
import com.example.ui.theme.BorderDividerSlate
import com.example.ui.theme.CriticalEmergencyRed
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.EmeraldGreenTertiary
import com.example.ui.theme.EmergencyBorderTint
import com.example.ui.theme.EmergencySurfaceTint
import com.example.ui.theme.InputBorderSlate
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.MetadataRuleDivider
import com.example.ui.theme.NeutralChipSurface
import com.example.ui.theme.NeutralSlate
import com.example.ui.theme.OceanicBlueSecondary
import com.example.ui.theme.OnSurfaceDarkNavy
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SlateLabelDark
import com.example.ui.theme.SurfaceContainerLowest

/**
 * Standardized Status Pill Badge:
 * - Normal / Adherent: Background #D1FAE5, text #065F46
 * - Pending / Refill: Background #FEF3C7, text #92400E
 * - Critical / Allergy: Background #FEE2E2, text #991B1B
 * - Oceanic / Info: Background #E0F2FE, text #075985
 */
@Composable
fun StatusPillBadge(
    statusType: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (statusType.uppercase()) {
        "NORMAL", "ADHERENT", "COMPLETED" -> BadgeNormalBg to BadgeNormalText
        "PENDING", "REFILL", "WARNING" -> BadgePendingBg to BadgePendingText
        "CRITICAL", "ALLERGY", "EMERGENCY" -> BadgeCriticalBg to BadgeCriticalText
        else -> BadgeOceanicBg to BadgeOceanicText
    }

    Surface(
        color = bgColor,
        shape = CircleShape,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

/**
 * Filter Chips: 36px height, rounded-full, #F1F5F9 neutral state stepping to #0D9488 with white label when selected.
 * Maintains 48dp minimum touch target height for accessibility.
 */
@Composable
fun ClinicalFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "filter_chip_$label"
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) DeepTealPrimary else NeutralChipSurface,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else SlateLabelDark,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipText"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
            .clip(CircleShape)
            .clickable(role = Role.Tab, onClick = onClick)
    ) {
        Surface(
            color = bgColor,
            shape = CircleShape,
            border = if (selected) null else BorderStroke(1.dp, BorderDividerSlate),
            modifier = Modifier.height(36.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor
                )
            }
        }
    }
}

/**
 * Adherence Checkbox:
 * 24x24dp visual box inside 48x48dp accessible touch target, 6px radius.
 * Unchecked: #E2E8F0 border. Checked: #10B981 fill with crisp white checkmark icon.
 */
@Composable
fun AdherenceCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    contentDesc: String,
    modifier: Modifier = Modifier,
    testTag: String = "adherence_checkbox"
) {
    val bgColor by animateColorAsState(
        targetValue = if (checked) EmeraldGreenTertiary else Color.White,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "checkboxBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) EmeraldGreenTertiary else BorderDividerSlate,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "checkboxBorder"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .testTag(testTag)
            .clip(CircleShape)
            .clickable(
                role = Role.Checkbox,
                onClick = onCheckedChange
            )
            .semantics { contentDescription = contentDesc }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .border(1.5.dp, borderColor, RoundedCornerShape(6.dp))
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Segmented Timeframe Selector (7D, 1M, 6M, 1Y):
 * Contained pill segment container (#F1F5F9) with sliding #FFFFFF elevation pill indicator.
 */
@Composable
fun SegmentedTimeframeSelector(
    selected: VitalTimeframe,
    onSelect: (VitalTimeframe) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NeutralChipSurface,
        shape = CircleShape,
        border = BorderStroke(1.dp, BorderDividerSlate),
        modifier = modifier.height(44.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            VitalTimeframe.entries.forEach { timeframe ->
                val isSelected = timeframe == selected
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("timeframe_${timeframe.label}")
                        .then(
                            if (isSelected) {
                                Modifier
                                    .shadow(2.dp, CircleShape, ambientColor = Color(0x140F172A))
                                    .clip(CircleShape)
                                    .background(Color.White)
                            } else {
                                Modifier.clip(CircleShape)
                            }
                        )
                        .clickable(role = Role.Tab) { onSelect(timeframe) }
                ) {
                    Text(
                        text = timeframe.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) DeepTealPrimary else NeutralSlate,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Standardized Buttons:
 * - Primary: Teal fill (#0D9488), white label, 12px radius, 52px mobile height.
 * - Secondary Oceanic: Oceanic blue fill (#0284C7), white label, for "Request Prescription Refill" or "Share PDF Record".
 * - Emergency Action: Red fill (#EF4444), white label, bold 16px typography, guaranteed 56px minimum height.
 * - Ghost / Outlined: Transparent background, 1.5px border (#E2E8F0), slate label (#334155).
 */
@Composable
fun PrimaryTealButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "primary_teal_button"
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DeepTealPrimary,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .testTag(testTag)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
            color = Color.White
        )
    }
}

@Composable
fun SecondaryOceanicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "secondary_oceanic_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = OceanicBlueSecondary,
            contentColor = Color.White,
            disabledContainerColor = NeutralChipSurface,
            disabledContentColor = NeutralSlate
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) Color.White else NeutralSlate
        )
    }
}

@Composable
fun EmergencyRedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.MedicalServices,
    testTag: String = "emergency_action_button"
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CriticalEmergencyRed,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontFamily = PlusJakartaSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}

@Composable
fun GhostOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "ghost_outlined_button"
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, BorderDividerSlate),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = SlateLabelDark
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SlateLabelDark,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = SlateLabelDark
        )
    }
}

/**
 * Input Fields & Selectors:
 * - Height & Bounds: 52px height, 8px radius, #FFFFFF surface fill, and 1.5px border (#CBD5E1).
 * - Focus State: 2px border #0D9488 with a soft 3px outer glow ring (rgba(13, 148, 136, 0.15)).
 * - Micro-labels: Persistent floating labels in label-sm (#64748B).
 */
@Composable
fun ClinicalInputField(
    value: String,
    onValueChange: (String) -> Unit,
    microLabel: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    testTag: String = "clinical_input_$microLabel"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val outerGlowColor = if (isFocused) Color(0x260D9488) else Color.Transparent
    val borderColor = if (isFocused) DeepTealPrimary else InputBorderSlate
    val borderWidth = if (isFocused) 2.dp else 1.5.dp

    Column(modifier = modifier) {
        Text(
            text = microLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isFocused) DeepTealPrimary else NeutralSlate,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, outerGlowColor, RoundedCornerShape(11.dp))
                .padding(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isFocused) DeepTealPrimary else NeutralSlate,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeutralSlate.copy(alpha = 0.75f)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = singleLine,
                        interactionSource = interactionSource,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = OnSurfaceDarkNavy,
                            fontFamily = InterFontFamily
                        ),
                        cursorBrush = SolidColor(DeepTealPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(testTag)
                    )
                }
            }
        }
    }
}

/**
 * Medical Record Cards:
 * - Surface: #FFFFFF, 16px radius, enclosed by 1px solid #E2E8F0.
 * - Padding: 16px uniform internal padding.
 * - Composition: Top-aligned title with right-aligned status pill badge;
 *   center-aligned diagnostic details;
 *   bottom metadata row (timestamp, treating physician) separated by a 1px #F1F5F9 rule.
 */
@Composable
fun MedicalRecordCard(
    record: MedicalRecordEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderDividerSlate),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("medical_record_card_${record.id}")
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top Row: Category tag + Status Pill Badge
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = record.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepTealPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceDarkNavy,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusPillBadge(
                    statusType = record.status,
                    label = record.statusBadgeText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Center-aligned diagnostic details & key metrics pill box
            Surface(
                color = NeutralChipSurface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = record.keyMetricsSummary,
                    style = MaterialTheme.typography.labelMedium,
                    color = SlateLabelDark,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = record.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralSlate,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1px #F1F5F9 rule
            HorizontalDivider(
                thickness = 1.dp,
                color = MetadataRuleDivider
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom metadata row (timestamp, treating physician, attachment)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = NeutralSlate,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = record.treatingPhysician,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateLabelDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = NeutralSlate,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = record.dateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralSlate
                    )
                }
            }

            if (record.attachmentFileName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = OceanicBlueSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = record.attachmentFileName,
                        style = MaterialTheme.typography.labelSmall,
                        color = OceanicBlueSecondary
                    )
                }
            }
        }
    }
}

/**
 * Emergency Accentuation Card:
 * Specialized border treatment (#FECACA) combined with a faint red-tinted ambient glow
 * (0px 4px 12px rgba(239, 68, 68, 0.08)), signaling urgent status without visual noise.
 */
@Composable
fun EmergencyMedicalRecordBanner(
    profile: EmergencyProfileEntity,
    onOpenEmergencyModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allergies = remember(profile.severeAllergies) {
        profile.severeAllergies.split("|").filter { it.isNotBlank() }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = CriticalEmergencyRed.copy(alpha = 0.25f),
                spotColor = CriticalEmergencyRed.copy(alpha = 0.20f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.5.dp, EmergencyBorderTint, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenEmergencyModal)
            .testTag("emergency_medical_record_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BadgeCriticalBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Critical Allergy Alert",
                            tint = CriticalEmergencyRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "EMERGENCY MEDICAL RECORD",
                            style = MaterialTheme.typography.labelSmall,
                            color = BadgeCriticalText
                        )
                        Text(
                            text = "Blood Type ${profile.bloodType} • Severe Allergies",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceDarkNavy
                        )
                    }
                }
                StatusPillBadge(
                    statusType = "CRITICAL",
                    label = "${allergies.size} Critical"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = EmergencySurfaceTint,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, EmergencyBorderTint.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    allergies.take(2).forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = BadgeCriticalText,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    if (profile.criticalDirectiveNote.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = profile.criticalDirectiveNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateLabelDark
                        )
                    }
                }
            }
        }
    }
}
