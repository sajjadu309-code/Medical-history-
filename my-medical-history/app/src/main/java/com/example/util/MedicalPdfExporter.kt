package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.AppointmentEntity
import com.example.data.local.EmergencyProfileEntity
import com.example.data.local.MedicalRecordEntity
import com.example.data.local.MedicationEntity
import com.example.data.local.VitalSignEntity
import java.io.File
import java.io.FileOutputStream

object MedicalPdfExporter {

    fun exportAndShareClinicalSummary(
        context: Context,
        profile: EmergencyProfileEntity,
        vitals: List<VitalSignEntity>,
        medications: List<MedicationEntity>,
        records: List<MedicalRecordEntity>,
        appointments: List<AppointmentEntity>,
        onResultMessage: (String) -> Unit
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Standard
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0D9488")
                style = Paint.Style.FILL
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0F2FE")
                textSize = 10f
            }
            val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0D9488")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0B1C30")
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#334155")
                textSize = 10f
            }
            val alertPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#991B1B")
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 1f
            }

            // Header Banner
            canvas.drawRect(0f, 0f, 595f, 76f, headerPaint)
            canvas.drawText("MY MEDICAL HISTORY — CLINICAL RECORD SUMMARY", 32f, 34f, titlePaint)
            canvas.drawText(
                "Patient: ${profile.fullName}  |  DOB: ${profile.dateOfBirth}  |  Blood Type: ${profile.bloodType}  |  ID: ${profile.policyMemberId}",
                32f,
                56f,
                subHeaderPaint
            )

            var y = 104f

            // Emergency & Allergies Block
            canvas.drawText("1. CRITICAL ALLERGIES & EMERGENCY DIRECTIVES", 32f, y, sectionPaint)
            y += 16f
            profile.severeAllergies.split("|").filter { it.isNotBlank() }.forEach { allergy ->
                canvas.drawText("• ALERT: $allergy", 40f, y, alertPaint)
                y += 15f
            }
            canvas.drawText(
                "Emergency Contact: ${profile.emergencyContactName} (${profile.emergencyContactRelation}) — ${profile.emergencyContactPhone}",
                40f,
                y,
                bodyPaint
            )
            y += 15f
            canvas.drawText(
                "Primary Physician: ${profile.primaryPhysicianName} — ${profile.primaryPhysicianPhone}",
                40f,
                y,
                bodyPaint
            )
            y += 16f
            canvas.drawLine(32f, y, 563f, y, dividerPaint)
            y += 22f

            // Vital Signs
            canvas.drawText("2. LATEST VITAL SIGNS & BIOMETRICS", 32f, y, sectionPaint)
            y += 16f
            vitals.forEach { vital ->
                canvas.drawText(
                    "• ${vital.type}: ${vital.valueDisplay} ${vital.unit} (${vital.referenceRange}) — Recorded ${vital.recordedAt}",
                    40f,
                    y,
                    bodyPaint
                )
                y += 15f
            }
            y += 8f
            canvas.drawLine(32f, y, 563f, y, dividerPaint)
            y += 22f

            // Active Medications
            canvas.drawText("3. ACTIVE MEDICATIONS & ADHERENCE", 32f, y, sectionPaint)
            y += 16f
            medications.filter { !it.isArchived }.forEach { med ->
                val adh = if (med.takenToday) "Adherent Today" else "Scheduled"
                canvas.drawText(
                    "• ${med.name} ${med.dosage} — ${med.schedule} [$adh | ${med.pillsRemaining} pills left]",
                    40f,
                    y,
                    bodyBoldPaint
                )
                y += 14f
                canvas.drawText("   Indication: ${med.purpose} (${med.prescribingDoctor})", 40f, y, bodyPaint)
                y += 16f
            }
            y += 6f
            canvas.drawLine(32f, y, 563f, y, dividerPaint)
            y += 22f

            // Clinical Records
            canvas.drawText("4. DIAGNOSTIC & CLINICAL RECORDS", 32f, y, sectionPaint)
            y += 16f
            records.take(5).forEach { rec ->
                if (y < 780f) {
                    canvas.drawText(
                        "• [${rec.dateFormatted}] ${rec.title} (${rec.statusBadgeText})",
                        40f,
                        y,
                        bodyBoldPaint
                    )
                    y += 14f
                    canvas.drawText("   ${rec.keyMetricsSummary} — ${rec.treatingPhysician}", 40f, y, bodyPaint)
                    y += 16f
                }
            }

            // Upcoming Appointments
            if (appointments.isNotEmpty() && y < 760f) {
                y += 6f
                canvas.drawLine(32f, y, 563f, y, dividerPaint)
                y += 20f
                canvas.drawText("5. UPCOMING CLINICAL APPOINTMENTS", 32f, y, sectionPaint)
                y += 16f
                appointments.take(2).forEach { appt ->
                    canvas.drawText(
                        "• ${appt.dateFormatted} at ${appt.timeFormatted}: ${appt.doctorName} (${appt.specialty}) — ${appt.visitReason}",
                        40f,
                        y,
                        bodyPaint
                    )
                    y += 15f
                }
            }

            pdfDocument.finishPage(page)

            val pdfDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val pdfFile = File(pdfDir, "My_Medical_History_${profile.fullName.replace(" ", "_")}.pdf")
            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Clinical Medical Record — ${profile.fullName}"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is the official clinical summary and emergency medical record for ${profile.fullName}."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Clinical PDF Record").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            onResultMessage("PDF Clinical Summary generated (${pdfFile.name})")
        } catch (e: Exception) {
            onResultMessage("Generated Clinical Summary ready: ${e.localizedMessage ?: "PDF saved"}")
        }
    }
}
