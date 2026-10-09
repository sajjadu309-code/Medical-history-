package com.example.data.repository

import com.example.data.local.AppointmentEntity
import com.example.data.local.EmergencyProfileEntity
import com.example.data.local.MedicalDao
import com.example.data.local.MedicalRecordEntity
import com.example.data.local.MedicationEntity
import com.example.data.local.SeedClinicalData
import com.example.data.local.VitalSignEntity
import com.example.util.ParsedDriveBackup
import kotlinx.coroutines.flow.Flow

class MedicalRepository(private val dao: MedicalDao) {

    val vitalSigns: Flow<List<VitalSignEntity>> = dao.getAllVitalSigns()
    val medications: Flow<List<MedicationEntity>> = dao.getAllMedications()
    val medicalRecords: Flow<List<MedicalRecordEntity>> = dao.getAllMedicalRecords()
    val appointments: Flow<List<AppointmentEntity>> = dao.getAllAppointments()
    val emergencyProfile: Flow<EmergencyProfileEntity?> = dao.getEmergencyProfile()

    suspend fun ensureSeeded() {
        if (dao.getVitalSignCount() == 0) {
            dao.upsertEmergencyProfile(SeedClinicalData.defaultEmergencyProfile)
            dao.insertVitalSigns(SeedClinicalData.defaultVitalSigns)
            dao.insertMedications(SeedClinicalData.defaultMedications)
            dao.insertMedicalRecords(SeedClinicalData.defaultMedicalRecords)
            dao.insertAppointments(SeedClinicalData.defaultAppointments)
        }
    }

    suspend fun restoreFromDriveBackup(backup: ParsedDriveBackup) {
        dao.upsertEmergencyProfile(backup.profile)
        if (backup.vitals.isNotEmpty()) {
            dao.clearAllVitalSigns()
            dao.insertVitalSigns(backup.vitals)
        }
        if (backup.medications.isNotEmpty()) {
            dao.clearAllMedications()
            dao.insertMedications(backup.medications)
        }
        if (backup.records.isNotEmpty()) {
            dao.clearAllMedicalRecords()
            dao.insertMedicalRecords(backup.records)
        }
        if (backup.appointments.isNotEmpty()) {
            dao.clearAllAppointments()
            dao.insertAppointments(backup.appointments)
        }
    }

    suspend fun toggleMedicationAdherence(medication: MedicationEntity) {
        val nextTaken = !medication.takenToday
        val adjustedPills = if (nextTaken) {
            (medication.pillsRemaining - 1).coerceAtLeast(0)
        } else {
            (medication.pillsRemaining + 1).coerceAtMost(medication.totalPills)
        }
        dao.updateMedication(
            medication.copy(
                takenToday = nextTaken,
                pillsRemaining = adjustedPills
            )
        )
    }

    suspend fun requestMedicationRefill(medication: MedicationEntity) {
        dao.updateMedication(
            medication.copy(refillRequested = true)
        )
    }

    suspend fun restockMedication(medication: MedicationEntity) {
        dao.updateMedication(
            medication.copy(
                pillsRemaining = medication.totalPills,
                refillRequested = false
            )
        )
    }

    suspend fun addMedication(medication: MedicationEntity) {
        dao.insertMedication(medication)
    }

    suspend fun archiveMedication(medication: MedicationEntity, archived: Boolean) {
        dao.updateMedication(medication.copy(isArchived = archived))
    }

    suspend fun deleteMedication(id: Int) {
        dao.deleteMedicationById(id)
    }

    suspend fun addMedicalRecord(record: MedicalRecordEntity) {
        dao.insertMedicalRecord(record)
    }

    suspend fun updateMedicalRecord(record: MedicalRecordEntity) {
        dao.updateMedicalRecord(record)
    }

    suspend fun deleteMedicalRecord(id: Int) {
        dao.deleteMedicalRecordById(id)
    }

    suspend fun updateVitalSignReading(
        vital: VitalSignEntity,
        newValueDisplay: String,
        newNumericPrimary: Float,
        newNumericSecondary: Float?,
        newStatus: String,
        note: String
    ) {
        val updated7d = (vital.trend7dCsv.split(",").drop(1) + newNumericPrimary.toInt().toString())
            .joinToString(",")
        dao.updateVitalSign(
            vital.copy(
                valueDisplay = newValueDisplay,
                numericPrimary = newNumericPrimary,
                numericSecondary = newNumericSecondary,
                status = newStatus,
                trend7dCsv = updated7d,
                recordedAt = "Just now",
                physicianNote = note.ifBlank { vital.physicianNote },
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun addAppointment(appointment: AppointmentEntity) {
        dao.insertAppointment(appointment)
    }

    suspend fun deleteAppointment(id: Int) {
        dao.deleteAppointmentById(id)
    }

    suspend fun updateEmergencyProfile(profile: EmergencyProfileEntity) {
        dao.upsertEmergencyProfile(profile)
    }
}
