package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LabResultEntity
import com.example.data.local.MedicalDao
import com.example.data.local.MedicalDatabase
import com.example.data.local.MedicationEntity
import com.example.data.local.SeedClinicalData
import com.example.data.local.VitalSignEntity
import com.example.data.repository.MedicalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: MedicalDatabase
    private lateinit var dao: MedicalDao
    private lateinit var repository: MedicalRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            MedicalDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = database.medicalDao()
        repository = MedicalRepository(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("My Medical History", appName)
    }

    @Test
    fun `room database seeds and queries vitals medications and lab results`() = runTest {
        repository.ensureSeeded()

        val vitals = repository.vitalSigns.first()
        val medications = repository.medications.first()
        val labResults = repository.labResults.first()
        val records = repository.medicalRecords.first()
        val profile = repository.emergencyProfileFlow.first()

        assertTrue(vitals.isNotEmpty())
        assertEquals("Blood Pressure", vitals.first().type)
        assertEquals("118/76", vitals.first().valueDisplay)

        assertTrue(medications.isNotEmpty())
        assertTrue(medications.any { it.name == "Lisinopril" && !it.isArchived })
        assertTrue(medications.any { it.isArchived })

        assertTrue(labResults.isNotEmpty())
        assertTrue(labResults.any { it.biomarkerName.contains("HbA1c") })

        assertTrue(records.isNotEmpty())
        assertNotNull(profile)
        assertEquals("O+", profile?.bloodType)
    }

    @Test
    fun `insert and update vital signs medications and lab results in room`() = runTest {
        // 1. Insert & update VitalSignEntity
        val vitalId = dao.insertVitalSign(
            VitalSignEntity(
                type = "Body Temperature",
                valueDisplay = "98.6",
                unit = "°F",
                referenceRange = "97.8 – 99.1 °F",
                status = "NORMAL",
                numericPrimary = 98.6f,
                trend7dCsv = "98.4,98.5,98.6",
                trend1mCsv = "98.4,98.5,98.6",
                trend6mCsv = "98.4,98.5,98.6",
                trend1yCsv = "98.4,98.5,98.6",
                recordedAt = "Today",
                physicianNote = "Afebrile"
            )
        ).toInt()

        val storedVital = dao.getVitalSignsByType("Body Temperature").first().first()
        assertEquals(vitalId, storedVital.id)
        assertEquals("98.6", storedVital.valueDisplay)

        // 2. Insert & toggle MedicationEntity adherence
        val medId = dao.insertMedication(
            MedicationEntity(
                name = "Atorvastatin",
                dosage = "20 mg",
                schedule = "Once daily",
                purpose = "Cholesterol management",
                prescribingDoctor = "Dr. Elena Rostova, MD",
                pillsRemaining = 6,
                totalPills = 30,
                takenToday = false,
                startDate = "Oct 08, 2026",
                instructions = "Take in the evening"
            )
        ).toInt()

        val medBefore = dao.getActiveMedications().first().first { it.id == medId }
        assertFalse(medBefore.takenToday)
        assertEquals(6, medBefore.pillsRemaining)

        repository.toggleMedicationAdherence(medBefore)
        val medAfter = dao.getActiveMedications().first().first { it.id == medId }
        assertTrue(medAfter.takenToday)
        assertEquals(5, medAfter.pillsRemaining)

        // Verify low supply query
        val lowSupply = dao.getLowSupplyMedications(threshold = 7).first()
        assertTrue(lowSupply.any { it.id == medId })

        // 3. Insert & query LabResultEntity
        dao.insertLabResult(
            LabResultEntity(
                biomarkerName = "TSH (Thyroid Stimulating Hormone)",
                panelName = "Thyroid Function Panel",
                measuredValue = 1.85f,
                valueDisplay = "1.85",
                unit = "mIU/L",
                referenceMin = 0.45f,
                referenceMax = 4.50f,
                referenceRangeLabel = "0.45 – 4.50 mIU/L",
                flagStatus = "NORMAL",
                flagLabel = "Optimal",
                orderingPhysician = "Dr. Elena Rostova, MD",
                labFacility = "Quest Clinical Laboratories",
                collectedDate = "Oct 08, 2026",
                clinicalInterpretation = "Euthyroid status confirmed."
            )
        )

        val thyroidLabs = dao.getLabResultsByPanel("Thyroid Function Panel").first()
        assertEquals(1, thyroidLabs.size)
        assertEquals(1.85f, thyroidLabs.first().measuredValue, 0.01f)
    }
}
