package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        VitalSignEntity::class,
        MedicationEntity::class,
        LabResultEntity::class,
        MedicalRecordEntity::class,
        AppointmentEntity::class,
        EmergencyProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(MedicalConverters::class)
abstract class MedicalDatabase : RoomDatabase() {
    abstract fun medicalDao(): MedicalDao

    companion object {
        @Volatile
        private var INSTANCE: MedicalDatabase? = null

        fun getDatabase(context: Context): MedicalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalDatabase::class.java,
                    "my_medical_history.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object SeedClinicalData {
    val defaultEmergencyProfile = EmergencyProfileEntity(
        id = 1,
        fullName = "Clara Vance-Sterling",
        dateOfBirth = "1989-04-14",
        age = 37,
        bloodType = "O+",
        weightHeight = "142 lbs • 5'7\" (170 cm)",
        organDonor = true,
        severeAllergies = "Penicillin (Anaphylaxis • Epinephrine Auto-Injector Carried)|Latex (Acute Contact Urticaria & Airway Edema)|Sulfonamide Antibiotics (Severe Rash & Fever)",
        chronicConditions = "Stage 1 Essential Hypertension (Controlled)|Seasonal Allergic Asthma (Mild Intermittent)|Iron-Deficiency Anemia (Resolved 2025)",
        surgicalHistory = "Laparoscopic Appendectomy (May 2019 • St. Jude Medical)|Wisdom Teeth Extraction (Aug 2014)",
        emergencyContactName = "Marcus Sterling",
        emergencyContactRelation = "Spouse / Healthcare Proxy",
        emergencyContactPhone = "+1 (415) 890-4321",
        primaryPhysicianName = "Dr. Elena Rostova, MD",
        primaryPhysicianClinic = "Pacific Coastal Internal Medicine & Cardiology",
        primaryPhysicianPhone = "+1 (415) 622-9100",
        insuranceProvider = "BlueShield Premier Clinical PPO",
        policyMemberId = "BSP-9048271-01",
        criticalDirectiveNote = "Patient carries EpiPen 0.3mg in handbag. Avoid all beta-lactam antibiotics and latex gloves during acute intervention."
    )

    val defaultVitalSigns = listOf(
        VitalSignEntity(
            type = "Blood Pressure",
            valueDisplay = "118/76",
            unit = "mmHg",
            referenceRange = "Optimal < 120/80 mmHg",
            status = "NORMAL",
            numericPrimary = 118f,
            numericSecondary = 76f,
            trend7dCsv = "122,120,119,121,118,117,118",
            trend1mCsv = "128,125,124,122,120,119,118",
            trend6mCsv = "134,131,128,124,121,119,118",
            trend1yCsv = "138,135,131,127,123,120,118",
            recordedAt = "Today, 08:15 AM",
            physicianNote = "Consistent downward trend following 10mg Lisinopril adherence."
        ),
        VitalSignEntity(
            type = "Heart Rate",
            valueDisplay = "68",
            unit = "bpm",
            referenceRange = "Resting 60–100 bpm",
            status = "NORMAL",
            numericPrimary = 68f,
            trend7dCsv = "71,70,69,72,68,67,68",
            trend1mCsv = "74,73,71,70,69,68,68",
            trend6mCsv = "76,75,73,71,70,69,68",
            trend1yCsv = "78,76,74,72,71,69,68",
            recordedAt = "Today, 08:15 AM",
            physicianNote = "Regular sinus rhythm, strong resting vagal tone."
        ),
        VitalSignEntity(
            type = "Blood Glucose",
            valueDisplay = "94",
            unit = "mg/dL",
            referenceRange = "Fasting 70–99 mg/dL",
            status = "NORMAL",
            numericPrimary = 94f,
            trend7dCsv = "96,95,92,98,94,93,94",
            trend1mCsv = "99,97,95,96,94,93,94",
            trend6mCsv = "102,100,98,96,95,94,94",
            trend1yCsv = "104,101,99,97,95,94,94",
            recordedAt = "Today, 07:45 AM",
            physicianNote = "Fasting capillary measurement within optimal metabolic target."
        ),
        VitalSignEntity(
            type = "Blood Oxygen",
            valueDisplay = "99",
            unit = "% SpO2",
            referenceRange = "Normal 95–100%",
            status = "NORMAL",
            numericPrimary = 99f,
            trend7dCsv = "98,99,99,98,99,99,99",
            trend1mCsv = "98,98,99,99,98,99,99",
            trend6mCsv = "97,98,98,99,99,99,99",
            trend1yCsv = "97,98,98,98,99,99,99",
            recordedAt = "Yesterday, 09:00 PM",
            physicianNote = "Clear pulmonary oxygenation on room air."
        )
    )

    val defaultMedications = listOf(
        MedicationEntity(
            name = "Lisinopril",
            dosage = "10 mg",
            schedule = "Once daily • 08:00 AM",
            purpose = "Blood Pressure & Arterial Protection",
            prescribingDoctor = "Dr. Elena Rostova, MD",
            pillsRemaining = 5,
            totalPills = 30,
            takenToday = true,
            refillRequested = false,
            isArchived = false,
            startDate = "Jan 12, 2026",
            instructions = "Take 1 tablet by mouth every morning with a full glass of water. Monitor blood pressure weekly."
        ),
        MedicationEntity(
            name = "Cholecalciferol (Vitamin D3)",
            dosage = "2000 IU",
            schedule = "Once daily • With breakfast",
            purpose = "Bone Density & Immune Support",
            prescribingDoctor = "Dr. Elena Rostova, MD",
            pillsRemaining = 22,
            totalPills = 60,
            takenToday = true,
            refillRequested = false,
            isArchived = false,
            startDate = "Mar 01, 2026",
            instructions = "Take 1 softgel daily with a meal containing healthy fats for optimal absorption."
        ),
        MedicationEntity(
            name = "Montelukast Sodium",
            dosage = "10 mg",
            schedule = "Once daily • 09:00 PM Evening",
            purpose = "Seasonal Allergic Asthma Prophylaxis",
            prescribingDoctor = "Dr. Aris Thorne, MD (Pulmonology)",
            pillsRemaining = 18,
            totalPills = 30,
            takenToday = false,
            refillRequested = false,
            isArchived = false,
            startDate = "Sep 15, 2026",
            instructions = "Take 1 tablet in the evening to prevent nighttime bronchial constriction and seasonal rhinitis."
        ),
        MedicationEntity(
            name = "Magnesium Glycinate",
            dosage = "400 mg",
            schedule = "Once daily • 09:30 PM Bedtime",
            purpose = "Vascular Relaxation & Sleep Recovery",
            prescribingDoctor = "Dr. Elena Rostova, MD",
            pillsRemaining = 6,
            totalPills = 30,
            takenToday = false,
            refillRequested = false,
            isArchived = false,
            startDate = "Apr 10, 2026",
            instructions = "Take 2 chelated capsules 30 minutes prior to sleep."
        ),
        MedicationEntity(
            name = "Ferrous Bisglycinate",
            dosage = "28 mg",
            schedule = "Completed Course • Every other day",
            purpose = "Ferritin Restoration Protocol",
            prescribingDoctor = "Dr. Maya Lin, MD (Hematology)",
            pillsRemaining = 0,
            totalPills = 90,
            takenToday = false,
            refillRequested = false,
            isArchived = true,
            startDate = "Jun 01, 2025",
            endDate = "Dec 18, 2025",
            instructions = "Discontinued after serum ferritin normalized to 78 ng/mL."
        ),
        MedicationEntity(
            name = "Azithromycin (Z-Pak)",
            dosage = "250 mg",
            schedule = "Completed 5-Day Regimen",
            purpose = "Acute Bacterial Sinusitis",
            prescribingDoctor = "Dr. Aris Thorne, MD",
            pillsRemaining = 0,
            totalPills = 6,
            takenToday = false,
            refillRequested = false,
            isArchived = true,
            startDate = "Nov 02, 2025",
            endDate = "Nov 07, 2025",
            instructions = "Safe macrolide alternative utilized due to documented severe Penicillin allergy."
        )
    )

    val defaultLabResults = listOf(
        LabResultEntity(
            biomarkerName = "Hemoglobin A1c (HbA1c)",
            panelName = "Comprehensive Metabolic & Lipid Panel",
            measuredValue = 5.3f,
            valueDisplay = "5.3",
            unit = "%",
            referenceMin = 4.0f,
            referenceMax = 5.6f,
            referenceRangeLabel = "4.0 – 5.6 %",
            flagStatus = "NORMAL",
            flagLabel = "Optimal",
            orderingPhysician = "Dr. Elena Rostova, MD",
            labFacility = "Quest Clinical Laboratories",
            collectedDate = "Sep 28, 2026",
            clinicalInterpretation = "Optimal 90-day glycemic control; well below pre-diabetic threshold (5.7%).",
            timestamp = 1790600000000L
        ),
        LabResultEntity(
            biomarkerName = "LDL Cholesterol (Direct)",
            panelName = "Comprehensive Metabolic & Lipid Panel",
            measuredValue = 88f,
            valueDisplay = "88",
            unit = "mg/dL",
            referenceMin = 0f,
            referenceMax = 100f,
            referenceRangeLabel = "< 100 mg/dL",
            flagStatus = "NORMAL",
            flagLabel = "Normal",
            orderingPhysician = "Dr. Elena Rostova, MD",
            labFacility = "Quest Clinical Laboratories",
            collectedDate = "Sep 28, 2026",
            clinicalInterpretation = "Reduced by 14% over 6 months with Mediterranean diet protocol.",
            timestamp = 1790600000000L
        ),
        LabResultEntity(
            biomarkerName = "Serum Ferritin",
            panelName = "Hematology & Iron Storage Panel",
            measuredValue = 78f,
            valueDisplay = "78",
            unit = "ng/mL",
            referenceMin = 30f,
            referenceMax = 200f,
            referenceRangeLabel = "30 – 200 ng/mL",
            flagStatus = "NORMAL",
            flagLabel = "Normal",
            orderingPhysician = "Dr. Maya Lin, MD",
            labFacility = "Pacific Clinical Diagnostics Center",
            collectedDate = "Sep 28, 2026",
            clinicalInterpretation = "Iron stores fully replenished following 6-month Ferrous Bisglycinate course.",
            timestamp = 1790500000000L
        ),
        LabResultEntity(
            biomarkerName = "25-Hydroxy Vitamin D",
            panelName = "Endocrine & Micronutrient Panel",
            measuredValue = 31f,
            valueDisplay = "31",
            unit = "ng/mL",
            referenceMin = 30f,
            referenceMax = 100f,
            referenceRangeLabel = "30 – 100 ng/mL",
            flagStatus = "PENDING",
            flagLabel = "Low-Normal",
            orderingPhysician = "Dr. Elena Rostova, MD",
            labFacility = "Quest Clinical Laboratories",
            collectedDate = "Sep 15, 2026",
            clinicalInterpretation = "Near lower bound; continue Cholecalciferol 2000 IU daily and re-test in winter.",
            timestamp = 1789500000000L
        ),
        LabResultEntity(
            biomarkerName = "Penicillin G Specific IgE (RAST)",
            panelName = "Allergy & Immunology RAST Panel",
            measuredValue = 19.4f,
            valueDisplay = "19.4",
            unit = "kU/L",
            referenceMin = 0.0f,
            referenceMax = 0.35f,
            referenceRangeLabel = "< 0.35 kU/L",
            flagStatus = "CRITICAL",
            flagLabel = "Class V High",
            orderingPhysician = "Dr. Aris Thorne, MD",
            labFacility = "Pacific Clinical Diagnostics Center",
            collectedDate = "Oct 02, 2026",
            clinicalInterpretation = "Critical IgE sensitization confirmed. Strict avoidance of all beta-lactam antibiotics required.",
            timestamp = 1791000000000L
        )
    )

    val defaultMedicalRecords = listOf(
        MedicalRecordEntity(
            title = "Severe Drug Allergy Alert: Penicillin & Beta-Lactams",
            category = "Diagnoses",
            status = "CRITICAL",
            statusBadgeText = "Critical Allergy",
            summary = "Confirmed IgE-mediated anaphylactic hypersensitivity to Penicillin and cross-reactive beta-lactam derivatives. Patient carries twin-pack Epinephrine 0.3mg auto-injectors at all times.",
            keyMetricsSummary = "Reaction: Anaphylaxis • RAST Class V • Verified ID Alert",
            treatingPhysician = "Dr. Aris Thorne, MD (Allergy & Immunology)",
            facility = "Pacific Clinical Diagnostics Center",
            dateFormatted = "Oct 02, 2026",
            attachmentFileName = "Allergy_Immunology_Directive_2026.pdf",
            clinicalNotes = "Verified emergency protocol updated in hospital portal. Use macrolides or fluoroquinolones when antimicrobial therapy is indicated.",
            timestamp = 1791000000000L
        ),
        MedicalRecordEntity(
            title = "Comprehensive Metabolic & Lipid Panel (Fasting)",
            category = "Lab Results",
            status = "NORMAL",
            statusBadgeText = "Normal",
            summary = "All hepatic, renal, and glycemic markers within optimal reference bounds. Total cholesterol and LDL-C improved by 14% over the past 6 months.",
            keyMetricsSummary = "HbA1c: 5.3% • LDL: 88 mg/dL • HDL: 64 mg/dL • eGFR: 96",
            treatingPhysician = "Dr. Elena Rostova, MD (Cardiology)",
            facility = "Quest Clinical Laboratories",
            dateFormatted = "Sep 28, 2026",
            attachmentFileName = "CMP_Lipid_Results_Sep2026.pdf",
            clinicalNotes = "Continue current Mediterranean nutrition protocol and 10mg Lisinopril daily.",
            timestamp = 1790600000000L
        ),
        MedicalRecordEntity(
            title = "24-Hour Ambulatory Blood Pressure Holter Review",
            category = "Procedures",
            status = "PENDING",
            statusBadgeText = "Pending Follow-up",
            summary = "Daytime average 119/77 mmHg; nocturnal dipping profile preserved at 106/66 mmHg (11% dip). Scheduled for routine 6-month cardiology review next week.",
            keyMetricsSummary = "24h Mean: 115/73 mmHg • Max: 132/84 • Nocturnal Dip: 11%",
            treatingPhysician = "Dr. Elena Rostova, MD (Cardiology)",
            facility = "Pacific Coastal Heart Institute",
            dateFormatted = "Sep 19, 2026",
            attachmentFileName = "ABPM_Holter_Summary_Sep2026.pdf",
            clinicalNotes = "Bring home blood pressure cuff to upcoming October consultation for calibration check.",
            timestamp = 1789800000000L
        ),
        MedicalRecordEntity(
            title = "High-Resolution Chest Spirometry & FeNO Test",
            category = "Imaging",
            status = "NORMAL",
            statusBadgeText = "Normal",
            summary = "Forced expiratory volume (FEV1/FVC ratio 0.84) demonstrates normal airway caliber with zero baseline obstruction. Fractional exhaled nitric oxide (FeNO) low at 16 ppb.",
            keyMetricsSummary = "FEV1: 102% Pred • FVC: 104% Pred • FeNO: 16 ppb",
            treatingPhysician = "Dr. Aris Thorne, MD (Pulmonology)",
            facility = "Bay Area Pulmonary & Allergy Clinic",
            dateFormatted = "Aug 14, 2026",
            attachmentFileName = "Spirometry_Pulmonary_Aug2026.pdf",
            clinicalNotes = "Seasonal asthma well controlled on evening Montelukast during autumn pollen peak.",
            timestamp = 1786700000000L
        ),
        MedicalRecordEntity(
            title = "Quadrivalent Influenza & Updated Tdap Booster",
            category = "Immunizations",
            status = "NORMAL",
            statusBadgeText = "Completed",
            summary = "Administered 0.5mL intramuscularly in left deltoid (Lot #FL-88412). Zero immediate post-vaccination adverse reaction observed during 20-minute clinical monitoring.",
            keyMetricsSummary = "Vaccine: IIV4 + Tdap • Site: Left Deltoid • Status: Immune",
            treatingPhysician = "Sarah Jenkins, NP-C",
            facility = "Pacific Coastal Internal Medicine",
            dateFormatted = "Jul 22, 2026",
            attachmentFileName = "Immunization_Certificate_2026.pdf",
            clinicalNotes = "Next tetanus booster due July 2036.",
            timestamp = 1784700000000L
        )
    )

    val defaultAppointments = listOf(
        AppointmentEntity(
            doctorName = "Dr. Elena Rostova, MD",
            specialty = "Cardiology & Internal Medicine",
            facility = "Pacific Coastal Heart Institute • Suite 410",
            dateFormatted = "Oct 14, 2026",
            timeFormatted = "10:30 AM",
            visitReason = "Biannual Hypertension & Lipid Panel Consultation",
            status = "PENDING",
            statusLabel = "Upcoming • 6 Days",
            preparationInstructions = "Fast for 8 hours prior if repeat Electrolyte panel is ordered. Bring home BP log."
        ),
        AppointmentEntity(
            doctorName = "Dr. Aris Thorne, MD",
            specialty = "Allergy, Asthma & Immunology",
            facility = "Bay Area Pulmonary & Allergy Clinic",
            dateFormatted = "Nov 09, 2026",
            timeFormatted = "02:15 PM",
            visitReason = "Annual Epinephrine Auto-Injector Renewal & Asthma Check",
            status = "NORMAL",
            statusLabel = "Confirmed",
            preparationInstructions = "Bring current EpiPen twin-pack to verify expiration dates."
        )
    )
}
