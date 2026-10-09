package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalDao {

    // Vital Signs
    @Query("SELECT * FROM vital_signs ORDER BY id ASC")
    fun getAllVitalSigns(): Flow<List<VitalSignEntity>>

    @Query("SELECT COUNT(*) FROM vital_signs")
    suspend fun getVitalSignCount(): Int

    @Query("DELETE FROM vital_signs")
    suspend fun clearAllVitalSigns()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitalSigns(vitals: List<VitalSignEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitalSign(vital: VitalSignEntity)

    @Update
    suspend fun updateVitalSign(vital: VitalSignEntity)

    // Medications
    @Query("SELECT * FROM medications ORDER BY isArchived ASC, id ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("DELETE FROM medications")
    suspend fun clearAllMedications()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<MedicationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Int)

    // Medical Records
    @Query("SELECT * FROM medical_records ORDER BY timestamp DESC, id DESC")
    fun getAllMedicalRecords(): Flow<List<MedicalRecordEntity>>

    @Query("DELETE FROM medical_records")
    suspend fun clearAllMedicalRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalRecords(records: List<MedicalRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalRecord(record: MedicalRecordEntity)

    @Update
    suspend fun updateMedicalRecord(record: MedicalRecordEntity)

    @Query("DELETE FROM medical_records WHERE id = :id")
    suspend fun deleteMedicalRecordById(id: Int)

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY id ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("DELETE FROM appointments")
    suspend fun clearAllAppointments()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointmentById(id: Int)

    // Emergency Profile
    @Query("SELECT * FROM emergency_profile WHERE id = 1")
    fun getEmergencyProfile(): Flow<EmergencyProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEmergencyProfile(profile: EmergencyProfileEntity)
}
