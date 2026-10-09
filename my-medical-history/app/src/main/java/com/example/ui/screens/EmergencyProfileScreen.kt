package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.EmergencyProfileEntity
import com.example.ui.MedicalUiState
import com.example.ui.components.ClinicalInputField
import com.example.ui.components.EmergencyRedButton
import com.example.ui.components.GhostOutlinedButton
import com.example.ui.components.PrimaryTealButton
import com.example.ui.components.SecondaryOceanicButton
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.BadgeCriticalBg
import com.example.ui.theme.BadgeCriticalText
import com.example.ui.theme.BadgeNormalBg
import com.example.ui.theme.BadgeNormalText
import com.example.ui.theme.BorderDividerSlate
import com.example.ui.theme.CriticalEmergencyRed
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.EmergencyBorderTint
import com.example.ui.theme.EmergencySurfaceTint
import com.example.ui.theme.MetadataRuleDivider
import com.example.ui.theme.MetricNumeralStyle
import com.example.ui.theme.NeutralChipSurface
import com.example.ui.theme.NeutralSlate
import com.example.ui.theme.OceanicBlueSecondary
import com.example.ui.theme.OnSurfaceDarkNavy
import com.example.ui.theme.SlateLabelDark
import com.example.ui.theme.SurfaceContainerLowest

@Composable
fun EmergencyProfileScreen(
    uiState: MedicalUiState,
    onUpdateEmergencyProfile: (EmergencyProfileEntity) -> Unit,
    onOpenEmergencyResponderModal: () -> Unit,
    onSharePdf: () -> Unit,
    onNavigateBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBackHome)

    val context = LocalContext.current
    val profile = uiState.emergencyProfile
    var showEditProfileDialog by rememberSaveable { mutableStateOf(false) }

    val allergies = remember(profile.severeAllergies) {
        profile.severeAllergies.split("|").filter { it.isNotBlank() }
    }
    val conditions = remember(profile.chronicConditions) {
        profile.chronicConditions.split("|").filter { it.isNotBlank() }
    }
    val surgeries = remember(profile.surgicalHistory) {
        profile.surgicalHistory.split("|").filter { it.isNotBlank() }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("emergency_profile_lazy_column")
    ) {
        // Persistent Emergency Action Banner & Header
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACUTE CARE DISCLOSURE",
                            style = MaterialTheme.typography.labelSmall,
                            color = CriticalEmergencyRed
                        )
                        Text(
                            text = "Emergency Medical ID",
                            style = MaterialTheme.typography.headlineMedium,
                            color = OnSurfaceDarkNavy
                        )
                    }
                    GhostOutlinedButton(
                        text = "Edit ID",
                        icon = Icons.Default.Edit,
                        onClick = { showEditProfileDialog = true },
                        testTag = "edit_emergency_id_button"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Guaranteed 56px minimum height Emergency Action Button (#EF4444)
                EmergencyRedButton(
                    text = "Launch Paramedic / ER Responder Card",
                    icon = Icons.Default.MedicalServices,
                    onClick = onOpenEmergencyResponderModal,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "launch_responder_card_button"
                )
            }
        }

        // Patient Biometric Identity & Blood Type Card
        item {
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_patient_avatar_1791480866939),
                            contentDescription = "Patient Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.dp, DeepTealPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.fullName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = OnSurfaceDarkNavy
                            )
                            Text(
                                text = "DOB: ${profile.dateOfBirth} • Age ${profile.age}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeutralSlate
                            )
                            Text(
                                text = profile.weightHeight,
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateLabelDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 1.dp, color = MetadataRuleDivider)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = EmergencySurfaceTint,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, EmergencyBorderTint),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "BLOOD TYPE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BadgeCriticalText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = profile.bloodType,
                                    style = MetricNumeralStyle,
                                    color = BadgeCriticalText
                                )
                            }
                        }

                        Surface(
                            color = BadgeNormalBg.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BadgeNormalBg),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "ORGAN DONOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BadgeNormalText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (profile.organDonor) "Registered" else "No",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = BadgeNormalText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Specialized Emergency Accentuation Card (#FECACA border + faint red glow)
        item {
            Box(
                modifier = Modifier
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
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = CriticalEmergencyRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Severe Allergies & Hypersensitivities",
                                style = MaterialTheme.typography.titleMedium,
                                color = OnSurfaceDarkNavy
                            )
                        }
                        StatusPillBadge(
                            statusType = "CRITICAL",
                            label = "Life-Critical"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    allergies.forEach { allergy ->
                        Surface(
                            color = EmergencySurfaceTint,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, EmergencyBorderTint),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "• $allergy",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = BadgeCriticalText,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ACUTE INTERVENTION DIRECTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralSlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile.criticalDirectiveNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateLabelDark
                    )
                }
            }
        }

        // Chronic Conditions & Surgical History Card
        item {
            Surface(
                color = SurfaceContainerLowest,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderDividerSlate),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = DeepTealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chronic Conditions & Surgical History",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceDarkNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ONGOING CONDITIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepTealPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    conditions.forEach { cond ->
                        Text(
                            text = "• $cond",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateLabelDark,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 1.dp, color = MetadataRuleDivider)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "PRIOR SURGICAL PROCEDURES",
                        style = MaterialTheme.typography.labelSmall,
                        color = OceanicBlueSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    surgeries.forEach { surg ->
                        Text(
                            text = "• $surg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateLabelDark,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Emergency Contacts & Primary Care Physician with Dial Action
        item {
            Surface(
                color = SurfaceContainerLowest,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderDividerSlate),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalInformation,
                            contentDescription = null,
                            tint = OceanicBlueSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Emergency Contact & Care Team",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceDarkNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Emergency Contact Box
                    Surface(
                        color = NeutralChipSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PRIMARY EMERGENCY PROXY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CriticalEmergencyRed
                                )
                                Text(
                                    text = profile.emergencyContactName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OnSurfaceDarkNavy
                                )
                                Text(
                                    text = "${profile.emergencyContactRelation} • ${profile.emergencyContactPhone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralSlate
                                )
                            }
                            GhostOutlinedButton(
                                text = "Call",
                                icon = Icons.Default.Call,
                                onClick = { dialPhoneNumber(context, profile.emergencyContactPhone) },
                                testTag = "call_emergency_contact_button"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Physician Box
                    Surface(
                        color = NeutralChipSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ATTENDING PHYSICIAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeepTealPrimary
                                )
                                Text(
                                    text = profile.primaryPhysicianName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OnSurfaceDarkNavy
                                )
                                Text(
                                    text = "${profile.primaryPhysicianClinic}\n${profile.primaryPhysicianPhone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralSlate
                                )
                            }
                            GhostOutlinedButton(
                                text = "Call",
                                icon = Icons.Default.Call,
                                onClick = { dialPhoneNumber(context, profile.primaryPhysicianPhone) },
                                testTag = "call_physician_button"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SecondaryOceanicButton(
                        text = "Export & Share Full PDF Medical Record",
                        icon = Icons.Default.PictureAsPdf,
                        onClick = onSharePdf,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "emergency_share_pdf_button"
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showEditProfileDialog) {
        EditEmergencyProfileDialog(
            profile = profile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updated ->
                onUpdateEmergencyProfile(updated)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
fun EmergencyResponderModalDialog(
    profile: EmergencyProfileEntity,
    onDismiss: () -> Unit,
    onSharePdf: () -> Unit
) {
    val context = LocalContext.current
    val allergies = remember(profile.severeAllergies) {
        profile.severeAllergies.split("|").filter { it.isNotBlank() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CriticalEmergencyRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PARAMEDIC / ER TRIAGE VIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = CriticalEmergencyRed
                    )
                    Text(
                        text = profile.fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurfaceDarkNavy
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPillBadge(statusType = "CRITICAL", label = "Blood Type ${profile.bloodType}")
                    StatusPillBadge(statusType = "NORMAL", label = "DOB: ${profile.dateOfBirth}")
                }

                Surface(
                    color = EmergencySurfaceTint,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, EmergencyBorderTint),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "LIFE-CRITICAL ALLERGIES",
                            style = MaterialTheme.typography.labelSmall,
                            color = BadgeCriticalText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        allergies.forEach { item ->
                            Text(
                                text = "• $item",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = BadgeCriticalText,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "CRITICAL DIRECTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralSlate
                )
                Text(
                    text = profile.criticalDirectiveNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceDarkNavy
                )

                HorizontalDivider(thickness = 1.dp, color = BorderDividerSlate)

                EmergencyRedButton(
                    text = "Call Proxy: ${profile.emergencyContactName}",
                    icon = Icons.Default.Call,
                    onClick = { dialPhoneNumber(context, profile.emergencyContactPhone) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "modal_call_proxy_button"
                )

                SecondaryOceanicButton(
                    text = "Share PDF Triage Summary",
                    icon = Icons.Default.PictureAsPdf,
                    onClick = onSharePdf,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GhostOutlinedButton(
                text = "Close Triage View",
                onClick = onDismiss,
                testTag = "close_triage_modal_button"
            )
        }
    )
}

@Composable
private fun EditEmergencyProfileDialog(
    profile: EmergencyProfileEntity,
    onDismiss: () -> Unit,
    onSave: (EmergencyProfileEntity) -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf(profile.fullName) }
    var bloodType by rememberSaveable { mutableStateOf(profile.bloodType) }
    var allergiesText by rememberSaveable { mutableStateOf(profile.severeAllergies) }
    var conditionsText by rememberSaveable { mutableStateOf(profile.chronicConditions) }
    var contactName by rememberSaveable { mutableStateOf(profile.emergencyContactName) }
    var contactPhone by rememberSaveable { mutableStateOf(profile.emergencyContactPhone) }
    var directiveNote by rememberSaveable { mutableStateOf(profile.criticalDirectiveNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "UPDATE MEDICAL ID",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepTealPrimary
                )
                Text(
                    text = "Edit Emergency Profile",
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClinicalInputField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        microLabel = "Patient Full Name",
                        placeholder = "Clara Vance-Sterling",
                        modifier = Modifier.weight(1.4f)
                    )
                    ClinicalInputField(
                        value = bloodType,
                        onValueChange = { bloodType = it },
                        microLabel = "Blood Type",
                        placeholder = "O+",
                        modifier = Modifier.weight(0.6f)
                    )
                }
                ClinicalInputField(
                    value = allergiesText,
                    onValueChange = { allergiesText = it },
                    microLabel = "Severe Allergies (Separate with |)",
                    placeholder = "Penicillin|Latex",
                    singleLine = false
                )
                ClinicalInputField(
                    value = conditionsText,
                    onValueChange = { conditionsText = it },
                    microLabel = "Chronic Conditions (Separate with |)",
                    placeholder = "Hypertension|Asthma",
                    singleLine = false
                )
                ClinicalInputField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    microLabel = "Emergency Contact Name",
                    placeholder = "Marcus Sterling"
                )
                ClinicalInputField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    microLabel = "Emergency Contact Phone",
                    placeholder = "+1 (415) 890-4321"
                )
                ClinicalInputField(
                    value = directiveNote,
                    onValueChange = { directiveNote = it },
                    microLabel = "Critical Care Directive",
                    placeholder = "EpiPen location & acute instructions",
                    singleLine = false
                )
            }
        },
        confirmButton = {
            PrimaryTealButton(
                text = "Save Profile",
                onClick = {
                    onSave(
                        profile.copy(
                            fullName = fullName.ifBlank { profile.fullName },
                            bloodType = bloodType.ifBlank { profile.bloodType },
                            severeAllergies = allergiesText.ifBlank { profile.severeAllergies },
                            chronicConditions = conditionsText.ifBlank { profile.chronicConditions },
                            emergencyContactName = contactName.ifBlank { profile.emergencyContactName },
                            emergencyContactPhone = contactPhone.ifBlank { profile.emergencyContactPhone },
                            criticalDirectiveNote = directiveNote.ifBlank { profile.criticalDirectiveNote }
                        )
                    )
                },
                testTag = "save_emergency_profile_button"
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

private fun dialPhoneNumber(context: Context, phone: String) {
    try {
        val cleanPhone = phone.filter { it.isDigit() || it == '+' }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
