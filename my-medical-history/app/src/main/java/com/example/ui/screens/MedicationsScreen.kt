package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.MedicationEntity
import com.example.ui.MedicalUiState
import com.example.ui.components.AdherenceCheckbox
import com.example.ui.components.ClinicalInputField
import com.example.ui.components.GhostOutlinedButton
import com.example.ui.components.PrimaryTealButton
import com.example.ui.components.SecondaryOceanicButton
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.BadgeNormalBg
import com.example.ui.theme.BadgeNormalText
import com.example.ui.theme.BadgePendingBg
import com.example.ui.theme.BadgePendingText
import com.example.ui.theme.BorderDividerSlate
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.EmeraldGreenTertiary
import com.example.ui.theme.MetadataRuleDivider
import com.example.ui.theme.MetricNumeralStyle
import com.example.ui.theme.NeutralChipSurface
import com.example.ui.theme.NeutralSlate
import com.example.ui.theme.OnSurfaceDarkNavy
import com.example.ui.theme.SlateLabelDark
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.WarningRefillAmber

@Composable
fun MedicationsScreen(
    uiState: MedicalUiState,
    onToggleAdherence: (MedicationEntity) -> Unit,
    onRequestRefill: (MedicationEntity) -> Unit,
    onRestockMedication: (MedicationEntity) -> Unit,
    onToggleArchive: (MedicationEntity) -> Unit,
    onAddMedication: (String, String, String, String, String, Int, Int, String) -> Unit,
    onNavigateBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBackHome)

    var showAddMedDialog by rememberSaveable { mutableStateOf(false) }

    val activeMedications = remember(uiState.medications) {
        uiState.medications.filter { !it.isArchived }
    }
    val archivedMedications = remember(uiState.medications) {
        uiState.medications.filter { it.isArchived }
    }
    val completedToday = remember(activeMedications) {
        activeMedications.count { it.takenToday }
    }
    val refillDueCount = remember(activeMedications) {
        activeMedications.count { it.pillsRemaining <= 7 }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("medications_lazy_column")
    ) {
        // Screen Header & Add Medication Action
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PHARMACOTHERAPY & ADHERENCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepTealPrimary
                    )
                    Text(
                        text = "Medications",
                        style = MaterialTheme.typography.headlineMedium,
                        color = OnSurfaceDarkNavy
                    )
                }
                PrimaryTealButton(
                    text = "Add Med",
                    icon = Icons.Default.Add,
                    onClick = { showAddMedDialog = true },
                    testTag = "add_medication_button"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Adherence & Refill Telemetry Banner
        item {
            AdherenceTelemetryCard(
                completedCount = completedToday,
                totalActive = activeMedications.size,
                refillDueCount = refillDueCount
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // MACRO GROUP 1: Active Medications
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Active Medications",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurfaceDarkNavy
                )
                StatusPillBadge(
                    statusType = "NORMAL",
                    label = "${activeMedications.size} Active"
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(activeMedications, key = { it.id }) { medication ->
            ActiveMedicationCard(
                medication = medication,
                onToggleAdherence = { onToggleAdherence(medication) },
                onRequestRefill = { onRequestRefill(medication) },
                onRestock = { onRestockMedication(medication) },
                onArchive = { onToggleArchive(medication) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // space-xl (32px / 32.dp) structural canvas separation between macro groups
        // (Active Medications vs. Historical Archive)
        item {
            Spacer(modifier = Modifier.height(16.dp)) // 16.dp + previous 16.dp = 32.dp (space-xl)
            HorizontalDivider(thickness = 1.dp, color = BorderDividerSlate)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "PAST PRESCRIPTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralSlate
                    )
                    Text(
                        text = "Historical Archive",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurfaceDarkNavy
                    )
                }
                StatusPillBadge(
                    statusType = "INFO",
                    label = "${archivedMedications.size} Completed"
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(archivedMedications, key = { it.id }) { archivedMed ->
            ArchivedMedicationCard(
                medication = archivedMed,
                onRestore = { onToggleArchive(archivedMed) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddMedDialog) {
        AddMedicationDialog(
            onDismiss = { showAddMedDialog = false },
            onConfirm = { name, dosage, schedule, purpose, doctor, remaining, total, instructions ->
                onAddMedication(name, dosage, schedule, purpose, doctor, remaining, total, instructions)
                showAddMedDialog = false
            }
        )
    }
}

@Composable
private fun AdherenceTelemetryCard(
    completedCount: Int,
    totalActive: Int,
    refillDueCount: Int
) {
    val ratio = if (totalActive > 0) (completedCount * 100) / totalActive else 100
    val progress = if (totalActive > 0) completedCount.toFloat() / totalActive.toFloat() else 1f

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
                        text = "TODAY'S ADHERENCE SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGreenTertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$ratio%",
                            style = MetricNumeralStyle,
                            color = OnSurfaceDarkNavy
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "($completedCount of $totalActive doses logged)",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeutralSlate,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                if (refillDueCount > 0) {
                    StatusPillBadge(
                        statusType = "REFILL",
                        label = "$refillDueCount Refill Alert"
                    )
                } else {
                    StatusPillBadge(
                        statusType = "ADHERENT",
                        label = "Supply Optimal"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = EmeraldGreenTertiary,
                trackColor = NeutralChipSurface,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
private fun ActiveMedicationCard(
    medication: MedicationEntity,
    onToggleAdherence: () -> Unit,
    onRequestRefill: () -> Unit,
    onRestock: () -> Unit,
    onArchive: () -> Unit
) {
    val isLowSupply = medication.pillsRemaining <= 7
    val supplyProgress = (medication.pillsRemaining.toFloat() / medication.totalPills.coerceAtLeast(1).toFloat())
        .coerceIn(0f, 1f)

    Surface(
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderDividerSlate),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medication_card_${medication.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AdherenceCheckbox(
                        checked = medication.takenToday,
                        onCheckedChange = onToggleAdherence,
                        contentDesc = "Toggle adherence for ${medication.name}",
                        testTag = "med_screen_checkbox_${medication.id}"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = medication.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = OnSurfaceDarkNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = NeutralChipSurface,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = medication.dosage,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeepTealPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = medication.schedule,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralSlate
                        )
                    }
                }

                StatusPillBadge(
                    statusType = when {
                        medication.takenToday -> "ADHERENT"
                        isLowSupply -> "REFILL"
                        else -> "INFO"
                    },
                    label = when {
                        medication.takenToday -> "Taken Today"
                        isLowSupply -> "Refill Due"
                        else -> "Pending Dose"
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Indication: ${medication.purpose}",
                style = MaterialTheme.typography.labelMedium,
                color = SlateLabelDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = medication.instructions,
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralSlate
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Supply Bar & Refill Status
            Surface(
                color = if (isLowSupply) BadgePendingBg.copy(alpha = 0.45f) else NeutralChipSurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Remaining Supply: ${medication.pillsRemaining} of ${medication.totalPills} tablets",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isLowSupply) BadgePendingText else SlateLabelDark
                        )
                        if (medication.refillRequested) {
                            Text(
                                text = "Pharmacy Processing Refill",
                                style = MaterialTheme.typography.labelSmall,
                                color = DeepTealPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { supplyProgress },
                        color = if (isLowSupply) WarningRefillAmber else DeepTealPrimary,
                        trackColor = Color.White,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 1.dp, color = MetadataRuleDivider)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = NeutralSlate,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = medication.prescribingDoctor,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateLabelDark
                    )
                }
                Text(
                    text = "Started ${medication.startDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralSlate
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Secondary Oceanic Button ("Request Prescription Refill") + Archive / Restock
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!medication.refillRequested) {
                    SecondaryOceanicButton(
                        text = "Request Prescription Refill",
                        icon = Icons.Default.LocalPharmacy,
                        onClick = onRequestRefill,
                        modifier = Modifier.weight(1f),
                        testTag = "request_refill_button_${medication.id}"
                    )
                } else {
                    SecondaryOceanicButton(
                        text = "Confirm Pharmacy Restock",
                        icon = Icons.Default.Inventory2,
                        onClick = onRestock,
                        modifier = Modifier.weight(1f),
                        testTag = "restock_button_${medication.id}"
                    )
                }

                GhostOutlinedButton(
                    text = "Archive",
                    icon = Icons.Default.Archive,
                    onClick = onArchive,
                    testTag = "archive_med_button_${medication.id}"
                )
            }
        }
    }
}

@Composable
private fun ArchivedMedicationCard(
    medication: MedicationEntity,
    onRestore: () -> Unit
) {
    Surface(
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderDividerSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = SlateLabelDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = NeutralChipSurface,
                            shape = CircleShape
                        ) {
                            Text(
                                text = medication.dosage,
                                style = MaterialTheme.typography.labelSmall,
                                color = NeutralSlate,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${medication.purpose} • ${medication.startDate} – ${medication.endDate ?: "Completed"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralSlate
                    )
                }
                StatusPillBadge(
                    statusType = "COMPLETED",
                    label = "Completed"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = medication.instructions,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralSlate
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, color = MetadataRuleDivider)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Prescribed by ${medication.prescribingDoctor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralSlate
                )
                GhostOutlinedButton(
                    text = "Restore Active",
                    icon = Icons.Default.Unarchive,
                    onClick = onRestore
                )
            }
        }
    }
}

@Composable
private fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, Int, Int, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var dosage by rememberSaveable { mutableStateOf("") }
    var schedule by rememberSaveable { mutableStateOf("Once daily • Morning") }
    var purpose by rememberSaveable { mutableStateOf("") }
    var doctor by rememberSaveable { mutableStateOf("Dr. Elena Rostova, MD") }
    var pillsRemainingText by rememberSaveable { mutableStateOf("30") }
    var instructions by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "NEW PRESCRIPTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepTealPrimary
                )
                Text(
                    text = "Add Active Medication",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurfaceDarkNavy
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                ClinicalInputField(
                    value = name,
                    onValueChange = { name = it },
                    microLabel = "Medication Name",
                    placeholder = "e.g., Atorvastatin Calcium",
                    testTag = "input_med_name"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClinicalInputField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        microLabel = "Dosage Strength",
                        placeholder = "e.g., 20 mg",
                        modifier = Modifier.weight(1f),
                        testTag = "input_med_dosage"
                    )
                    ClinicalInputField(
                        value = pillsRemainingText,
                        onValueChange = { pillsRemainingText = it },
                        microLabel = "Pills in Bottle",
                        placeholder = "30",
                        modifier = Modifier.weight(1f)
                    )
                }
                ClinicalInputField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    microLabel = "Dosing Frequency & Timing",
                    placeholder = "Once daily • 08:00 AM"
                )
                ClinicalInputField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    microLabel = "Clinical Indication",
                    placeholder = "e.g., Lipid & Cardiovascular Management"
                )
                ClinicalInputField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    microLabel = "Administration Instructions",
                    placeholder = "Take 1 tablet by mouth with water…",
                    singleLine = false
                )
            }
        },
        confirmButton = {
            PrimaryTealButton(
                text = "Save Medication",
                onClick = {
                    val finalName = name.ifBlank { "Atorvastatin" }
                    val finalDosage = dosage.ifBlank { "20 mg" }
                    val finalPurpose = purpose.ifBlank { "Preventive Clinical Maintenance" }
                    val finalInst = instructions.ifBlank { "Take as directed by prescribing physician." }
                    val count = pillsRemainingText.toIntOrNull()?.coerceAtLeast(1) ?: 30
                    onConfirm(
                        finalName,
                        finalDosage,
                        schedule,
                        finalPurpose,
                        doctor,
                        count,
                        count.coerceAtLeast(30),
                        finalInst
                    )
                },
                testTag = "confirm_save_med_button"
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
