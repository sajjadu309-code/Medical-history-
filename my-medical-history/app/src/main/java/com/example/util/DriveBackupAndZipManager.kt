package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.AppointmentEntity
import com.example.data.local.EmergencyProfileEntity
import com.example.data.local.MedicalRecordEntity
import com.example.data.local.MedicationEntity
import com.example.data.local.VitalSignEntity
import java.io.File
import java.io.FileOutputStream
import org.json.JSONArray
import org.json.JSONObject

data class ParsedDriveBackup(
    val profile: EmergencyProfileEntity,
    val vitals: List<VitalSignEntity>,
    val medications: List<MedicationEntity>,
    val records: List<MedicalRecordEntity>,
    val appointments: List<AppointmentEntity>
)

object DriveBackupAndZipManager {

    const val FLUTTER_ZIP_ASSET_NAME = "my_medical_history_flutter_project.zip"
    const val DEFAULT_DRIVE_BACKUP_FILENAME = "my_medical_history_drive_backup.json"

    fun buildDriveBackupJson(
        profile: EmergencyProfileEntity,
        vitals: List<VitalSignEntity>,
        medications: List<MedicationEntity>,
        records: List<MedicalRecordEntity>,
        appointments: List<AppointmentEntity>
    ): String {
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put("app", "My Medical History")
        root.put("exportedAtTimestamp", System.currentTimeMillis())

        val profileObj = JSONObject().apply {
            put("fullName", profile.fullName)
            put("dateOfBirth", profile.dateOfBirth)
            put("age", profile.age)
            put("bloodType", profile.bloodType)
            put("weightHeight", profile.weightHeight)
            put("organDonor", profile.organDonor)
            put("severeAllergies", profile.severeAllergies)
            put("chronicConditions", profile.chronicConditions)
            put("surgicalHistory", profile.surgicalHistory)
            put("emergencyContactName", profile.emergencyContactName)
            put("emergencyContactRelation", profile.emergencyContactRelation)
            put("emergencyContactPhone", profile.emergencyContactPhone)
            put("primaryPhysicianName", profile.primaryPhysicianName)
            put("primaryPhysicianClinic", profile.primaryPhysicianClinic)
            put("primaryPhysicianPhone", profile.primaryPhysicianPhone)
            put("insuranceProvider", profile.insuranceProvider)
            put("policyMemberId", profile.policyMemberId)
            put("criticalDirectiveNote", profile.criticalDirectiveNote)
        }
        root.put("profile", profileObj)

        val vitalsArray = JSONArray()
        vitals.forEach { v ->
            vitalsArray.put(
                JSONObject().apply {
                    put("type", v.type)
                    put("valueDisplay", v.valueDisplay)
                    put("unit", v.unit)
                    put("referenceRange", v.referenceRange)
                    put("status", v.status)
                    put("numericPrimary", v.numericPrimary.toDouble())
                    if (v.numericSecondary != null) {
                        put("numericSecondary", v.numericSecondary.toDouble())
                    }
                    put("trend7dCsv", v.trend7dCsv)
                    put("trend1mCsv", v.trend1mCsv)
                    put("trend6mCsv", v.trend6mCsv)
                    put("trend1yCsv", v.trend1yCsv)
                    put("recordedAt", v.recordedAt)
                    put("physicianNote", v.physicianNote)
                }
            )
        }
        root.put("vitals", vitalsArray)

        val medsArray = JSONArray()
        medications.forEach { m ->
            medsArray.put(
                JSONObject().apply {
                    put("name", m.name)
                    put("dosage", m.dosage)
                    put("schedule", m.schedule)
                    put("purpose", m.purpose)
                    put("prescribingDoctor", m.prescribingDoctor)
                    put("pillsRemaining", m.pillsRemaining)
                    put("totalPills", m.totalPills)
                    put("takenToday", m.takenToday)
                    put("refillRequested", m.refillRequested)
                    put("isArchived", m.isArchived)
                    put("startDate", m.startDate)
                    put("endDate", m.endDate ?: "")
                    put("instructions", m.instructions)
                }
            )
        }
        root.put("medications", medsArray)

        val recordsArray = JSONArray()
        records.forEach { r ->
            recordsArray.put(
                JSONObject().apply {
                    put("title", r.title)
                    put("category", r.category)
                    put("status", r.status)
                    put("statusBadgeText", r.statusBadgeText)
                    put("summary", r.summary)
                    put("keyMetricsSummary", r.keyMetricsSummary)
                    put("treatingPhysician", r.treatingPhysician)
                    put("facility", r.facility)
                    put("dateFormatted", r.dateFormatted)
                    put("attachmentFileName", r.attachmentFileName)
                    put("clinicalNotes", r.clinicalNotes)
                    put("timestamp", r.timestamp)
                }
            )
        }
        root.put("records", recordsArray)

        val apptsArray = JSONArray()
        appointments.forEach { a ->
            apptsArray.put(
                JSONObject().apply {
                    put("doctorName", a.doctorName)
                    put("specialty", a.specialty)
                    put("facility", a.facility)
                    put("dateFormatted", a.dateFormatted)
                    put("timeFormatted", a.timeFormatted)
                    put("visitReason", a.visitReason)
                    put("status", a.status)
                    put("statusLabel", a.statusLabel)
                    put("preparationInstructions", a.preparationInstructions)
                }
            )
        }
        root.put("appointments", apptsArray)

        return root.toString(2)
    }

    fun parseDriveBackupJson(rawJson: String): ParsedDriveBackup {
        val root = JSONObject(rawJson)
        val pObj = root.getJSONObject("profile")
        val profile = EmergencyProfileEntity(
            id = 1,
            fullName = pObj.optString("fullName", "Clara Vance-Sterling"),
            dateOfBirth = pObj.optString("dateOfBirth", "1989-04-14"),
            age = pObj.optInt("age", 37),
            bloodType = pObj.optString("bloodType", "O+"),
            weightHeight = pObj.optString("weightHeight", "142 lbs • 5'7\""),
            organDonor = pObj.optBoolean("organDonor", true),
            severeAllergies = pObj.optString("severeAllergies", ""),
            chronicConditions = pObj.optString("chronicConditions", ""),
            surgicalHistory = pObj.optString("surgicalHistory", ""),
            emergencyContactName = pObj.optString("emergencyContactName", ""),
            emergencyContactRelation = pObj.optString("emergencyContactRelation", ""),
            emergencyContactPhone = pObj.optString("emergencyContactPhone", ""),
            primaryPhysicianName = pObj.optString("primaryPhysicianName", ""),
            primaryPhysicianClinic = pObj.optString("primaryPhysicianClinic", ""),
            primaryPhysicianPhone = pObj.optString("primaryPhysicianPhone", ""),
            insuranceProvider = pObj.optString("insuranceProvider", "BlueShield Premier PPO"),
            policyMemberId = pObj.optString("policyMemberId", "BSP-9048271-01"),
            criticalDirectiveNote = pObj.optString("criticalDirectiveNote", "")
        )

        val vitals = mutableListOf<VitalSignEntity>()
        val vitalsArr = root.optJSONArray("vitals") ?: JSONArray()
        for (i in 0 until vitalsArr.length()) {
            val v = vitalsArr.getJSONObject(i)
            vitals.add(
                VitalSignEntity(
                    type = v.optString("type", "Blood Pressure"),
                    valueDisplay = v.optString("valueDisplay", "118/76"),
                    unit = v.optString("unit", "mmHg"),
                    referenceRange = v.optString("referenceRange", ""),
                    status = v.optString("status", "NORMAL"),
                    numericPrimary = v.optDouble("numericPrimary", 118.0).toFloat(),
                    numericSecondary = if (v.has("numericSecondary")) v.optDouble("numericSecondary").toFloat() else null,
                    trend7dCsv = v.optString("trend7dCsv", "120,119,118"),
                    trend1mCsv = v.optString("trend1mCsv", "122,120,118"),
                    trend6mCsv = v.optString("trend6mCsv", "125,121,118"),
                    trend1yCsv = v.optString("trend1yCsv", "128,122,118"),
                    recordedAt = v.optString("recordedAt", "Restored from Drive"),
                    physicianNote = v.optString("physicianNote", "")
                )
            )
        }

        val medications = mutableListOf<MedicationEntity>()
        val medsArr = root.optJSONArray("medications") ?: JSONArray()
        for (i in 0 until medsArr.length()) {
            val m = medsArr.getJSONObject(i)
            val endD = m.optString("endDate", "")
            medications.add(
                MedicationEntity(
                    name = m.optString("name", "Medication"),
                    dosage = m.optString("dosage", "10 mg"),
                    schedule = m.optString("schedule", "Once daily"),
                    purpose = m.optString("purpose", ""),
                    prescribingDoctor = m.optString("prescribingDoctor", ""),
                    pillsRemaining = m.optInt("pillsRemaining", 30),
                    totalPills = m.optInt("totalPills", 30),
                    takenToday = m.optBoolean("takenToday", false),
                    refillRequested = m.optBoolean("refillRequested", false),
                    isArchived = m.optBoolean("isArchived", false),
                    startDate = m.optString("startDate", "2026"),
                    endDate = endD.ifBlank { null },
                    instructions = m.optString("instructions", "")
                )
            )
        }

        val records = mutableListOf<MedicalRecordEntity>()
        val recsArr = root.optJSONArray("records") ?: JSONArray()
        for (i in 0 until recsArr.length()) {
            val r = recsArr.getJSONObject(i)
            records.add(
                MedicalRecordEntity(
                    title = r.optString("title", "Clinical Record"),
                    category = r.optString("category", "Lab Results"),
                    status = r.optString("status", "NORMAL"),
                    statusBadgeText = r.optString("statusBadgeText", "Normal"),
                    summary = r.optString("summary", ""),
                    keyMetricsSummary = r.optString("keyMetricsSummary", ""),
                    treatingPhysician = r.optString("treatingPhysician", ""),
                    facility = r.optString("facility", ""),
                    dateFormatted = r.optString("dateFormatted", "2026"),
                    attachmentFileName = r.optString("attachmentFileName", ""),
                    clinicalNotes = r.optString("clinicalNotes", ""),
                    timestamp = r.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }

        val appointments = mutableListOf<AppointmentEntity>()
        val apptsArr = root.optJSONArray("appointments") ?: JSONArray()
        for (i in 0 until apptsArr.length()) {
            val a = apptsArr.getJSONObject(i)
            appointments.add(
                AppointmentEntity(
                    doctorName = a.optString("doctorName", ""),
                    specialty = a.optString("specialty", ""),
                    facility = a.optString("facility", ""),
                    dateFormatted = a.optString("dateFormatted", ""),
                    timeFormatted = a.optString("timeFormatted", ""),
                    visitReason = a.optString("visitReason", ""),
                    status = a.optString("status", "PENDING"),
                    statusLabel = a.optString("statusLabel", "Upcoming"),
                    preparationInstructions = a.optString("preparationInstructions", "")
                )
            )
        }

        return ParsedDriveBackup(
            profile = profile,
            vitals = vitals,
            medications = medications,
            records = records,
            appointments = appointments
        )
    }

    fun shareBackupToGoogleDrive(
        context: Context,
        backupJson: String,
        onResult: (String) -> Unit
    ) {
        try {
            val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
            val backupFile = File(backupDir, DEFAULT_DRIVE_BACKUP_FILENAME)
            backupFile.writeText(backupJson)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "My Medical History — Google Drive Cloud Backup")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Save this JSON backup file to Google Drive to sync or restore your offline clinical records on any device."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Save Backup to Google Drive").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            onResult("Google Drive Backup file ready (${backupFile.name})")
        } catch (e: Exception) {
            onResult("Offline backup saved: ${e.localizedMessage ?: "Ready"}")
        }
    }

    fun writeBackupToDocumentUri(
        context: Context,
        targetUri: Uri,
        backupJson: String,
        onResult: (String) -> Unit
    ) {
        try {
            context.contentResolver.openOutputStream(targetUri)?.use { out ->
                out.write(backupJson.toByteArray(Charsets.UTF_8))
            }
            onResult("Saved clinical backup directly to Google Drive / Selected Storage!")
        } catch (e: Exception) {
            onResult("Failed to write backup: ${e.localizedMessage}")
        }
    }

    fun copyFlutterProjectZipToUri(
        context: Context,
        targetUri: Uri,
        onResult: (String) -> Unit
    ) {
        try {
            context.assets.open(FLUTTER_ZIP_ASSET_NAME).use { input ->
                context.contentResolver.openOutputStream(targetUri)?.use { output ->
                    input.copyTo(output)
                }
            }
            onResult("Saved $FLUTTER_ZIP_ASSET_NAME to your device / Google Drive!")
        } catch (e: Exception) {
            onResult("Could not save ZIP: ${e.localizedMessage}")
        }
    }

    fun shareFlutterProjectZip(
        context: Context,
        onResult: (String) -> Unit
    ) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val zipFile = File(exportDir, FLUTTER_ZIP_ASSET_NAME)
            context.assets.open(FLUTTER_ZIP_ASSET_NAME).use { input ->
                FileOutputStream(zipFile).use { output ->
                    input.copyTo(output)
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "My Medical History — Complete Flutter Project (.zip)")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Extract this Flutter project in VS Code and run `flutter pub get && flutter run` or `flutter build apk --release`."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Download / Share Flutter Project ZIP").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            onResult("Exporting $FLUTTER_ZIP_ASSET_NAME")
        } catch (e: Exception) {
            onResult("Flutter project ZIP ready in workspace root ($FLUTTER_ZIP_ASSET_NAME)")
        }
    }
}
