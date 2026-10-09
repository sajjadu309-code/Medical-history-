package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.local.LabResultEntity
import com.example.data.local.MedicalRecordEntity
import com.example.ui.MedicalUiState
import com.example.ui.components.ClinicalFilterChip
import com.example.ui.components.ClinicalInputField
import com.example.ui.components.GhostOutlinedButton
import com.example.ui.components.MedicalRecordCard
import com.example.ui.components.PrimaryTealButton
import com.example.ui.components.SecondaryOceanicButton
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.BorderDividerSlate
import com.example.ui.theme.CriticalEmergencyRed
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
import com.example.ui.theme.WarningRefillAmber

private val RECORD_CATEGORIES = listOf(
    "All",
    "Lab Results",
    "Diagnoses",
    "Procedures",
    "Imaging",
    "Immunizations"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    uiState: MedicalUiState,
    onSelectCategory: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectRecord: (MedicalRecordEntity?) -> Unit,
    onAddRecord: (String, String, String, String, String, String, String, String, String, String) -> Unit,
    onAddLabResult: (String, String, Float, String, Float, Float, String, String, String) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onDeleteRecord: (MedicalRecordEntity) -> Unit,
    onSharePdf: () -> Unit,
    onNavigateBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBackHome)

    var showAddRecordDialog by rememberSaveable { mutableStateOf(false) }
    var showAddLabBiomarkerDialog by rememberSaveable { mutableStateOf(false) }

    val filteredRecords = remember(
        uiState.records,
        uiState.selectedRecordCategory,
        uiState.recordSearchQuery
    ) {
        uiState.records.filter { record ->
            val matchesCategory = uiState.selectedRecordCategory == "All" ||
                record.category.equals(uiState.selectedRecordCategory, ignoreCase = true)
            val query = uiState.recordSearchQuery.trim()
            val matchesQuery = query.isEmpty() ||
                record.title.contains(query, ignoreCase = true) ||
                record.summary.contains(query, ignoreCase = true) ||
                record.treatingPhysician.contains(query, ignoreCase = true) ||
                record.keyMetricsSummary.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    val filteredLabs = remember(
        uiState.labResults,
        uiState.selectedRecordCategory,
        uiState.recordSearchQuery
    ) {
        val showLabs = uiState.selectedRecordCategory == "All" ||
            uiState.selectedRecordCategory.equals("Lab Results", ignoreCase = true)
        if (!showLabs) emptyList()
        else {
            val query = uiState.recordSearchQuery.trim()
            uiState.labResults.filter { lab ->
                query.isEmpty() ||
                    lab.biomarkerName.contains(query, ignoreCase = true) ||
                    lab.panelName.contains(query, ignoreCase = true) ||
                    lab.clinicalInterpretation.contains(query, ignoreCase = true)
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 700.dp

        if (isExpanded) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                ) {
                    RecordsHeaderAndFilters(
                        searchQuery = uiState.recordSearchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        selectedCategory = uiState.selectedRecordCategory,
                        onSelectCategory = onSelectCategory,
                        onAddRecordClick = { showAddRecordDialog = true },
                        onAddLabClick = { showAddLabBiomarkerDialog = true }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    RecordsListContent(
                        records = filteredRecords,
                        labResults = filteredLabs,
                        onSelectRecord = onSelectRecord,
                        onAddRecordClick = { showAddRecordDialog = true },
                        onAddLabClick = { showAddLabBiomarkerDialog = true },
                        contentPadding = PaddingValues(bottom = 24.dp)
                    )
                }

                Surface(
                    color = SurfaceContainerLowest,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderDividerSlate),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                ) {
                    val activeDetail = uiState.selectedRecordForDetail ?: filteredRecords.firstOrNull()
                    if (activeDetail != null) {
                        RecordDetailContent(
                            record = activeDetail,
                            onClose = null,
                            onSharePdf = onSharePdf,
                            onDelete = { onDeleteRecord(activeDetail) }
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Select a clinical record to inspect full diagnostic values.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeutralSlate
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                RecordsHeaderAndFilters(
                    searchQuery = uiState.recordSearchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    selectedCategory = uiState.selectedRecordCategory,
                    onSelectCategory = onSelectCategory,
                    onAddRecordClick = { showAddRecordDialog = true },
                    onAddLabClick = { showAddLabBiomarkerDialog = true }
                )

                Spacer(modifier = Modifier.height(14.dp))

                RecordsListContent(
                    records = filteredRecords,
                    labResults = filteredLabs,
                    onSelectRecord = onSelectRecord,
                    onAddRecordClick = { showAddRecordDialog = true },
                    onAddLabClick = { showAddLabBiomarkerDialog = true },
                    contentPadding = PaddingValues(bottom = 24.dp)
                )
            }

            if (uiState.selectedRecordForDetail != null) {
                ModalBottomSheet(
                    onDismissRequest = { onSelectRecord(null) },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    containerColor = Color.White
                ) {
                    RecordDetailContent(
                        record = uiState.selectedRecordForDetail,
                        onClose = { onSelectRecord(null) },
                        onSharePdf = onSharePdf,
                        onDelete = { onDeleteRecord(uiState.selectedRecordForDetail) }
                    )
                }
            }
        }
    }

    if (showAddRecordDialog) {
        AddMedicalRecordDialog(
            onDismiss = { showAddRecordDialog = false },
            onConfirm = { title, category, status, badge, summary, metrics, doc, fac, date, notes ->
                onAddRecord(title, category, status, badge, summary, metrics, doc, fac, date, notes)
                showAddRecordDialog = false
            }
        )
    }

    if (showAddLabBiomarkerDialog) {
        AddLabBiomarkerDialog(
            onDismiss = { showAddLabBiomarkerDialog = false },
            onConfirm = { biomarker, panel, value, unit, min, max, status, doc, notes ->
                onAddLabResult(biomarker, panel, value, unit, min, max, status, doc, notes)
                showAddLabBiomarkerDialog = false
            }
        )
    }
}

@Composable
private fun RecordsHeaderAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onAddRecordClick: () -> Unit,
    onAddLabClick: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DIAGNOSTIC ARCHIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepTealPrimary
                )
                Text(
                    text = "Clinical Records & Labs",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnSurfaceDarkNavy
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostOutlinedButton(
                    text = "Add Lab",
                    icon = Icons.Default.Science,
                    onClick = onAddLabClick,
                    testTag = "add_lab_biomarker_button"
                )
                PrimaryTealButton(
                    text = "Add Record",
                    icon = Icons.Default.Add,
                    onClick = onAddRecordClick,
                    testTag = "add_record_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        ClinicalInputField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            microLabel = "Filter Clinical History & Biomarkers",
            placeholder = "Search diagnoses, labs, HbA1c, Ferritin, physicians…",
            leadingIcon = Icons.Default.Search,
            testTag = "search_records_input"
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(RECORD_CATEGORIES) { category ->
                ClinicalFilterChip(
                    label = category,
                    selected = selectedCategory == category,
                    onClick = { onSelectCategory(category) },
                    testTag = "category_chip_$category"
                )
            }
        }
    }
}

@Composable
private fun RecordsListContent(
    records: List<MedicalRecordEntity>,
    labResults: List<LabResultEntity>,
    onSelectRecord: (MedicalRecordEntity) -> Unit,
    onAddRecordClick: () -> Unit,
    onAddLabClick: () -> Unit,
    contentPadding: PaddingValues
) {
    if (records.isEmpty() && labResults.isEmpty()) {
        Surface(
            color = SurfaceContainerLowest,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderDividerSlate),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = DeepTealPrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Matching Clinical Records",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurfaceDarkNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Adjust your category filter or log a new diagnostic lab report.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralSlate
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryTealButton(
                    text = "Log New Clinical Record",
                    icon = Icons.Default.Add,
                    onClick = onAddRecordClick
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("records_lazy_column")
        ) {
            if (labResults.isNotEmpty()) {
                item {
                    QuantitativeLabResultsPanelCard(
                        labResults = labResults,
                        onAddLabClick = onAddLabClick
                    )
                }
            }

            items(records, key = { it.id }) { record ->
                MedicalRecordCard(
                    record = record,
                    onClick = { onSelectRecord(record) }
                )
            }
        }
    }
}

@Composable
private fun QuantitativeLabResultsPanelCard(
    labResults: List<LabResultEntity>,
    onAddLabClick: () -> Unit
) {
    Surface(
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderDividerSlate),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quantitative_lab_panel_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LABORATORY BIOMARKER TELEMETRY",
                        style = MaterialTheme.typography.labelSmall,
                        color = OceanicBlueSecondary
                    )
                    Text(
                        text = "Quantitative Lab Results",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurfaceDarkNavy
                    )
                }
                StatusPillBadge(
                    statusType = "INFO",
                    label = "${labResults.size} Biomarkers"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                labResults.forEach { lab ->
                    val span = (lab.referenceMax - lab.referenceMin).coerceAtLeast(1f)
                    val normalized = ((lab.measuredValue - lab.referenceMin) / span).coerceIn(0.08f, 1f)
                    val indicatorColor = when (lab.flagStatus) {
                        "CRITICAL" -> CriticalEmergencyRed
                        "PENDING" -> WarningRefillAmber
                        else -> EmeraldGreenTertiary
                    }

                    Surface(
                        color = NeutralChipSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderDividerSlate),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lab_result_row_${lab.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lab.panelName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DeepTealPrimary
                                    )
                                    Text(
                                        text = lab.biomarkerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OnSurfaceDarkNavy
                                    )
                                }
                                StatusPillBadge(
                                    statusType = lab.flagStatus,
                                    label = lab.flagLabel
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = lab.valueDisplay,
                                        style = MetricNumeralStyle,
                                        color = OnSurfaceDarkNavy
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = lab.unit,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeutralSlate,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                Text(
                                    text = "Ref: ${lab.referenceRangeLabel}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SlateLabelDark
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { normalized },
                                color = indicatorColor,
                                trackColor = Color.White,
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = lab.clinicalInterpretation,
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateLabelDark
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(thickness = 1.dp, color = MetadataRuleDivider)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${lab.orderingPhysician} • ${lab.labFacility}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeutralSlate
                                )
                                Text(
                                    text = lab.collectedDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeutralSlate
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordDetailContent(
    record: MedicalRecordEntity,
    onClose: (() -> Unit)?,
    onSharePdf: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.category.uppercase()} • ${record.dateFormatted}",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepTealPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurfaceDarkNavy
                )
            }
            if (onClose != null) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close record details",
                        tint = NeutralSlate
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        StatusPillBadge(
            statusType = record.status,
            label = record.statusBadgeText
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = NeutralChipSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderDividerSlate),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "KEY DIAGNOSTIC BIOMARKERS & FINDINGS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralSlate
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = record.keyMetricsSummary,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceDarkNavy
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CLINICAL INTERPRETATION",
            style = MaterialTheme.typography.labelSmall,
            color = NeutralSlate
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = record.summary,
            style = MaterialTheme.typography.bodyLarge,
            color = SlateLabelDark
        )

        if (record.clinicalNotes.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "PHYSICIAN CARE DIRECTIVE",
                style = MaterialTheme.typography.labelSmall,
                color = NeutralSlate
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = record.clinicalNotes,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateLabelDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 1.dp, color = MetadataRuleDivider)
        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = DeepTealPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Attending: ${record.treatingPhysician}",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceDarkNavy
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.LocalHospital,
                contentDescription = null,
                tint = OceanicBlueSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Facility: ${record.facility}",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateLabelDark
            )
        }

        if (record.attachmentFileName.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = OceanicBlueSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Document: ${record.attachmentFileName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = OceanicBlueSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SecondaryOceanicButton(
                text = "Share PDF Record",
                icon = Icons.Default.PictureAsPdf,
                onClick = onSharePdf,
                modifier = Modifier.weight(1f),
                testTag = "detail_share_pdf_button"
            )
            GhostOutlinedButton(
                text = "Delete",
                icon = Icons.Default.DeleteOutline,
                onClick = onDelete,
                testTag = "detail_delete_record_button"
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AddMedicalRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String, String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Lab Results") }
    var selectedStatus by rememberSaveable { mutableStateOf("NORMAL") }
    var keyMetrics by rememberSaveable { mutableStateOf("") }
    var summary by rememberSaveable { mutableStateOf("") }
    var physician by rememberSaveable { mutableStateOf("Dr. Elena Rostova, MD") }
    var facility by rememberSaveable { mutableStateOf("Pacific Clinical Diagnostics Center") }
    var dateFormatted by rememberSaveable { mutableStateOf("Oct 08, 2026") }
    var notes by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "NEW CLINICAL ENTRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepTealPrimary
                )
                Text(
                    text = "Add Medical Record",
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
                    value = title,
                    onValueChange = { title = it },
                    microLabel = "Record Title / Diagnostic Name",
                    placeholder = "e.g., Complete Blood Count (CBC) & Ferritin",
                    testTag = "input_record_title"
                )

                Text(
                    text = "RECORD CATEGORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralSlate
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(RECORD_CATEGORIES.drop(1)) { cat ->
                        ClinicalFilterChip(
                            label = cat,
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }

                Text(
                    text = "CLINICAL STATUS FLAG",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralSlate
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "NORMAL" to "Normal",
                        "PENDING" to "Pending",
                        "CRITICAL" to "Critical"
                    ).forEach { (code, label) ->
                        ClinicalFilterChip(
                            label = label,
                            selected = selectedStatus == code,
                            onClick = { selectedStatus = code }
                        )
                    }
                }

                ClinicalInputField(
                    value = keyMetrics,
                    onValueChange = { keyMetrics = it },
                    microLabel = "Key Quantitative Metrics / Values",
                    placeholder = "e.g., Hemoglobin: 13.8 g/dL • Ferritin: 82 ng/mL",
                    testTag = "input_record_metrics"
                )

                ClinicalInputField(
                    value = summary,
                    onValueChange = { summary = it },
                    microLabel = "Clinical Interpretation Summary",
                    placeholder = "Describe diagnostic findings and reference ranges…",
                    singleLine = false,
                    testTag = "input_record_summary"
                )

                ClinicalInputField(
                    value = physician,
                    onValueChange = { physician = it },
                    microLabel = "Treating Physician",
                    placeholder = "Dr. Elena Rostova, MD"
                )
            }
        },
        confirmButton = {
            PrimaryTealButton(
                text = "Save Record",
                onClick = {
                    val finalTitle = title.ifBlank { "Clinical Diagnostic Evaluation" }
                    val finalMetrics = keyMetrics.ifBlank { "Verified within clinical reference bounds" }
                    val finalSummary = summary.ifBlank { "Routine diagnostic evaluation logged by patient." }
                    val badgeLabel = when (selectedStatus) {
                        "CRITICAL" -> "Critical Alert"
                        "PENDING" -> "Pending Review"
                        else -> "Normal"
                    }
                    onConfirm(
                        finalTitle,
                        selectedCategory,
                        selectedStatus,
                        badgeLabel,
                        finalSummary,
                        finalMetrics,
                        physician,
                        facility,
                        dateFormatted,
                        notes
                    )
                },
                testTag = "confirm_save_record_button"
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
private fun AddLabBiomarkerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Float, String, Float, Float, String, String, String) -> Unit
) {
    var biomarkerName by rememberSaveable { mutableStateOf("") }
    var panelName by rememberSaveable { mutableStateOf("Comprehensive Metabolic & Lipid Panel") }
    var measuredValueText by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf("mg/dL") }
    var refMinText by rememberSaveable { mutableStateOf("60") }
    var refMaxText by rememberSaveable { mutableStateOf("100") }
    var selectedStatus by rememberSaveable { mutableStateOf("NORMAL") }
    var orderingPhysician by rememberSaveable { mutableStateOf("Dr. Elena Rostova, MD") }
    var clinicalInterpretation by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "NEW LAB RESULT BIOMARKER",
                    style = MaterialTheme.typography.labelSmall,
                    color = OceanicBlueSecondary
                )
                Text(
                    text = "Log Quantitative Lab Result",
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
                    value = biomarkerName,
                    onValueChange = { biomarkerName = it },
                    microLabel = "Biomarker / Analyte Name",
                    placeholder = "e.g., High-Sensitivity CRP (hs-CRP)",
                    testTag = "input_lab_biomarker_name"
                )
                ClinicalInputField(
                    value = panelName,
                    onValueChange = { panelName = it },
                    microLabel = "Diagnostic Panel",
                    placeholder = "e.g., Cardiovascular Inflammation Panel"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClinicalInputField(
                        value = measuredValueText,
                        onValueChange = { measuredValueText = it },
                        microLabel = "Measured Value",
                        placeholder = "0.6",
                        modifier = Modifier.weight(1f),
                        testTag = "input_lab_value"
                    )
                    ClinicalInputField(
                        value = unit,
                        onValueChange = { unit = it },
                        microLabel = "Unit",
                        placeholder = "mg/L",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClinicalInputField(
                        value = refMinText,
                        onValueChange = { refMinText = it },
                        microLabel = "Reference Min",
                        placeholder = "0.0",
                        modifier = Modifier.weight(1f)
                    )
                    ClinicalInputField(
                        value = refMaxText,
                        onValueChange = { refMaxText = it },
                        microLabel = "Reference Max",
                        placeholder = "1.0",
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "CLINICAL FLAG",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralSlate
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "NORMAL" to "Optimal",
                        "PENDING" to "Borderline",
                        "CRITICAL" to "High Alert"
                    ).forEach { (code, label) ->
                        ClinicalFilterChip(
                            label = label,
                            selected = selectedStatus == code,
                            onClick = { selectedStatus = code }
                        )
                    }
                }
                ClinicalInputField(
                    value = clinicalInterpretation,
                    onValueChange = { clinicalInterpretation = it },
                    microLabel = "Clinical Interpretation",
                    placeholder = "Low systemic vascular inflammation risk.",
                    singleLine = false
                )
            }
        },
        confirmButton = {
            PrimaryTealButton(
                text = "Save Biomarker",
                onClick = {
                    val name = biomarkerName.ifBlank { "High-Sensitivity CRP" }
                    val value = measuredValueText.toFloatOrNull() ?: 0.6f
                    val min = refMinText.toFloatOrNull() ?: 0.0f
                    val max = refMaxText.toFloatOrNull() ?: 1.0f
                    val notes = clinicalInterpretation.ifBlank { "Within expected clinical reference range." }
                    onConfirm(
                        name,
                        panelName,
                        value,
                        unit,
                        min,
                        max,
                        selectedStatus,
                        orderingPhysician,
                        notes
                    )
                },
                testTag = "confirm_save_lab_button"
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
