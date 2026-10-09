package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppDestination
import com.example.ui.MedicalViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmergencyProfileScreen
import com.example.ui.screens.EmergencyResponderModalDialog
import com.example.ui.screens.MedicationsScreen
import com.example.ui.screens.RecordsScreen
import com.example.ui.theme.CanvasBase
import com.example.ui.theme.CriticalEmergencyRed
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeutralSlate
import com.example.ui.theme.OnSurfaceDarkNavy
import com.example.ui.theme.SurfaceContainerLowest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MedicalViewModel = viewModel(
                    factory = MedicalViewModel.provideFactory(application)
                )
                MyMedicalHistoryApp(viewModel = viewModel)
            }
        }
    }
}

private data class NavItemSpec(
    val destination: AppDestination,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val NAV_ITEMS = listOf(
    NavItemSpec(
        destination = AppDestination.OVERVIEW,
        selectedIcon = Icons.Filled.SpaceDashboard,
        unselectedIcon = Icons.Outlined.SpaceDashboard
    ),
    NavItemSpec(
        destination = AppDestination.RECORDS,
        selectedIcon = Icons.Filled.Description,
        unselectedIcon = Icons.Outlined.Description
    ),
    NavItemSpec(
        destination = AppDestination.MEDICATIONS,
        selectedIcon = Icons.Filled.Medication,
        unselectedIcon = Icons.Outlined.Medication
    ),
    NavItemSpec(
        destination = AppDestination.EMERGENCY_ID,
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )
)

@Composable
fun MyMedicalHistoryApp(viewModel: MedicalViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = CanvasBase,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = SurfaceContainerLowest,
                        tonalElevation = 4.dp,
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        NAV_ITEMS.forEach { item ->
                            val selected = currentDestination == item.destination
                            val isEmergencyTab = item.destination == AppDestination.EMERGENCY_ID
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(item.destination) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.destination.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.destination.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = if (isEmergencyTab) CriticalEmergencyRed else DeepTealPrimary,
                                    indicatorColor = if (isEmergencyTab) CriticalEmergencyRed else DeepTealPrimary,
                                    unselectedIconColor = if (isEmergencyTab) CriticalEmergencyRed.copy(alpha = 0.8f) else NeutralSlate,
                                    unselectedTextColor = NeutralSlate
                                ),
                                modifier = Modifier.testTag("nav_item_${item.destination.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = SurfaceContainerLowest,
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("side_navigation_rail")
                    ) {
                        NAV_ITEMS.forEach { item ->
                            val selected = currentDestination == item.destination
                            val isEmergencyTab = item.destination == AppDestination.EMERGENCY_ID
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(item.destination) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.destination.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.destination.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = if (isEmergencyTab) CriticalEmergencyRed else DeepTealPrimary,
                                    indicatorColor = if (isEmergencyTab) CriticalEmergencyRed else DeepTealPrimary,
                                    unselectedIconColor = NeutralSlate,
                                    unselectedTextColor = NeutralSlate
                                ),
                                modifier = Modifier.testTag("rail_item_${item.destination.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Crossfade(
                        targetState = currentDestination,
                        label = "screen_crossfade"
                    ) { destination ->
                        when (destination) {
                            AppDestination.OVERVIEW -> DashboardScreen(
                                uiState = uiState,
                                onSelectTimeframe = viewModel::selectTimeframe,
                                onSelectVitalIndex = viewModel::selectVitalIndex,
                                onLogVitalReading = viewModel::logVitalSignReading,
                                onToggleMedication = viewModel::toggleMedicationAdherence,
                                onRequestRefill = viewModel::requestMedicationRefill,
                                onNavigate = viewModel::navigateTo,
                                onOpenEmergencyModal = { viewModel.setEmergencyResponderModalVisible(true) },
                                onSharePdf = { viewModel.exportAndSharePdf(context) },
                                onSaveDriveBackupUri = { uri -> viewModel.saveBackupToSelectedUri(context, uri) },
                                onRestoreDriveBackupUri = { uri -> viewModel.restoreBackupFromSelectedUri(context, uri) },
                                onShareDriveBackup = { viewModel.shareBackupToGoogleDrive(context) },
                                onSaveFlutterZipUri = { uri -> viewModel.saveFlutterZipToSelectedUri(context, uri) },
                                onShareFlutterZip = { viewModel.shareFlutterProjectZip(context) },
                                onAddAppointment = viewModel::addAppointment
                            )

                            AppDestination.RECORDS -> RecordsScreen(
                                uiState = uiState,
                                onSelectCategory = viewModel::selectRecordCategory,
                                onSearchQueryChange = viewModel::updateRecordSearchQuery,
                                onSelectRecord = viewModel::selectRecordForDetail,
                                onAddRecord = viewModel::addMedicalRecord,
                                onDeleteRecord = viewModel::deleteMedicalRecord,
                                onSharePdf = { viewModel.exportAndSharePdf(context) },
                                onNavigateBackHome = { viewModel.navigateTo(AppDestination.OVERVIEW) }
                            )

                            AppDestination.MEDICATIONS -> MedicationsScreen(
                                uiState = uiState,
                                onToggleAdherence = viewModel::toggleMedicationAdherence,
                                onRequestRefill = viewModel::requestMedicationRefill,
                                onRestockMedication = viewModel::restockMedication,
                                onToggleArchive = viewModel::toggleArchiveMedication,
                                onAddMedication = viewModel::addMedication,
                                onNavigateBackHome = { viewModel.navigateTo(AppDestination.OVERVIEW) }
                            )

                            AppDestination.EMERGENCY_ID -> EmergencyProfileScreen(
                                uiState = uiState,
                                onUpdateEmergencyProfile = viewModel::updateEmergencyProfile,
                                onOpenEmergencyResponderModal = { viewModel.setEmergencyResponderModalVisible(true) },
                                onSharePdf = { viewModel.exportAndSharePdf(context) },
                                onNavigateBackHome = { viewModel.navigateTo(AppDestination.OVERVIEW) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showEmergencyResponderModal) {
        EmergencyResponderModalDialog(
            profile = uiState.emergencyProfile,
            onDismiss = { viewModel.setEmergencyResponderModalVisible(false) },
            onSharePdf = { viewModel.exportAndSharePdf(context) }
        )
    }
}
