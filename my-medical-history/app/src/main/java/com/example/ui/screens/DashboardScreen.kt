package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.MedicationEntity
import com.example.data.local.VitalSignEntity
import com.example.ui.AppDestination
import com.example.ui.MedicalUiState
import com.example.ui.VitalTimeframe
import com.example.ui.components.AdherenceCheckbox
import com.example.ui.components.ClinicalFilterChip
import com.example.ui.components.ClinicalInputField
import com.example.ui.components.EmergencyMedicalRecordBanner
import com.example.ui.components.GhostOutlinedButton
import com.example.ui.components.PrimaryTealButton
import com.example.ui.components.SecondaryOceanicButton
import com.example.ui.components.SegmentedTimeframeSelector
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.BadgeNormalBg
import com.example.ui.theme.BadgeNormalText
import com.example.ui.theme.BadgeOceanicBg
import com.example.ui.theme.BadgeOceanicText
import com.example.ui.theme.BorderDividerSlate
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.EmeraldGreenTertiary
import com.example.ui.theme.MetadataRuleDivider
import com.example.ui.theme.MetricNumeralStyle
import com.example.ui.theme.NeutralChipSurface
import com.example.ui.theme.NeutralSlate
import com.example.ui.theme.OceanicBlueSecondary
import com.example.ui.theme.OnSurfaceDarkNavy
import com.example.ui.theme.SlateLabelDark
import com.example.ui.theme.SurfaceContainerLowest

@Composable
fun DashboardScreen(
    uiState: MedicalUiState,
    onSelectTimeframe: (VitalTimeframe) -> Unit,
    onSelectVitalIndex: (Int) -> Unit,
    onLogVitalReading: (VitalSignEntity, String, Float, Float?, String, String) -> Unit,
    onToggleMedication: (MedicationEntity) -> Unit,
    onRequestRefill: (MedicationEntity) -> Unit,
    onNavigate: (AppDestination) -> Unit,
    onOpenEmergencyModal: () -> Unit,
    onSharePdf: () -> Unit,
    onSaveDriveBackupUri: (android.net.Uri) -> Unit,
    onRestoreDriveBackupUri: (android.net.Uri) -> Unit,
    onShareDriveBackup: () -> Unit,
    onSaveFlutterZipUri: (android.net.Uri) -> Unit,
    onShareFlutterZip: () -> Unit,
    onAddAppointment: (String, String, String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var vitalToLog by remember { mutableStateOf<VitalSignEntity?>(null) }
    var showAddAppointmentDialog by rememberSaveable { mutableStateOf(false) }

    val activeMeds = remember(uiState.medications) {
        uiState.medications.filter { !it.isArchived }
    }
    val completedMedsCount = remember(activeMeds) {
        activeMeds.count { it.takenToday }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp), // space-lg (24px) card-to-card grouping
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_lazy_column")
    ) {
        // 1. Patient Clinical Header & Quick Actions
        item {
            PatientClinicalHeaderCard(
                uiState = uiState,
                onSharePdf = onSharePdf,
                onOpenEmergencyModal = onOpenEmergencyModal
            )
        }

        // 2. Offline Storage, Google Drive Backup/Restore & Flutter Project ZIP Hub
        item {
            com.example.ui.components.DriveAndFlutterHubCard(
                uiState = uiState,
                onSaveDriveBackupUri = onSaveDriveBackupUri,
                onRestoreDriveBackupUri = onRestoreDriveBackupUri,
                onShareDriveBackup = onShareDriveBackup,
                onSaveFlutterZipUri = onSaveFlutterZipUri,
                onShareFlutterZip = onShareFlutterZip
            )
        }

        // 3. Emergency Medical Record Banner (Specialized #FECACA border + faint red glow)
        item {
            EmergencyMedicalRecordBanner(
                profile = uiState.emergencyProfile,
                onOpenEmergencyModal = onOpenEmergencyModal
            )
        }

        // 3. Vital Signs & Biometrics with Segmented Timeframe Selector (7D, 1M, 6M, 1Y)
        item {
            VitalSignsBiometricSection(
                vitals = uiState.vitals,
                selectedVitalIndex = uiState.selectedVitalIndex,
                selectedTimeframe = uiState.selectedTimeframe,
                onSelectTimeframe = onSelectTimeframe,
                onSelectVitalIndex = onSelectVitalIndex,
                onLogVitalClick = { vitalToLog = it }
            )
        }

        // 4. Daily Medication Adherence Snapshot
        item {
            DailyAdherenceOverviewSection(
                activeMeds = activeMeds,
                completedCount = completedMedsCount,
                onToggleMedication = onToggleMedication,
                onRequestRefill = onRequestRefill,
                onViewAllMedications = { onNavigate(AppDestination.MEDICATIONS) }
            )
        }

        // 5. Upcoming Consultations & Portal Sync
        item {
            UpcomingAppointmentsSection(
                appointments = uiState.appointments,
                onScheduleClick = { showAddAppointmentDialog = true },
                onViewRecordsClick = { onNavigate(AppDestination.RECORDS) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (vitalToLog != null) {
        LogVitalSignDialog(
            vital = vitalToLog!!,
            onDismiss = { vitalToLog = null },
            onConfirm = { valueStr, primaryVal, secondaryVal, status, note ->
                onLogVitalReading(vitalToLog!!, valueStr, primaryVal, secondaryVal, status, note)
                vitalToLog = null
            }
        )
    }

    if (showAddAppointmentDialog) {
        ScheduleAppointmentDialog(
            onDismiss = { showAddAppointmentDialog = false },
            onConfirm = { doc, spec, fac, date, time, reason, prep ->
                onAddAppointment(doc, spec, fac, date, time, reason, prep)
                showAddAppointmentDialog = false
            }
        )
    }
}

@Composable
private fun PatientClinicalHeaderCard(
    uiState: MedicalUiState,
    onSharePdf: () -> Unit,
    onOpenEmergencyModal: () -> Unit
) {
    Surface(
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderDividerSlate),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Subtle Hero Banner Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_clinical_hero_1791480882987),
                    contentDescription = "Clinical Wellness Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    OnSurfaceDarkNavy.copy(alpha = 0.78f),
                                    DeepTealPrimary.copy(alpha = 0.55f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF6FFBBE),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VERIFIED CLINICAL RECORD",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF89F5E7)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "My Medical History",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Text(
                            text = "ID: ${uiState.emergencyProfile.policyMemberId} • Synced Today",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Patient Info & Quick Action Row
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_patient_avatar_1791480866939),
                        contentDescription = "Patient Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, DeepTealPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.emergencyProfile.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurfaceDarkNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "DOB: ${uiState.emergencyProfile.dateOfBirth} (${uiState.emergencyProfile.age} yrs) • ${uiState.emergencyProfile.weightHeight}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralSlate
                        )
                    }
                    Surface(
                        color = BadgeOceanicBg,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Blood ${uiState.emergencyProfile.bloodType}",
                            style = MaterialTheme.typography.labelMedium,
                            color = BadgeOceanicText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SecondaryOceanicButton(
                        text = "Share PDF Record",
                        icon = Icons.Default.PictureAsPdf,
                        onClick = onSharePdf,
                        modifier = Modifier.weight(1f),
                        testTag = "dashboard_share_pdf_button"
                    )
                    GhostOutlinedButton(
                        text = "Emergency ID",
                        icon = Icons.Default.Favorite,
                        onClick = onOpenEmergencyModal,
                        testTag = "dashboard_emergency_id_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun VitalSignsBiometricSection(
    vitals: List<VitalSignEntity>,
    selectedVitalIndex: Int,
    selectedTimeframe: VitalTimeframe,
    onSelectTimeframe: (VitalTimeframe) -> Unit,
    onSelectVitalIndex: (Int) -> Unit,
    onLogVitalClick: (VitalSignEntity) -> Unit
) {
    val activeVital = vitals.getOrNull(selectedVitalIndex) ?: vitals.firstOrNull()

    Surface(
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderDividerSlate),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "BIOMETRIC TELEMETRY",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepTealPrimary
                    )
                    Text(
                        text = "Vital Signs & Trends",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurfaceDarkNavy
                    )
                }
                if (activeVital != null) {
                    GhostOutlinedButton(
                        text = "Log Vital",
                        icon = Icons.Default.Add,
                        onClick = { onLogVitalClick(activeVital) },
                        testTag = "log_vital_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented Timeframe Selector (7D, 1M, 6M, 1Y)
            SegmentedTimeframeSelector(
                selected = selectedTimeframe,
                onSelect = onSelectTimeframe,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2x2 Grid of Metric Numeral Cards
            val pairs = vitals.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                pairs.forEachIndexed { rowIdx, rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEachIndexed { colIdx, vital ->
                            val globalIdx = rowIdx * 2 + colIdx
                            val isSelected = globalIdx == selectedVitalIndex
                            VitalMetricCard(
                                vital = vital,
                                isSelected = isSelected,
                                onClick = { onSelectVitalIndex(globalIdx) },
                                onQuickUpdate = { onLogVitalClick(vital) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (activeVital != null) {
                Spacer(modifier = Modifier.height(16.dp))
                BiometricTrendChartCard(
                    vital = activeVital,
                    timeframe = selectedTimeframe
                )
            }
        }
    }
}

@Composable
private fun VitalMetricCard(
    vital: VitalSignEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onQuickUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) Color(0xFFF0FDFA) else SurfaceContainerLowest,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) DeepTealPrimary else BorderDividerSlate
        ),
        modifier = modifier
            .testTag("vital_card_${vital.type}")
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = vital.type,
                    style = MaterialTheme.typography.labelMedium,
                    color = SlateLabelDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusPillBadge(
                    statusType = vital.status,
                    label = if (vital.status == "NORMAL") "Stable" else vital.status
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metric Numeral (28px Plus Jakarta Sans Bold) paired with label-sm unit descriptor
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = vital.valueDisplay,
                    style = MetricNumeralStyle,
                    color = OnSurfaceDarkNavy
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = vital.unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralSlate,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = vital.referenceRange,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = vital.recordedAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepTealPrimary
                )
                Text(
                    text = "Update",
                    style = MaterialTheme.typography.labelSmall,
                    color = OceanicBlueSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onQuickUpdate)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun BiometricTrendChartCard(
    vital: VitalSignEntity,
    timeframe: VitalTimeframe
) {
    val rawCsv = when (timeframe) {
        VitalTimeframe.DAYS_7 -> vital.trend7dCsv
        VitalTimeframe.MONTH_1 -> vital.trend1mCsv
        VitalTimeframe.MONTHS_6 -> vital.trend6mCsv
        VitalTimeframe.YEAR_1 -> vital.trend1yCsv
    }
    val points = remember(rawCsv) {
        rawCsv.split(",").mapNotNull { it.trim().toFloatOrNull() }.ifEmpty { listOf(118f, 118f) }
    }

    Surface(
        color = NeutralChipSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderDividerSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonitorHeart,
                        contentDescription = null,
                        tint = DeepTealPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${vital.type} Trend (${timeframe.label})",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurfaceDarkNavy
                    )
                }
                Text(
                    text = "Range: ${points.minOrNull()?.toInt()}–${points.maxOrNull()?.toInt()} ${vital.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralSlate
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val width = size.width
                val height = size.height
                val minVal = (points.minOrNull() ?: 0f) - 4f
                val maxVal = (points.maxOrNull() ?: 100f) + 4f
                val span = (maxVal - minVal).coerceAtLeast(1f)

                // Horizontal reference grid lines
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = height * (i.toFloat() / gridLines)
                    drawLine(
                        color = BorderDividerSlate,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (points.size >= 2) {
                    val stepX = width / (points.size - 1)
                    val coords = points.mapIndexed { idx, v ->
                        val normalizedY = 1f - ((v - minVal) / span)
                        Offset(idx * stepX, (normalizedY * (height - 16.dp.toPx())) + 8.dp.toPx())
                    }

                    val linePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 1 until coords.size) {
                            val prev = coords[i - 1]
                            val curr = coords[i]
                            val midX = (prev.x + curr.x) / 2f
                            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(coords.last().x, height)
                        lineTo(coords.first().x, height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                DeepTealPrimary.copy(alpha = 0.28f),
                                EmeraldGreenTertiary.copy(alpha = 0.03f)
                            )
                        )
                    )

                    drawPath(
                        path = linePath,
                        color = DeepTealPrimary,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    coords.forEachIndexed { index, pt ->
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = if (index == coords.lastIndex) EmeraldGreenTertiary else DeepTealPrimary,
                            radius = 3.5.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = vital.physicianNote,
                style = MaterialTheme.typography.bodySmall,
                color = SlateLabelDark
            )
        }
    }
}

@Composable
private fun DailyAdherenceOverviewSection(
    activeMeds: List<MedicationEntity>,
    completedCount: Int,
    onToggleMedication: (MedicationEntity) -> Unit,
    onRequestRefill: (MedicationEntity) -> Unit,
    onViewAllMedications: () -> Unit
) {
    val progress = if (activeMeds.isNotEmpty()) {
        completedCount.toFloat() / activeMeds.size.toFloat()
    } else 1f

    Surface(
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderDividerSlate),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DAILY REGIMEN",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGreenTertiary
                    )
                    Text(
                        text = "Medication Adherence",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurfaceDarkNavy
                    )
                }
                StatusPillBadge(
                    statusType = if (completedCount == activeMeds.size) "ADHERENT" else "PENDING",
                    label = "$completedCount of ${activeMeds.size} Taken"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = EmeraldGreenTertiary,
                trackColor = NeutralChipSurface,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                activeMeds.forEach { med ->
                    val isLowSupply = med.pillsRemaining <= 7
                    Surface(
                        color = if (med.takenToday) Color(0xFFF0FDF4) else SurfaceContainerLowest,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (med.takenToday) BadgeNormalBg else BorderDividerSlate
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AdherenceCheckbox(
                                    checked = med.takenToday,
                                    onCheckedChange = { onToggleMedication(med) },
                                    contentDesc = "Mark ${med.name} as taken",
                                    testTag = "dashboard_med_checkbox_${med.id}"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = med.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = OnSurfaceDarkNavy
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = NeutralChipSurface,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = med.dosage,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SlateLabelDark,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = med.schedule,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeutralSlate
                                    )
                                }
                                if (isLowSupply) {
                                    StatusPillBadge(
                                        statusType = "REFILL",
                                        label = "${med.pillsRemaining} left"
                                    )
                                } else {
                                    StatusPillBadge(
                                        statusType = if (med.takenToday) "ADHERENT" else "INFO",
                                        label = if (med.takenToday) "Adherent" else "Scheduled"
                                    )
                                }
                            }

                            if (isLowSupply && !med.refillRequested) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 52.dp)
                                ) {
                                    Text(
                                        text = "Refill alert: only ${med.pillsRemaining} doses remaining",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = { onRequestRefill(med) },
                                        modifier = Modifier.testTag("quick_refill_${med.id}")
                                    ) {
                                        Text(
                                            text = "Request Refill",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = OceanicBlueSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GhostOutlinedButton(
                text = "Manage All Medications & Refills",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = onViewAllMedications,
                modifier = Modifier.fillMaxWidth(),
                testTag = "view_all_medications_button"
            )
        }
    }
}

@Composable
private fun UpcomingAppointmentsSection(
    appointments: List<com.example.data.local.AppointmentEntity>,
    onScheduleClick: () -> Unit,
    onViewRecordsClick: () -> Unit
) {
    Surface(
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderDividerSlate),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "CARE COORDINATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = OceanicBlueSecondary
                    )
                    Text(
                        text = "Upcoming Consultations",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurfaceDarkNavy
                    )
                }
                GhostOutlinedButton(
                    text = "Schedule",
                    icon = Icons.Default.CalendarMonth,
                    onClick = onScheduleClick,
                    testTag = "schedule_visit_button"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            appointments.forEachIndexed { idx, appt ->
                if (idx > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Surface(
                    color = NeutralChipSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderDividerSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appt.doctorName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OnSurfaceDarkNavy
                                )
                                Text(
                                    text = "${appt.specialty} • ${appt.facility}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralSlate
                                )
                            }
                            StatusPillBadge(
                                statusType = appt.status,
                                label = appt.statusLabel
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${appt.dateFormatted} at ${appt.timeFormatted} — ${appt.visitReason}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SlateLabelDark
                        )

                        if (appt.preparationInstructions.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(thickness = 1.dp, color = MetadataRuleDivider)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Prep: ${appt.preparationInstructions}",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralSlate
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryTealButton(
                text = "Browse All Clinical Lab & Diagnostic Records",
                onClick = onViewRecordsClick,
                modifier = Modifier.fillMaxWidth(),
                testTag = "browse_all_records_button"
            )
        }
    }
}

@Composable
private fun LogVitalSignDialog(
    vital: VitalSignEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, Float, Float?, String, String) -> Unit
) {
    var valueText by rememberSaveable { mutableStateOf(vital.valueDisplay) }
    var noteText by rememberSaveable { mutableStateOf("") }
    var selectedStatus by rememberSaveable { mutableStateOf("NORMAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "LOG BIOMETRIC MEASUREMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepTealPrimary
                )
                Text(
                    text = "Update ${vital.type}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurfaceDarkNavy
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ClinicalInputField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    microLabel = "${vital.type} (${vital.unit})",
                    placeholder = "e.g., ${vital.valueDisplay}",
                    testTag = "input_vital_value"
                )
                Text(
                    text = "CLINICAL STATUS ASSESSMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralSlate
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("NORMAL" to "Normal", "PENDING" to "Borderline", "CRITICAL" to "High Alert").forEach { (code, label) ->
                        ClinicalFilterChip(
                            label = label,
                            selected = selectedStatus == code,
                            onClick = { selectedStatus = code }
                        )
                    }
                }
                ClinicalInputField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    microLabel = "Clinical Context / Measurement Note",
                    placeholder = "e.g., Resting morning measurement after hydration",
                    singleLine = false,
                    testTag = "input_vital_note"
                )
            }
        },
        confirmButton = {
            PrimaryTealButton(
                text = "Save Reading",
                onClick = {
                    val parts = valueText.split("/")
                    val primary = parts.firstOrNull()?.trim()?.toFloatOrNull() ?: vital.numericPrimary
                    val secondary = parts.getOrNull(1)?.trim()?.toFloatOrNull()
                    if (valueText.isNotBlank()) {
                        onConfirm(valueText, primary, secondary, selectedStatus, noteText)
                    }
                },
                testTag = "confirm_save_vital_button"
            )
        },
        dismissButton = {
            GhostOutlinedButton(
                text = "Cancel",
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun ScheduleAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String) -> Unit
) {
    var doctorName by rememberSaveable { mutableStateOf("") }
    var specialty by rememberSaveable { mutableStateOf("Internal Medicine") }
    var facility by rememberSaveable { mutableStateOf("Pacific Coastal Medical Center") }
    var dateFormatted by rememberSaveable { mutableStateOf("Nov 18, 2026") }
    var timeFormatted by rememberSaveable { mutableStateOf("09:30 AM") }
    var visitReason by rememberSaveable { mutableStateOf("") }
    var prepNotes by rememberSaveable { mutableStateOf("Bring updated medication list and lab summary.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "NEW CONSULTATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepTealPrimary
                )
                Text(
                    text = "Schedule Clinical Visit",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurfaceDarkNavy
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ClinicalInputField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    microLabel = "Treating Physician",
                    placeholder = "e.g., Dr. Marcus Vance, MD",
                    testTag = "appt_doctor_input"
                )
                ClinicalInputField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    microLabel = "Clinical Specialty",
                    placeholder = "e.g., Cardiology"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClinicalInputField(
                        value = dateFormatted,
                        onValueChange = { dateFormatted = it },
                        microLabel = "Date",
                        placeholder = "Nov 18, 2026",
                        modifier = Modifier.weight(1f)
                    )
                    ClinicalInputField(
                        value = timeFormatted,
                        onValueChange = { timeFormatted = it },
                        microLabel = "Time",
                        placeholder = "09:30 AM",
                        modifier = Modifier.weight(1f)
                    )
                }
                ClinicalInputField(
                    value = visitReason,
                    onValueChange = { visitReason = it },
                    microLabel = "Consultation Reason",
                    placeholder = "e.g., Annual diagnostic wellness review",
                    testTag = "appt_reason_input"
                )
            }
        },
        confirmButton = {
            PrimaryTealButton(
                text = "Confirm Visit",
                onClick = {
                    val doc = doctorName.ifBlank { "Dr. Elena Rostova, MD" }
                    val reason = visitReason.ifBlank { "Follow-up Clinical Consultation" }
                    onConfirm(doc, specialty, facility, dateFormatted, timeFormatted, reason, prepNotes)
                },
                testTag = "confirm_appointment_button"
            )
        },
        dismissButton = {
            GhostOutlinedButton(
                text = "Cancel",
                onClick = onDismiss
            )
        }
    )
}
