package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppointmentEntity
import com.example.data.local.EmergencyProfileEntity
import com.example.data.local.LabResultEntity
import com.example.data.local.MedicalDatabase
import com.example.data.local.MedicalRecordEntity
import com.example.data.local.MedicationEntity
import com.example.data.local.SeedClinicalData
import com.example.data.local.VitalSignEntity
import com.example.data.repository.MedicalRepository
import com.example.util.DriveBackupAndZipManager
import com.example.util.MedicalPdfExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppDestination(val route: String, val label: String) {
    OVERVIEW("overview", "Overview"),
    RECORDS("records", "Records"),
    MEDICATIONS("medications", "Medications"),
    EMERGENCY_ID("emergency_id", "Emergency ID")
}

enum class VitalTimeframe(val label: String) {
    DAYS_7("7D"),
    MONTH_1("1M"),
    MONTHS_6("6M"),
    YEAR_1("1Y")
}

data class MedicalUiState(
    val vitals: List<VitalSignEntity> = SeedClinicalData.defaultVitalSigns,
    val medications: List<MedicationEntity> = SeedClinicalData.defaultMedications,
    val labResults: List<LabResultEntity> = SeedClinicalData.defaultLabResults,
    val records: List<MedicalRecordEntity> = SeedClinicalData.defaultMedicalRecords,
    val appointments: List<AppointmentEntity> = SeedClinicalData.defaultAppointments,
    val emergencyProfile: EmergencyProfileEntity = SeedClinicalData.defaultEmergencyProfile,
    val selectedTimeframe: VitalTimeframe = VitalTimeframe.DAYS_7,
    val selectedVitalIndex: Int = 0,
    val selectedRecordCategory: String = "All",
    val recordSearchQuery: String = "",
    val selectedRecordForDetail: MedicalRecordEntity? = null,
    val showEmergencyResponderModal: Boolean = false,
    val lastDriveSyncStatus: String = "Offline DB Active • Ready for Google Drive Sync",
    val snackbarMessage: String? = null
)

class MedicalViewModel(
    application: Application,
    private val repository: MedicalRepository
) : AndroidViewModel(application) {

    private val _selectedTimeframe = MutableStateFlow(VitalTimeframe.DAYS_7)
    private val _selectedVitalIndex = MutableStateFlow(0)
    private val _selectedRecordCategory = MutableStateFlow("All")
    private val _recordSearchQuery = MutableStateFlow("")
    private val _selectedRecordForDetail = MutableStateFlow<MedicalRecordEntity?>(null)
    private val _showEmergencyResponderModal = MutableStateFlow(false)
    private val _lastDriveSyncStatus =
        MutableStateFlow("Offline DB Active • Ready for Google Drive Sync")
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    private val _currentDestination = MutableStateFlow(AppDestination.OVERVIEW)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    val uiState: StateFlow<MedicalUiState> = combine(
        repository.vitalSigns,
        repository.medications,
        repository.medicalRecords,
        repository.appointments,
        repository.emergencyProfile
    ) { vitals, meds, records, appts, profile ->
        MedicalUiState(
            vitals = vitals.ifEmpty { SeedClinicalData.defaultVitalSigns },
            medications = meds.ifEmpty { SeedClinicalData.defaultMedications },
            records = records.ifEmpty { SeedClinicalData.defaultMedicalRecords },
            appointments = appts.ifEmpty { SeedClinicalData.defaultAppointments },
            emergencyProfile = profile ?: SeedClinicalData.defaultEmergencyProfile
        )
    }.combine(_selectedTimeframe) { state, timeframe ->
        state.copy(selectedTimeframe = timeframe)
    }.combine(_selectedVitalIndex) { state, vitalIdx ->
        state.copy(selectedVitalIndex = vitalIdx.coerceIn(0, (state.vitals.size - 1).coerceAtLeast(0)))
    }.combine(_selectedRecordCategory) { state, category ->
        state.copy(selectedRecordCategory = category)
    }.combine(_recordSearchQuery) { state, query ->
        state.copy(recordSearchQuery = query)
    }.combine(_selectedRecordForDetail) { state, detail ->
        state.copy(selectedRecordForDetail = detail)
    }.combine(_showEmergencyResponderModal) { state, emergencyModal ->
        state.copy(showEmergencyResponderModal = emergencyModal)
    }.combine(_lastDriveSyncStatus) { state, syncStatus ->
        state.copy(lastDriveSyncStatus = syncStatus)
    }.combine(_snackbarMessage) { state, msg ->
        state.copy(snackbarMessage = msg)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MedicalUiState()
    )

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    fun selectTimeframe(timeframe: VitalTimeframe) {
        _selectedTimeframe.value = timeframe
    }

    fun selectVitalIndex(index: Int) {
        _selectedVitalIndex.value = index
    }

    fun selectRecordCategory(category: String) {
        _selectedRecordCategory.value = category
    }

    fun updateRecordSearchQuery(query: String) {
        _recordSearchQuery.value = query
    }

    fun selectRecordForDetail(record: MedicalRecordEntity?) {
        _selectedRecordForDetail.value = record
    }

    fun setEmergencyResponderModalVisible(visible: Boolean) {
        _showEmergencyResponderModal.value = visible
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun getDriveBackupJson(): String {
        val current = uiState.value
        return DriveBackupAndZipManager.buildDriveBackupJson(
            profile = current.emergencyProfile,
            vitals = current.vitals,
            medications = current.medications,
            records = current.records,
            appointments = current.appointments
        )
    }

    fun shareBackupToGoogleDrive(context: Context) {
        val json = getDriveBackupJson()
        DriveBackupAndZipManager.shareBackupToGoogleDrive(context, json) { msg ->
            _lastDriveSyncStatus.value = "Backed up to Google Drive • Just now"
            _snackbarMessage.value = msg
        }
    }

    fun saveBackupToSelectedUri(context: Context, uri: Uri) {
        val json = getDriveBackupJson()
        DriveBackupAndZipManager.writeBackupToDocumentUri(context, uri, json) { msg ->
            _lastDriveSyncStatus.value = "Saved to Google Drive / Storage • Just now"
            _snackbarMessage.value = msg
        }
    }

    fun restoreBackupFromSelectedUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val rawJson = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                    it.readText()
                }
                if (!rawJson.isNullOrBlank()) {
                    val parsed = DriveBackupAndZipManager.parseDriveBackupJson(rawJson)
                    repository.restoreFromDriveBackup(parsed)
                    _lastDriveSyncStatus.value = "Restored from Google Drive • Just now"
                    _snackbarMessage.value =
                        "Restored ${parsed.records.size} clinical records & ${parsed.medications.size} medications from Drive!"
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Invalid backup file: ${e.localizedMessage}"
            }
        }
    }

    fun saveFlutterZipToSelectedUri(context: Context, uri: Uri) {
        DriveBackupAndZipManager.copyFlutterProjectZipToUri(context, uri) { msg ->
            _snackbarMessage.value = msg
        }
    }

    fun shareFlutterProjectZip(context: Context) {
        DriveBackupAndZipManager.shareFlutterProjectZip(context) { msg ->
            _snackbarMessage.value = msg
        }
    }

    fun toggleMedicationAdherence(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.toggleMedicationAdherence(medication)
            val statusText = if (!medication.takenToday) {
                "Logged ${medication.name} (${medication.dosage}) as taken today"
            } else {
                "Unchecked ${medication.name} dose for today"
            }
            _snackbarMessage.value = statusText
        }
    }

    fun requestMedicationRefill(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.requestMedicationRefill(medication)
            _snackbarMessage.value =
                "Prescription refill requested for ${medication.name} (${medication.dosage}) with ${medication.prescribingDoctor}"
        }
    }

    fun restockMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.restockMedication(medication)
            _snackbarMessage.value = "${medication.name} supply restocked to ${medication.totalPills} pills"
        }
    }

    fun addMedication(
        name: String,
        dosage: String,
        schedule: String,
        purpose: String,
        doctor: String,
        pillsRemaining: Int,
        totalPills: Int,
        instructions: String
    ) {
        viewModelScope.launch {
            repository.addMedication(
                MedicationEntity(
                    name = name.trim(),
                    dosage = dosage.trim(),
                    schedule = schedule.trim(),
                    purpose = purpose.trim(),
                    prescribingDoctor = doctor.trim(),
                    pillsRemaining = pillsRemaining,
                    totalPills = totalPills.coerceAtLeast(pillsRemaining),
                    takenToday = false,
                    refillRequested = false,
                    isArchived = false,
                    startDate = "Oct 08, 2026",
                    instructions = instructions.trim()
                )
            )
            _snackbarMessage.value = "Added $name ($dosage) to Active Medications"
        }
    }

    fun toggleArchiveMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            val nextArchived = !medication.isArchived
            repository.archiveMedication(medication, nextArchived)
            _snackbarMessage.value = if (nextArchived) {
                "Moved ${medication.name} to Historical Archive"
            } else {
                "Restored ${medication.name} to Active Medications"
            }
        }
    }

    fun addMedicalRecord(
        title: String,
        category: String,
        status: String,
        statusBadgeText: String,
        summary: String,
        keyMetricsSummary: String,
        physician: String,
        facility: String,
        dateFormatted: String,
        clinicalNotes: String
    ) {
        viewModelScope.launch {
            val newRec = MedicalRecordEntity(
                title = title.trim(),
                category = category,
                status = status,
                statusBadgeText = statusBadgeText.trim(),
                summary = summary.trim(),
                keyMetricsSummary = keyMetricsSummary.trim(),
                treatingPhysician = physician.trim(),
                facility = facility.trim(),
                dateFormatted = dateFormatted.trim(),
                attachmentFileName = "${title.take(18).replace(" ", "_")}_Report.pdf",
                clinicalNotes = clinicalNotes.trim()
            )
            repository.addMedicalRecord(newRec)
            _snackbarMessage.value = "Saved clinical record: ${newRec.title}"
        }
    }

    fun deleteMedicalRecord(record: MedicalRecordEntity) {
        viewModelScope.launch {
            repository.deleteMedicalRecord(record.id)
            if (_selectedRecordForDetail.value?.id == record.id) {
                _selectedRecordForDetail.value = null
            }
            _snackbarMessage.value = "Removed record: ${record.title}"
        }
    }

    fun logVitalSignReading(
        vital: VitalSignEntity,
        newValueDisplay: String,
        newPrimary: Float,
        newSecondary: Float?,
        status: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.updateVitalSignReading(
                vital = vital,
                newValueDisplay = newValueDisplay.trim(),
                newNumericPrimary = newPrimary,
                newNumericSecondary = newSecondary,
                newStatus = status,
                note = note.trim()
            )
            _snackbarMessage.value = "Updated ${vital.type}: $newValueDisplay ${vital.unit}"
        }
    }

    fun addAppointment(
        doctorName: String,
        specialty: String,
        facility: String,
        dateFormatted: String,
        timeFormatted: String,
        visitReason: String,
        preparationInstructions: String
    ) {
        viewModelScope.launch {
            repository.addAppointment(
                AppointmentEntity(
                    doctorName = doctorName.trim(),
                    specialty = specialty.trim(),
                    facility = facility.trim(),
                    dateFormatted = dateFormatted.trim(),
                    timeFormatted = timeFormatted.trim(),
                    visitReason = visitReason.trim(),
                    status = "PENDING",
                    statusLabel = "Upcoming",
                    preparationInstructions = preparationInstructions.trim()
                )
            )
            _snackbarMessage.value = "Scheduled consultation with $doctorName"
        }
    }

    fun updateEmergencyProfile(profile: EmergencyProfileEntity) {
        viewModelScope.launch {
            repository.updateEmergencyProfile(profile)
            _snackbarMessage.value = "Updated Emergency Medical ID & Directives"
        }
    }

    fun exportAndSharePdf(context: Context) {
        val current = uiState.value
        MedicalPdfExporter.exportAndShareClinicalSummary(
            context = context,
            profile = current.emergencyProfile,
            vitals = current.vitals,
            medications = current.medications,
            records = current.records,
            appointments = current.appointments,
            onResultMessage = { msg ->
                _snackbarMessage.value = msg
            }
        )
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = MedicalDatabase.getDatabase(application)
                    val repo = MedicalRepository(db.medicalDao())
                    return MedicalViewModel(application, repo) as T
                }
            }
    }
}
