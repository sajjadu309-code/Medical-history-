package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ClinicalStatus(val label: String) {
    NORMAL("Normal"),
    ADHERENT("Adherent"),
    PENDING("Pending"),
    REFILL_ALERT("Refill Due"),
    CRITICAL("Critical"),
    ALLERGY("Severe Allergy")
}

@Entity(tableName = "vital_signs")
data class VitalSignEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,               // e.g., "Blood Pressure", "Heart Rate", "Blood Glucose", "Body Temp"
    val valueDisplay: String,       // e.g., "118/76", "68", "94", "98.4"
    val unit: String,               // e.g., "mmHg", "bpm", "mg/dL", "°F"
    val referenceRange: String,     // e.g., "Optimal < 120/80 mmHg"
    val status: String,             // NORMAL, PENDING, CRITICAL
    val numericPrimary: Float,      // Used for trend chart plotting
    val numericSecondary: Float? = null,
    val trend7dCsv: String,         // Comma-separated 7-day trend points
    val trend1mCsv: String,         // Comma-separated 1-month trend points
    val trend6mCsv: String,         // Comma-separated 6-month trend points
    val trend1yCsv: String,         // Comma-separated 1-year trend points
    val recordedAt: String,         // e.g., "Today, 08:15 AM"
    val physicianNote: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,               // e.g., "Lisinopril"
    val dosage: String,             // e.g., "10 mg"
    val schedule: String,           // e.g., "Once daily • 08:00 AM with water"
    val purpose: String,            // e.g., "Essential Hypertension Maintenance"
    val prescribingDoctor: String,  // e.g., "Dr. Elena Rostova, MD"
    val pillsRemaining: Int,        // e.g., 5 (triggers Refill Alert if <= 7)
    val totalPills: Int,            // e.g., 30
    val takenToday: Boolean,        // Controlled by the 24x24dp #10B981 Adherence Checkbox
    val refillRequested: Boolean = false,
    @ColumnInfo(name = "isArchived")
    val isArchived: Boolean = false,// false = Active Medications, true = Historical Archive
    val startDate: String,
    val endDate: String? = null,
    val instructions: String
)

@Entity(tableName = "medical_records")
data class MedicalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,              // e.g., "Comprehensive Metabolic & Lipid Panel"
    val category: String,           // "Lab Results", "Diagnoses", "Immunizations", "Imaging", "Procedures"
    val status: String,             // "NORMAL", "PENDING", "CRITICAL"
    val statusBadgeText: String,    // e.g., "Normal Range", "Pending Review", "Critical Flag"
    val summary: String,            // Multi-line Inter body description
    val keyMetricsSummary: String,  // e.g., "HbA1c: 5.3% • LDL: 88 mg/dL • eGFR: 96"
    val treatingPhysician: String,  // e.g., "Dr. Elena Rostova, MD (Cardiology)"
    val facility: String,           // e.g., "Pacific Clinical Diagnostics Center"
    val dateFormatted: String,      // e.g., "Oct 04, 2026"
    val attachmentFileName: String, // e.g., "CMP_Lipid_Panel_Oct2026.pdf"
    val clinicalNotes: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorName: String,
    val specialty: String,
    val facility: String,
    val dateFormatted: String,
    val timeFormatted: String,
    val visitReason: String,
    val status: String,             // "PENDING", "NORMAL"
    val statusLabel: String,        // "Upcoming in 3 Days", "Completed", "Pre-Visit Labs Due"
    val preparationInstructions: String
)

@Entity(tableName = "emergency_profile")
data class EmergencyProfileEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String,
    val dateOfBirth: String,
    val age: Int,
    val bloodType: String,
    val weightHeight: String,
    val organDonor: Boolean,
    val severeAllergies: String,    // Pipe-separated e.g. "Penicillin (Anaphylaxis - Severe)|Latex (Acute Contact Urticaria)|Sulfonamides (Bronchospasm)"
    val chronicConditions: String,  // Pipe-separated
    val surgicalHistory: String,    // Pipe-separated
    val emergencyContactName: String,
    val emergencyContactRelation: String,
    val emergencyContactPhone: String,
    val primaryPhysicianName: String,
    val primaryPhysicianClinic: String,
    val primaryPhysicianPhone: String,
    val insuranceProvider: String,
    val policyMemberId: String,
    val criticalDirectiveNote: String
)

@Entity(tableName = "lab_results")
data class LabResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val biomarkerName: String,
    val panelName: String,
    val measuredValue: Float,
    val valueDisplay: String,
    val unit: String,
    val referenceMin: Float,
    val referenceMax: Float,
    val referenceRangeLabel: String,
    val flagStatus: String,
    val flagLabel: String,
    val orderingPhysician: String,
    val labFacility: String,
    val collectedDate: String,
    val clinicalInterpretation: String,
    val timestamp: Long = System.currentTimeMillis()
)
