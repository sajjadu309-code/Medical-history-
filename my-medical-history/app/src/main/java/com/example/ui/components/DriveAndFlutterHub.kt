package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.ui.MedicalUiState
import com.example.ui.theme.BadgeNormalBg
import com.example.ui.theme.BadgeNormalText
import com.example.ui.theme.BadgeOceanicBg
import com.example.ui.theme.BadgeOceanicText
import com.example.ui.theme.BorderDividerSlate
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.MetadataRuleDivider
import com.example.ui.theme.NeutralChipSurface
import com.example.ui.theme.NeutralSlate
import com.example.ui.theme.OceanicBlueSecondary
import com.example.ui.theme.OnSurfaceDarkNavy
import com.example.ui.theme.SlateLabelDark
import com.example.ui.theme.SurfaceContainerLowest
import com.example.util.DriveBackupAndZipManager

@Composable
fun DriveAndFlutterHubCard(
    uiState: MedicalUiState,
    onSaveDriveBackupUri: (Uri) -> Unit,
    onRestoreDriveBackupUri: (Uri) -> Unit,
    onShareDriveBackup: () -> Unit,
    onSaveFlutterZipUri: (Uri) -> Unit,
    onShareFlutterZip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showVsCodeGuideDialog by rememberSaveable { mutableStateOf(false) }

    val createDriveBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            onSaveDriveBackupUri(uri)
        }
    }

    val openDriveBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onRestoreDriveBackupUri(uri)
        }
    }

    val saveFlutterZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            onSaveFlutterZipUri(uri)
        }
    }

    Surface(
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderDividerSlate),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("drive_and_flutter_hub_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = BadgeOceanicBg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Google Drive & Offline Sync",
                            tint = OceanicBlueSecondary,
                            modifier = Modifier.padding(9.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "OFFLINE ENGINE & GOOGLE DRIVE SYNC",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepTealPrimary
                        )
                        Text(
                            text = "Google Drive Backup & Flutter ZIP",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceDarkNavy
                        )
                    }
                }
                StatusPillBadge(
                    statusType = "NORMAL",
                    label = "Offline Ready"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = uiState.lastDriveSyncStatus,
                style = MaterialTheme.typography.bodySmall,
                color = SlateLabelDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 1: Google Drive Cloud Backup & Restore
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SecondaryOceanicButton(
                    text = "Backup to Drive",
                    icon = Icons.Default.CloudUpload,
                    onClick = {
                        createDriveBackupLauncher.launch(
                            DriveBackupAndZipManager.DEFAULT_DRIVE_BACKUP_FILENAME
                        )
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "backup_to_drive_button"
                )
                GhostOutlinedButton(
                    text = "Restore Drive",
                    icon = Icons.Default.CloudDownload,
                    onClick = {
                        openDriveBackupLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "restore_from_drive_button"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = MetadataRuleDivider)
            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Download Flutter Project ZIP & VS Code Guide
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryTealButton(
                    text = "Save Flutter .ZIP",
                    icon = Icons.Default.FolderZip,
                    onClick = {
                        saveFlutterZipLauncher.launch(
                            DriveBackupAndZipManager.FLUTTER_ZIP_ASSET_NAME
                        )
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "save_flutter_zip_button"
                )
                GhostOutlinedButton(
                    text = "VS Code & APK Guide",
                    icon = Icons.Default.Code,
                    onClick = { showVsCodeGuideDialog = true },
                    modifier = Modifier.weight(1f),
                    testTag = "vscode_apk_guide_button"
                )
            }
        }
    }

    if (showVsCodeGuideDialog) {
        VsCodeAndFlutterGuideDialog(
            onDismiss = { showVsCodeGuideDialog = false },
            onShareZip = {
                showVsCodeGuideDialog = false
                onShareFlutterZip()
            },
            onShareDriveBackup = {
                showVsCodeGuideDialog = false
                onShareDriveBackup()
            }
        )
    }
}

@Composable
private fun VsCodeAndFlutterGuideDialog(
    onDismiss: () -> Unit,
    onShareZip: () -> Unit,
    onShareDriveBackup: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "FLUTTER + VS CODE + APK WORKFLOW",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepTealPrimary
                )
                Text(
                    text = "Run in VS Code & Build APK",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurfaceDarkNavy
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "1. Download the Flutter Project (.zip)",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurfaceDarkNavy
                )
                Text(
                    text = "Tap 'Save Flutter .ZIP' on the dashboard (or click 'Export ZIP' in the AI Studio menu — the complete Flutter codebase is inside the /flutter_medical_history folder and my_medical_history_flutter_project.zip).",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateLabelDark
                )

                Text(
                    text = "2. Run Smoothly in VS Code Terminal",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurfaceDarkNavy
                )
                Surface(
                    color = NeutralChipSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderDividerSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "cd flutter_medical_history\nflutter create . --project-name my_medical_history\nflutter pub get\nflutter run",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        color = BadgeOceanicText,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text(
                    text = "3. Create Installable Mobile APK File",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurfaceDarkNavy
                )
                Surface(
                    color = BadgeNormalBg.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BadgeNormalBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "flutter build apk --release\n\n# Output APK Path:\nbuild/app/outputs/flutter-apk/app-release.apk",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        color = BadgeNormalText,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryOceanicButton(
                        text = "Share .ZIP",
                        icon = Icons.Default.Share,
                        onClick = onShareZip,
                        modifier = Modifier.weight(1f)
                    )
                    GhostOutlinedButton(
                        text = "Share Drive JSON",
                        onClick = onShareDriveBackup,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            PrimaryTealButton(
                text = "Done",
                onClick = onDismiss
            )
        }
    )
}
