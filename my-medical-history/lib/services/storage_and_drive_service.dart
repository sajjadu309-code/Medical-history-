import 'dart:convert';
import 'dart:io';
import 'package:flutter/foundation.dart';
import '../models/medical_models.dart';

/// Handles 100% offline local persistence on the device filesystem and
/// exports/imports full JSON clinical snapshots for Google Drive backup & sync.
class StorageAndDriveService {
  static const String _backupFileName = 'my_medical_history_drive_backup.json';

  Future<File?> _getLocalStoreFile() async {
    if (kIsWeb) return null;
    try {
      final dir = Directory.systemTemp;
      return File('${dir.path}/$_backupFileName');
    } catch (_) {
      return null;
    }
  }

  /// Saves the complete clinical state locally for instant offline startup
  /// and returns the formatted Google Drive backup JSON payload.
  Future<String> saveOfflineAndPrepareDrivePayload({
    required EmergencyProfileModel profile,
    required List<VitalSignModel> vitals,
    required List<MedicationModel> medications,
    required List<MedicalRecordModel> records,
    required List<AppointmentModel> appointments,
  }) async {
    final payloadMap = {
      'schemaVersion': 1,
      'app': 'My Medical History',
      'exportedAt': DateTime.now().toIso8601String(),
      'profile': profile.toJson(),
      'vitals': vitals.map((v) => v.toJson()).toList(),
      'medications': medications.map((m) => m.toJson()).toList(),
      'records': records.map((r) => r.toJson()).toList(),
      'appointments': appointments.map((a) => a.toJson()).toList(),
    };
    final jsonString = const JsonEncoder.withIndent('  ').convert(payloadMap);
    final file = await _getLocalStoreFile();
    if (file != null) {
      await file.writeAsString(jsonString, flush: true);
    }
    return jsonString;
  }

  /// Loads saved offline state if available, or returns null so default clinical data seeds.
  Future<Map<String, dynamic>?> loadOfflineState() async {
    try {
      final file = await _getLocalStoreFile();
      if (file != null && await file.exists()) {
        final content = await file.readAsString();
        return jsonDecode(content) as Map<String, dynamic>;
      }
    } catch (_) {}
    return null;
  }

  /// Restores state from a Google Drive JSON backup string and persists it offline.
  Future<Map<String, dynamic>> restoreFromDriveJson(String rawJson) async {
    final decoded = jsonDecode(rawJson) as Map<String, dynamic>;
    final file = await _getLocalStoreFile();
    if (file != null) {
      await file.writeAsString(rawJson, flush: true);
    }
    return decoded;
  }

  static EmergencyProfileModel seedProfile() => EmergencyProfileModel(
        fullName: 'Clara Vance-Sterling',
        dateOfBirth: '1989-04-14',
        age: 37,
        bloodType: 'O+',
        weightHeight: '142 lbs • 5\'7" (170 cm)',
        organDonor: true,
        severeAllergies: [
          'Penicillin (Anaphylaxis • Epinephrine Auto-Injector Carried)',
          'Latex (Acute Contact Urticaria & Airway Edema)',
          'Sulfonamide Antibiotics (Severe Rash & Fever)',
        ],
        chronicConditions: [
          'Stage 1 Essential Hypertension (Controlled)',
          'Seasonal Allergic Asthma (Mild Intermittent)',
          'Iron-Deficiency Anemia (Resolved 2025)',
        ],
        surgicalHistory: [
          'Laparoscopic Appendectomy (May 2019 • St. Jude Medical)',
          'Wisdom Teeth Extraction (Aug 2014)',
        ],
        emergencyContactName: 'Marcus Sterling',
        emergencyContactRelation: 'Spouse / Healthcare Proxy',
        emergencyContactPhone: '+1 (415) 890-4321',
        primaryPhysicianName: 'Dr. Elena Rostova, MD',
        primaryPhysicianClinic: 'Pacific Coastal Internal Medicine & Cardiology',
        primaryPhysicianPhone: '+1 (415) 622-9100',
        policyMemberId: 'BSP-9048271-01',
        criticalDirectiveNote:
            'Patient carries EpiPen 0.3mg in handbag. Avoid all beta-lactam antibiotics and latex gloves during acute intervention.',
      );

  static List<VitalSignModel> seedVitals() => [
        VitalSignModel(
          id: 'vital_bp',
          type: 'Blood Pressure',
          valueDisplay: '118/76',
          unit: 'mmHg',
          referenceRange: 'Optimal < 120/80 mmHg',
          status: 'NORMAL',
          numericPrimary: 118,
          trend7d: [122, 120, 119, 121, 118, 117, 118],
          trend1m: [128, 125, 124, 122, 120, 119, 118],
          trend6m: [134, 131, 128, 124, 121, 119, 118],
          trend1y: [138, 135, 131, 127, 123, 120, 118],
          recordedAt: 'Today, 08:15 AM',
          physicianNote:
              'Consistent downward trend following 10mg Lisinopril adherence.',
        ),
        VitalSignModel(
          id: 'vital_hr',
          type: 'Heart Rate',
          valueDisplay: '68',
          unit: 'bpm',
          referenceRange: 'Resting 60–100 bpm',
          status: 'NORMAL',
          numericPrimary: 68,
          trend7d: [71, 70, 69, 72, 68, 67, 68],
          trend1m: [74, 73, 71, 70, 69, 68, 68],
          trend6m: [76, 75, 73, 71, 70, 69, 68],
          trend1y: [78, 76, 74, 72, 71, 69, 68],
          recordedAt: 'Today, 08:15 AM',
          physicianNote: 'Regular sinus rhythm, strong resting vagal tone.',
        ),
        VitalSignModel(
          id: 'vital_glucose',
          type: 'Blood Glucose',
          valueDisplay: '94',
          unit: 'mg/dL',
          referenceRange: 'Fasting 70–99 mg/dL',
          status: 'NORMAL',
          numericPrimary: 94,
          trend7d: [96, 95, 92, 98, 94, 93, 94],
          trend1m: [99, 97, 95, 96, 94, 93, 94],
          trend6m: [102, 100, 98, 96, 95, 94, 94],
          trend1y: [104, 101, 99, 97, 95, 94, 94],
          recordedAt: 'Today, 07:45 AM',
          physicianNote:
              'Fasting capillary measurement within optimal metabolic target.',
        ),
        VitalSignModel(
          id: 'vital_spo2',
          type: 'Blood Oxygen',
          valueDisplay: '99',
          unit: '% SpO2',
          referenceRange: 'Normal 95–100%',
          status: 'NORMAL',
          numericPrimary: 99,
          trend7d: [98, 99, 99, 98, 99, 99, 99],
          trend1m: [98, 98, 99, 99, 98, 99, 99],
          trend6m: [97, 98, 98, 99, 99, 99, 99],
          trend1y: [97, 98, 98, 98, 99, 99, 99],
          recordedAt: 'Yesterday, 09:00 PM',
          physicianNote: 'Clear pulmonary oxygenation on room air.',
        ),
      ];

  static List<MedicationModel> seedMedications() => [
        MedicationModel(
          id: 'med_1',
          name: 'Lisinopril',
          dosage: '10 mg',
          schedule: 'Once daily • 08:00 AM',
          purpose: 'Blood Pressure & Arterial Protection',
          prescribingDoctor: 'Dr. Elena Rostova, MD',
          pillsRemaining: 5,
          totalPills: 30,
          takenToday: true,
          refillRequested: false,
          isArchived: false,
          startDate: 'Jan 12, 2026',
          instructions:
              'Take 1 tablet by mouth every morning with water. Monitor BP weekly.',
        ),
        MedicationModel(
          id: 'med_2',
          name: 'Cholecalciferol (Vitamin D3)',
          dosage: '2000 IU',
          schedule: 'Once daily • With breakfast',
          purpose: 'Bone Density & Immune Support',
          prescribingDoctor: 'Dr. Elena Rostova, MD',
          pillsRemaining: 22,
          totalPills: 60,
          takenToday: true,
          refillRequested: false,
          isArchived: false,
          startDate: 'Mar 01, 2026',
          instructions:
              'Take 1 softgel daily with a meal containing healthy fats.',
        ),
        MedicationModel(
          id: 'med_3',
          name: 'Montelukast Sodium',
          dosage: '10 mg',
          schedule: 'Once daily • 09:00 PM Evening',
          purpose: 'Seasonal Allergic Asthma Prophylaxis',
          prescribingDoctor: 'Dr. Aris Thorne, MD (Pulmonology)',
          pillsRemaining: 18,
          totalPills: 30,
          takenToday: false,
          refillRequested: false,
          isArchived: false,
          startDate: 'Sep 15, 2026',
          instructions:
              'Take 1 tablet in the evening to prevent nighttime bronchial constriction.',
        ),
        MedicationModel(
          id: 'med_4',
          name: 'Magnesium Glycinate',
          dosage: '400 mg',
          schedule: 'Once daily • 09:30 PM Bedtime',
          purpose: 'Vascular Relaxation & Sleep Recovery',
          prescribingDoctor: 'Dr. Elena Rostova, MD',
          pillsRemaining: 6,
          totalPills: 30,
          takenToday: false,
          refillRequested: false,
          isArchived: false,
          startDate: 'Apr 10, 2026',
          instructions: 'Take 2 chelated capsules 30 minutes prior to sleep.',
        ),
        MedicationModel(
          id: 'med_5',
          name: 'Ferrous Bisglycinate',
          dosage: '28 mg',
          schedule: 'Completed Course • Every other day',
          purpose: 'Ferritin Restoration Protocol',
          prescribingDoctor: 'Dr. Maya Lin, MD (Hematology)',
          pillsRemaining: 0,
          totalPills: 90,
          takenToday: false,
          isArchived: true,
          startDate: 'Jun 01, 2025',
          endDate: 'Dec 18, 2025',
          instructions: 'Discontinued after serum ferritin normalized to 78 ng/mL.',
        ),
      ];

  static List<MedicalRecordModel> seedRecords() => [
        MedicalRecordModel(
          id: 'rec_1',
          title: 'Severe Drug Allergy Alert: Penicillin & Beta-Lactams',
          category: 'Diagnoses',
          status: 'CRITICAL',
          statusBadgeText: 'Critical Allergy',
          summary:
              'Confirmed IgE-mediated anaphylactic hypersensitivity to Penicillin and cross-reactive beta-lactam derivatives. Patient carries twin-pack Epinephrine 0.3mg auto-injectors.',
          keyMetricsSummary: 'Reaction: Anaphylaxis • RAST Class V • Verified ID Alert',
          treatingPhysician: 'Dr. Aris Thorne, MD (Allergy & Immunology)',
          facility: 'Pacific Clinical Diagnostics Center',
          dateFormatted: 'Oct 02, 2026',
          attachmentFileName: 'Allergy_Immunology_Directive_2026.pdf',
          clinicalNotes:
              'Use macrolides or fluoroquinolones when antimicrobial therapy is indicated.',
        ),
        MedicalRecordModel(
          id: 'rec_2',
          title: 'Comprehensive Metabolic & Lipid Panel (Fasting)',
          category: 'Lab Results',
          status: 'NORMAL',
          statusBadgeText: 'Normal',
          summary:
              'All hepatic, renal, and glycemic markers within optimal reference bounds. Total cholesterol and LDL-C improved by 14% over the past 6 months.',
          keyMetricsSummary: 'HbA1c: 5.3% • LDL: 88 mg/dL • HDL: 64 mg/dL • eGFR: 96',
          treatingPhysician: 'Dr. Elena Rostova, MD (Cardiology)',
          facility: 'Quest Clinical Laboratories',
          dateFormatted: 'Sep 28, 2026',
          attachmentFileName: 'CMP_Lipid_Results_Sep2026.pdf',
          clinicalNotes:
              'Continue current Mediterranean nutrition protocol and 10mg Lisinopril daily.',
        ),
        MedicalRecordModel(
          id: 'rec_3',
          title: '24-Hour Ambulatory Blood Pressure Holter Review',
          category: 'Procedures',
          status: 'PENDING',
          statusBadgeText: 'Pending Follow-up',
          summary:
              'Daytime average 119/77 mmHg; nocturnal dipping profile preserved at 106/66 mmHg (11% dip). Scheduled for routine 6-month cardiology review next week.',
          keyMetricsSummary: '24h Mean: 115/73 mmHg • Max: 132/84 • Nocturnal Dip: 11%',
          treatingPhysician: 'Dr. Elena Rostova, MD (Cardiology)',
          facility: 'Pacific Coastal Heart Institute',
          dateFormatted: 'Sep 19, 2026',
          attachmentFileName: 'ABPM_Holter_Summary_Sep2026.pdf',
          clinicalNotes:
              'Bring home blood pressure cuff to upcoming October consultation.',
        ),
        MedicalRecordModel(
          id: 'rec_4',
          title: 'High-Resolution Chest Spirometry & FeNO Test',
          category: 'Imaging',
          status: 'NORMAL',
          statusBadgeText: 'Normal',
          summary:
              'Forced expiratory volume (FEV1/FVC ratio 0.84) demonstrates normal airway caliber with zero baseline obstruction.',
          keyMetricsSummary: 'FEV1: 102% Pred • FVC: 104% Pred • FeNO: 16 ppb',
          treatingPhysician: 'Dr. Aris Thorne, MD (Pulmonology)',
          facility: 'Bay Area Pulmonary & Allergy Clinic',
          dateFormatted: 'Aug 14, 2026',
          attachmentFileName: 'Spirometry_Pulmonary_Aug2026.pdf',
          clinicalNotes: 'Seasonal asthma well controlled on evening Montelukast.',
        ),
      ];

  static List<AppointmentModel> seedAppointments() => [
        AppointmentModel(
          id: 'appt_1',
          doctorName: 'Dr. Elena Rostova, MD',
          specialty: 'Cardiology & Internal Medicine',
          facility: 'Pacific Coastal Heart Institute • Suite 410',
          dateFormatted: 'Oct 14, 2026',
          timeFormatted: '10:30 AM',
          visitReason: 'Biannual Hypertension & Lipid Panel Consultation',
          status: 'PENDING',
          statusLabel: 'Upcoming • 6 Days',
          preparationInstructions:
              'Fast for 8 hours prior if repeat Electrolyte panel is ordered.',
        ),
      ];
}
