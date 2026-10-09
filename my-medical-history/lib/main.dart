import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'models/medical_models.dart';
import 'services/storage_and_drive_service.dart';
import 'theme/clinical_theme.dart';
import 'widgets/clinical_components.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyMedicalHistoryFlutterApp());
}

class MyMedicalHistoryFlutterApp extends StatelessWidget {
  const MyMedicalHistoryFlutterApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'My Medical History',
      debugShowCheckedModeBanner: false,
      theme: ClinicalTheme.lightTheme,
      home: const ClinicalHomeShell(),
    );
  }
}

class ClinicalHomeShell extends StatefulWidget {
  const ClinicalHomeShell({super.key});

  @override
  State<ClinicalHomeShell> createState() => _ClinicalHomeShellState();
}

class _ClinicalHomeShellState extends State<ClinicalHomeShell> {
  final StorageAndDriveService _storage = StorageAndDriveService();

  int _currentTab = 0;
  String _selectedTimeframe = '7D';
  int _selectedVitalIndex = 0;
  String _selectedRecordCategory = 'All';
  String _searchQuery = '';

  EmergencyProfileModel _profile = StorageAndDriveService.seedProfile();
  List<VitalSignModel> _vitals = StorageAndDriveService.seedVitals();
  List<MedicationModel> _medications = StorageAndDriveService.seedMedications();
  List<MedicalRecordModel> _records = StorageAndDriveService.seedRecords();
  List<AppointmentModel> _appointments =
      StorageAndDriveService.seedAppointments();

  static const List<String> _recordCategories = [
    'All',
    'Lab Results',
    'Diagnoses',
    'Procedures',
    'Imaging',
    'Immunizations',
  ];

  @override
  void initState() {
    super.initState();
    _loadPersistedOfflineData();
  }

  Future<void> _loadPersistedOfflineData() async {
    final saved = await _storage.loadOfflineState();
    if (saved != null && mounted) {
      setState(() {
        if (saved['profile'] != null) {
          _profile = EmergencyProfileModel.fromJson(
              saved['profile'] as Map<String, dynamic>);
        }
        if (saved['vitals'] != null) {
          _vitals = (saved['vitals'] as List)
              .map((e) => VitalSignModel.fromJson(e as Map<String, dynamic>))
              .toList();
        }
        if (saved['medications'] != null) {
          _medications = (saved['medications'] as List)
              .map((e) => MedicationModel.fromJson(e as Map<String, dynamic>))
              .toList();
        }
        if (saved['records'] != null) {
          _records = (saved['records'] as List)
              .map(
                  (e) => MedicalRecordModel.fromJson(e as Map<String, dynamic>))
              .toList();
        }
        if (saved['appointments'] != null) {
          _appointments = (saved['appointments'] as List)
              .map((e) => AppointmentModel.fromJson(e as Map<String, dynamic>))
              .toList();
        }
      });
    }
  }

  Future<String> _persistCurrentState() async {
    return _storage.saveOfflineAndPrepareDrivePayload(
      profile: _profile,
      vitals: _vitals,
      medications: _medications,
      records: _records,
      appointments: _appointments,
    );
  }

  void _showSnack(String msg) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(msg),
        backgroundColor: ClinicalColors.onSurfaceDarkNavy,
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  // --- MUTATIONS (All persisted offline automatically) ---

  void _toggleMedicationAdherence(MedicationModel med) {
    setState(() {
      final idx = _medications.indexWhere((m) => m.id == med.id);
      if (idx != -1) {
        final nextTaken = !med.takenToday;
        final nextPills = nextTaken
            ? (med.pillsRemaining - 1).clamp(0, med.totalPills)
            : (med.pillsRemaining + 1).clamp(0, med.totalPills);
        _medications[idx] = med.copyWith(
          takenToday: nextTaken,
          pillsRemaining: nextPills,
        );
      }
    });
    _persistCurrentState();
    _showSnack(
      !med.takenToday
          ? 'Logged ${med.name} (${med.dosage}) as taken today'
          : 'Unchecked ${med.name} dose for today',
    );
  }

  void _requestRefill(MedicationModel med) {
    setState(() {
      final idx = _medications.indexWhere((m) => m.id == med.id);
      if (idx != -1) {
        _medications[idx] = med.copyWith(refillRequested: true);
      }
    });
    _persistCurrentState();
    _showSnack(
        'Prescription refill requested for ${med.name} (${med.dosage})');
  }

  void _restockMedication(MedicationModel med) {
    setState(() {
      final idx = _medications.indexWhere((m) => m.id == med.id);
      if (idx != -1) {
        _medications[idx] = med.copyWith(
          pillsRemaining: med.totalPills,
          refillRequested: false,
        );
      }
    });
    _persistCurrentState();
    _showSnack('${med.name} restocked to ${med.totalPills} tablets');
  }

  void _toggleArchiveMedication(MedicationModel med) {
    setState(() {
      final idx = _medications.indexWhere((m) => m.id == med.id);
      if (idx != -1) {
        _medications[idx] = med.copyWith(isArchived: !med.isArchived);
      }
    });
    _persistCurrentState();
    _showSnack(
      !med.isArchived
          ? 'Moved ${med.name} to Historical Archive'
          : 'Restored ${med.name} to Active Medications',
    );
  }

  // --- DIALOGS & SHEETS ---

  void _showLogVitalDialog(VitalSignModel vital) {
    final valueCtrl = TextEditingController(text: vital.valueDisplay);
    final noteCtrl = TextEditingController();
    String selectedStatus = vital.status;

    showDialog(
      context: context,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (ctx, setDialogState) {
            return AlertDialog(
              backgroundColor: Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(24),
              ),
              title: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'LOG BIOMETRIC MEASUREMENT',
                    style: ClinicalTypography.labelSm.copyWith(
                      color: ClinicalColors.primaryTeal,
                    ),
                  ),
                  Text(
                    'Update ${vital.type}',
                    style: ClinicalTypography.headlineSm,
                  ),
                ],
              ),
              content: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '${vital.type.toUpperCase()} (${vital.unit})',
                      style: ClinicalTypography.labelSm,
                    ),
                    const SizedBox(height: 6),
                    TextField(
                      controller: valueCtrl,
                      decoration: InputDecoration(
                        hintText: 'e.g., ${vital.valueDisplay}',
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
                    ),
                    const SizedBox(height: 12),
                    const Text(
                      'CLINICAL STATUS',
                      style: ClinicalTypography.labelSm,
                    ),
                    const SizedBox(height: 6),
                    Wrap(
                      spacing: 8,
                      children: [
                        for (final entry in [
                          ('NORMAL', 'Normal'),
                          ('PENDING', 'Borderline'),
                          ('CRITICAL', 'High Alert')
                        ])
                          ChoiceChip(
                            label: Text(entry.$2),
                            selected: selectedStatus == entry.$1,
                            selectedColor: ClinicalColors.primaryTeal,
                            labelStyle: TextStyle(
                              color: selectedStatus == entry.$1
                                  ? Colors.white
                                  : ClinicalColors.slateLabelDark,
                            ),
                            onSelected: (_) => setDialogState(
                                () => selectedStatus = entry.$1),
                          ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    const Text(
                      'CLINICAL NOTE',
                      style: ClinicalTypography.labelSm,
                    ),
                    const SizedBox(height: 6),
                    TextField(
                      controller: noteCtrl,
                      maxLines: 2,
                      decoration: InputDecoration(
                        hintText: 'Resting morning measurement…',
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(ctx),
                  child: const Text('Cancel'),
                ),
                ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: ClinicalColors.primaryTeal,
                    foregroundColor: Colors.white,
                  ),
                  onPressed: () {
                    final rawVal = valueCtrl.text.trim();
                    if (rawVal.isEmpty) return;
                    final firstPart = rawVal.split('/').first.trim();
                    final parsedNum =
                        double.tryParse(firstPart) ?? vital.numericPrimary;
                    setState(() {
                      final idx =
                          _vitals.indexWhere((v) => v.id == vital.id);
                      if (idx != -1) {
                        final updated7d = [
                          ...vital.trend7d.skip(1),
                          parsedNum,
                        ];
                        _vitals[idx] = vital.copyWith(
                          valueDisplay: rawVal,
                          numericPrimary: parsedNum,
                          status: selectedStatus,
                          trend7d: updated7d,
                          recordedAt: 'Just now',
                          physicianNote: noteCtrl.text.trim().isEmpty
                              ? vital.physicianNote
                              : noteCtrl.text.trim(),
                        );
                      }
                    });
                    _persistCurrentState();
                    Navigator.pop(ctx);
                    _showSnack(
                        'Updated ${vital.type}: $rawVal ${vital.unit} (Saved Offline)');
                  },
                  child: const Text('Save Reading'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  void _showScheduleVisitDialog() {
    final docCtrl = TextEditingController(text: 'Dr. Elena Rostova, MD');
    final specCtrl = TextEditingController(text: 'Cardiology & Internal Med');
    final dateCtrl = TextEditingController(text: 'Nov 18, 2026');
    final timeCtrl = TextEditingController(text: '09:30 AM');
    final reasonCtrl = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'NEW CONSULTATION',
              style: ClinicalTypography.labelSm.copyWith(
                color: ClinicalColors.primaryTeal,
              ),
            ),
            const Text(
              'Schedule Clinical Visit',
              style: ClinicalTypography.headlineSm,
            ),
          ],
        ),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: docCtrl,
                decoration: const InputDecoration(labelText: 'Physician Name'),
              ),
              const SizedBox(height: 8),
              TextField(
                controller: specCtrl,
                decoration: const InputDecoration(labelText: 'Specialty'),
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: dateCtrl,
                      decoration: const InputDecoration(labelText: 'Date'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: TextField(
                      controller: timeCtrl,
                      decoration: const InputDecoration(labelText: 'Time'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              TextField(
                controller: reasonCtrl,
                decoration: const InputDecoration(
                  labelText: 'Consultation Reason',
                  hintText: 'e.g., Routine BP & Lipid Follow-up',
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: ClinicalColors.primaryTeal,
              foregroundColor: Colors.white,
            ),
            onPressed: () {
              setState(() {
                _appointments.insert(
                  0,
                  AppointmentModel(
                    id: 'appt_${DateTime.now().millisecondsSinceEpoch}',
                    doctorName: docCtrl.text.trim().isEmpty
                        ? 'Dr. Elena Rostova, MD'
                        : docCtrl.text.trim(),
                    specialty: specCtrl.text.trim(),
                    facility: 'Pacific Coastal Medical Center',
                    dateFormatted: dateCtrl.text.trim(),
                    timeFormatted: timeCtrl.text.trim(),
                    visitReason: reasonCtrl.text.trim().isEmpty
                        ? 'Clinical Follow-up Consultation'
                        : reasonCtrl.text.trim(),
                    status: 'PENDING',
                    statusLabel: 'Upcoming',
                    preparationInstructions:
                        'Bring updated medication list and home blood pressure log.',
                  ),
                );
              });
              _persistCurrentState();
              Navigator.pop(ctx);
              _showSnack('Scheduled clinical consultation (Saved Offline)');
            },
            child: const Text('Confirm Visit'),
          ),
        ],
      ),
    );
  }

  void _showAddRecordDialog() {
    final titleCtrl = TextEditingController();
    final metricsCtrl = TextEditingController();
    final summaryCtrl = TextEditingController();
    final docCtrl = TextEditingController(text: 'Dr. Elena Rostova, MD');
    String selectedCategory = 'Lab Results';
    String selectedStatus = 'NORMAL';

    showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialogState) => AlertDialog(
          backgroundColor: Colors.white,
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
          title: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'NEW CLINICAL ENTRY',
                style: ClinicalTypography.labelSm.copyWith(
                  color: ClinicalColors.primaryTeal,
                ),
              ),
              const Text(
                'Add Medical Record',
                style: ClinicalTypography.headlineSm,
              ),
            ],
          ),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                TextField(
                  controller: titleCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Record Title / Diagnostic Name',
                    hintText: 'e.g., Complete Blood Count (CBC)',
                  ),
                ),
                const SizedBox(height: 10),
                const Text('CATEGORY', style: ClinicalTypography.labelSm),
                const SizedBox(height: 6),
                Wrap(
                  spacing: 6,
                  children: _recordCategories.skip(1).map((cat) {
                    final sel = selectedCategory == cat;
                    return ChoiceChip(
                      label: Text(cat),
                      selected: sel,
                      selectedColor: ClinicalColors.primaryTeal,
                      labelStyle: TextStyle(
                        color: sel ? Colors.white : ClinicalColors.slateLabelDark,
                        fontSize: 12,
                      ),
                      onSelected: (_) =>
                          setDialogState(() => selectedCategory = cat),
                    );
                  }).toList(),
                ),
                const SizedBox(height: 10),
                const Text('CLINICAL STATUS FLAG',
                    style: ClinicalTypography.labelSm),
                const SizedBox(height: 6),
                Wrap(
                  spacing: 6,
                  children: [
                    ('NORMAL', 'Normal'),
                    ('PENDING', 'Pending'),
                    ('CRITICAL', 'Critical')
                  ].map((st) {
                    final sel = selectedStatus == st.$1;
                    return ChoiceChip(
                      label: Text(st.$2),
                      selected: sel,
                      selectedColor: ClinicalColors.primaryTeal,
                      labelStyle: TextStyle(
                        color: sel ? Colors.white : ClinicalColors.slateLabelDark,
                        fontSize: 12,
                      ),
                      onSelected: (_) =>
                          setDialogState(() => selectedStatus = st.$1),
                    );
                  }).toList(),
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: metricsCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Key Quantitative Biomarkers',
                    hintText: 'e.g., Hemoglobin: 13.8 g/dL • Ferritin: 82',
                  ),
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: summaryCtrl,
                  maxLines: 2,
                  decoration: const InputDecoration(
                    labelText: 'Clinical Interpretation Summary',
                  ),
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: docCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Treating Physician',
                  ),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: ClinicalColors.primaryTeal,
                foregroundColor: Colors.white,
              ),
              onPressed: () {
                final title = titleCtrl.text.trim().isEmpty
                    ? 'Clinical Diagnostic Evaluation'
                    : titleCtrl.text.trim();
                final badge = selectedStatus == 'CRITICAL'
                    ? 'Critical Alert'
                    : selectedStatus == 'PENDING'
                        ? 'Pending Review'
                        : 'Normal';
                setState(() {
                  _records.insert(
                    0,
                    MedicalRecordModel(
                      id: 'rec_${DateTime.now().millisecondsSinceEpoch}',
                      title: title,
                      category: selectedCategory,
                      status: selectedStatus,
                      statusBadgeText: badge,
                      summary: summaryCtrl.text.trim().isEmpty
                          ? 'Diagnostic evaluation logged within reference bounds.'
                          : summaryCtrl.text.trim(),
                      keyMetricsSummary: metricsCtrl.text.trim().isEmpty
                          ? 'Verified within clinical reference bounds'
                          : metricsCtrl.text.trim(),
                      treatingPhysician: docCtrl.text.trim(),
                      facility: 'Pacific Clinical Diagnostics Center',
                      dateFormatted: 'Oct 08, 2026',
                      attachmentFileName:
                          '${title.replaceAll(' ', '_')}_Report.pdf',
                      clinicalNotes:
                          'Logged and persisted in offline medical history.',
                    ),
                  );
                });
                _persistCurrentState();
                Navigator.pop(ctx);
                _showSnack('Saved clinical record: $title');
              },
              child: const Text('Save Record'),
            ),
          ],
        ),
      ),
    );
  }

  void _showRecordDetailSheet(MedicalRecordModel record) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  '${record.category.toUpperCase()} • ${record.dateFormatted}',
                  style: ClinicalTypography.labelSm.copyWith(
                    color: ClinicalColors.primaryTeal,
                  ),
                ),
                StatusPillBadge(
                  statusType: record.status,
                  label: record.statusBadgeText,
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(record.title, style: ClinicalTypography.headlineSm),
            const SizedBox(height: 14),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: ClinicalColors.neutralChipSurface,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: ClinicalColors.borderDivider),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'KEY DIAGNOSTIC BIOMARKERS',
                    style: ClinicalTypography.labelSm,
                  ),
                  const SizedBox(height: 4),
                  Text(
                    record.keyMetricsSummary,
                    style: ClinicalTypography.titleMd,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 14),
            const Text(
              'CLINICAL INTERPRETATION',
              style: ClinicalTypography.labelSm,
            ),
            const SizedBox(height: 4),
            Text(record.summary, style: ClinicalTypography.bodyLg),
            const SizedBox(height: 12),
            Text(
              'Attending: ${record.treatingPhysician}\nFacility: ${record.facility}',
              style: ClinicalTypography.bodySm,
            ),
            const SizedBox(height: 20),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: ClinicalColors.secondaryOceanic,
                      foregroundColor: Colors.white,
                      minimumSize: const Size.fromHeight(48),
                    ),
                    onPressed: () {
                      Navigator.pop(ctx);
                      _openGoogleDriveBackupModal();
                    },
                    icon: const Icon(Icons.cloud_upload_outlined, size: 18),
                    label: const Text('Backup Record to Drive'),
                  ),
                ),
                const SizedBox(width: 10),
                OutlinedButton.icon(
                  style: OutlinedButton.styleFrom(
                    minimumSize: const Size(110, 48),
                    foregroundColor: ClinicalColors.criticalRed,
                  ),
                  onPressed: () {
                    setState(() {
                      _records.removeWhere((r) => r.id == record.id);
                    });
                    _persistCurrentState();
                    Navigator.pop(ctx);
                    _showSnack('Deleted record: ${record.title}');
                  },
                  icon: const Icon(Icons.delete_outline, size: 18),
                  label: const Text('Delete'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _showAddMedicationDialog() {
    final nameCtrl = TextEditingController();
    final dosageCtrl = TextEditingController();
    final pillsCtrl = TextEditingController(text: '30');
    final scheduleCtrl = TextEditingController(text: 'Once daily • Morning');
    final purposeCtrl = TextEditingController();
    final instCtrl = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'NEW PRESCRIPTION',
              style: ClinicalTypography.labelSm.copyWith(
                color: ClinicalColors.primaryTeal,
              ),
            ),
            const Text(
              'Add Active Medication',
              style: ClinicalTypography.headlineSm,
            ),
          ],
        ),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: nameCtrl,
                decoration: const InputDecoration(
                  labelText: 'Medication Name',
                  hintText: 'e.g., Atorvastatin Calcium',
                ),
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: dosageCtrl,
                      decoration: const InputDecoration(
                        labelText: 'Dosage',
                        hintText: '20 mg',
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: TextField(
                      controller: pillsCtrl,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(
                        labelText: 'Pills in Bottle',
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              TextField(
                controller: scheduleCtrl,
                decoration: const InputDecoration(labelText: 'Dosing Schedule'),
              ),
              const SizedBox(height: 8),
              TextField(
                controller: purposeCtrl,
                decoration: const InputDecoration(
                  labelText: 'Clinical Indication',
                  hintText: 'e.g., Lipid Management',
                ),
              ),
              const SizedBox(height: 8),
              TextField(
                controller: instCtrl,
                decoration: const InputDecoration(
                  labelText: 'Administration Instructions',
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: ClinicalColors.primaryTeal,
              foregroundColor: Colors.white,
            ),
            onPressed: () {
              final name = nameCtrl.text.trim().isEmpty
                  ? 'Atorvastatin'
                  : nameCtrl.text.trim();
              final dosage = dosageCtrl.text.trim().isEmpty
                  ? '20 mg'
                  : dosageCtrl.text.trim();
              final pills = int.tryParse(pillsCtrl.text.trim()) ?? 30;
              setState(() {
                _medications.insert(
                  0,
                  MedicationModel(
                    id: 'med_${DateTime.now().millisecondsSinceEpoch}',
                    name: name,
                    dosage: dosage,
                    schedule: scheduleCtrl.text.trim(),
                    purpose: purposeCtrl.text.trim().isEmpty
                        ? 'Clinical Maintenance'
                        : purposeCtrl.text.trim(),
                    prescribingDoctor: 'Dr. Elena Rostova, MD',
                    pillsRemaining: pills,
                    totalPills: pills < 30 ? 30 : pills,
                    takenToday: false,
                    startDate: 'Oct 08, 2026',
                    instructions: instCtrl.text.trim().isEmpty
                        ? 'Take as directed by prescribing physician.'
                        : instCtrl.text.trim(),
                  ),
                );
              });
              _persistCurrentState();
              Navigator.pop(ctx);
              _showSnack('Added $name ($dosage) to Active Medications');
            },
            child: const Text('Save Medication'),
          ),
        ],
      ),
    );
  }

  void _showEditEmergencyProfileDialog() {
    final nameCtrl = TextEditingController(text: _profile.fullName);
    final bloodCtrl = TextEditingController(text: _profile.bloodType);
    final allergiesCtrl =
        TextEditingController(text: _profile.severeAllergies.join(' | '));
    final conditionsCtrl =
        TextEditingController(text: _profile.chronicConditions.join(' | '));
    final proxyNameCtrl =
        TextEditingController(text: _profile.emergencyContactName);
    final proxyPhoneCtrl =
        TextEditingController(text: _profile.emergencyContactPhone);
    final directiveCtrl =
        TextEditingController(text: _profile.criticalDirectiveNote);

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'UPDATE MEDICAL ID',
              style: ClinicalTypography.labelSm.copyWith(
                color: ClinicalColors.primaryTeal,
              ),
            ),
            const Text(
              'Edit Emergency Profile',
              style: ClinicalTypography.headlineSm,
            ),
          ],
        ),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                children: [
                  Expanded(
                    flex: 2,
                    child: TextField(
                      controller: nameCtrl,
                      decoration:
                          const InputDecoration(labelText: 'Patient Full Name'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: TextField(
                      controller: bloodCtrl,
                      decoration:
                          const InputDecoration(labelText: 'Blood Type'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              TextField(
                controller: allergiesCtrl,
                maxLines: 2,
                decoration: const InputDecoration(
                  labelText: 'Severe Allergies (Separate with |)',
                ),
              ),
              const SizedBox(height: 8),
              TextField(
                controller: conditionsCtrl,
                maxLines: 2,
                decoration: const InputDecoration(
                  labelText: 'Chronic Conditions (Separate with |)',
                ),
              ),
              const SizedBox(height: 8),
              TextField(
                controller: proxyNameCtrl,
                decoration:
                    const InputDecoration(labelText: 'Emergency Proxy Name'),
              ),
              const SizedBox(height: 8),
              TextField(
                controller: proxyPhoneCtrl,
                decoration:
                    const InputDecoration(labelText: 'Emergency Proxy Phone'),
              ),
              const SizedBox(height: 8),
              TextField(
                controller: directiveCtrl,
                maxLines: 2,
                decoration:
                    const InputDecoration(labelText: 'Critical Care Directive'),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: ClinicalColors.primaryTeal,
              foregroundColor: Colors.white,
            ),
            onPressed: () {
              setState(() {
                _profile = _profile.copyWith(
                  fullName: nameCtrl.text.trim(),
                  bloodType: bloodCtrl.text.trim(),
                  severeAllergies: allergiesCtrl.text
                      .split('|')
                      .map((e) => e.trim())
                      .where((e) => e.isNotEmpty)
                      .toList(),
                  chronicConditions: conditionsCtrl.text
                      .split('|')
                      .map((e) => e.trim())
                      .where((e) => e.isNotEmpty)
                      .toList(),
                  emergencyContactName: proxyNameCtrl.text.trim(),
                  emergencyContactPhone: proxyPhoneCtrl.text.trim(),
                  criticalDirectiveNote: directiveCtrl.text.trim(),
                );
              });
              _persistCurrentState();
              Navigator.pop(ctx);
              _showSnack('Updated Emergency Medical ID (Saved Offline)');
            },
            child: const Text('Save Profile'),
          ),
        ],
      ),
    );
  }

  void _showParamedicTriageModal() {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: ClinicalColors.criticalRed,
                borderRadius: BorderRadius.circular(10),
              ),
              child: const Icon(Icons.favorite, color: Colors.white, size: 20),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'PARAMEDIC / ER TRIAGE VIEW',
                    style: ClinicalTypography.labelSm.copyWith(
                      color: ClinicalColors.criticalRed,
                    ),
                  ),
                  Text(
                    _profile.fullName,
                    style: ClinicalTypography.headlineSm,
                  ),
                ],
              ),
            ),
          ],
        ),
        content: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Wrap(
                spacing: 8,
                children: [
                  StatusPillBadge(
                    statusType: 'CRITICAL',
                    label: 'Blood Type ${_profile.bloodType}',
                  ),
                  StatusPillBadge(
                    statusType: 'NORMAL',
                    label: 'DOB: ${_profile.dateOfBirth}',
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: ClinicalColors.emergencySurfaceTint,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(
                      color: ClinicalColors.emergencyBorderTint, width: 1.5),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'LIFE-CRITICAL ALLERGIES',
                      style: ClinicalTypography.labelSm.copyWith(
                        color: ClinicalColors.badgeCriticalText,
                      ),
                    ),
                    const SizedBox(height: 6),
                    ..._profile.severeAllergies.map(
                      (a) => Padding(
                        padding: const EdgeInsets.symmetric(vertical: 2),
                        child: Text(
                          '• $a',
                          style: ClinicalTypography.bodyMd.copyWith(
                            color: ClinicalColors.badgeCriticalText,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 12),
              const Text(
                'CRITICAL DIRECTIVE',
                style: ClinicalTypography.labelSm,
              ),
              const SizedBox(height: 4),
              Text(
                _profile.criticalDirectiveNote,
                style: ClinicalTypography.bodyMd.copyWith(
                  color: ClinicalColors.onSurfaceDarkNavy,
                ),
              ),
              const SizedBox(height: 12),
              Text(
                'Emergency Proxy: ${_profile.emergencyContactName} (${_profile.emergencyContactRelation})\nPhone: ${_profile.emergencyContactPhone}',
                style: ClinicalTypography.labelMd,
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Close Triage View'),
          ),
        ],
      ),
    );
  }

  Future<void> _openGoogleDriveBackupModal() async {
    final jsonPayload = await _persistCurrentState();
    if (!mounted) return;
    final importController = TextEditingController();

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) {
        return Padding(
          padding: EdgeInsets.only(
            left: 20,
            right: 20,
            top: 20,
            bottom: MediaQuery.of(ctx).viewInsets.bottom + 24,
          ),
          child: SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: ClinicalColors.badgeOceanicBg,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: const Icon(
                        Icons.cloud_done_rounded,
                        color: ClinicalColors.secondaryOceanic,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'OFFLINE STORAGE & GOOGLE DRIVE SYNC',
                            style: ClinicalTypography.labelSm.copyWith(
                              color: ClinicalColors.primaryTeal,
                            ),
                          ),
                          const Text(
                            'Cloud Backup & Restore',
                            style: ClinicalTypography.headlineSm,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                const Text(
                  'All clinical records, vitals, and medication logs are automatically persisted offline on your device. You can also export or restore your JSON backup snapshot to Google Drive.',
                  style: ClinicalTypography.bodyMd,
                ),
                const SizedBox(height: 16),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: ClinicalColors.neutralChipSurface,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: ClinicalColors.borderDivider),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'BACKUP SNAPSHOT STATUS',
                        style: ClinicalTypography.labelSm,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '${_records.length} Clinical Records • ${_medications.length} Medications • ${_vitals.length} Vitals',
                        style: ClinicalTypography.titleMd,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        _profile.lastDriveBackupAt,
                        style: ClinicalTypography.bodySm.copyWith(
                          color: ClinicalColors.primaryTeal,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  height: 52,
                  child: ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: ClinicalColors.secondaryOceanic,
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                    onPressed: () {
                      Clipboard.setData(ClipboardData(text: jsonPayload));
                      setState(() {
                        _profile = _profile.copyWith(
                          lastDriveBackupAt:
                              'Backed up for Google Drive • Just now',
                        );
                      });
                      _persistCurrentState();
                      Navigator.pop(ctx);
                      _showSnack(
                          'Google Drive Backup JSON copied & saved locally!');
                    },
                    icon: const Icon(Icons.cloud_upload_outlined),
                    label: const Text('Export & Copy Google Drive Backup JSON'),
                  ),
                ),
                const SizedBox(height: 16),
                const Text(
                  'RESTORE FROM GOOGLE DRIVE BACKUP JSON',
                  style: ClinicalTypography.labelSm,
                ),
                const SizedBox(height: 6),
                TextField(
                  controller: importController,
                  maxLines: 3,
                  decoration: InputDecoration(
                    hintText: 'Paste backup JSON from Google Drive here…',
                    filled: true,
                    fillColor: Colors.white,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                ),
                const SizedBox(height: 10),
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: OutlinedButton.icon(
                    onPressed: () async {
                      if (importController.text.trim().isEmpty) return;
                      try {
                        await _storage
                            .restoreFromDriveJson(importController.text.trim());
                        await _loadPersistedOfflineData();
                        if (ctx.mounted) Navigator.pop(ctx);
                        _showSnack(
                            'Successfully restored clinical history from Drive backup!');
                      } catch (_) {
                        _showSnack('Invalid backup JSON format.');
                      }
                    },
                    icon: const Icon(Icons.settings_backup_restore),
                    label: const Text('Restore Data from Backup'),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: IndexedStack(
          index: _currentTab,
          children: [
            _buildOverviewTab(),
            _buildRecordsTab(),
            _buildMedicationsTab(),
            _buildEmergencyIdTab(),
          ],
        ),
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentTab,
        onDestinationSelected: (idx) => setState(() => _currentTab = idx),
        backgroundColor: Colors.white,
        indicatorColor: _currentTab == 3
            ? ClinicalColors.criticalRed
            : ClinicalColors.primaryTeal,
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.space_dashboard_outlined),
            selectedIcon: Icon(Icons.space_dashboard, color: Colors.white),
            label: 'Overview',
          ),
          NavigationDestination(
            icon: Icon(Icons.description_outlined),
            selectedIcon: Icon(Icons.description, color: Colors.white),
            label: 'Records',
          ),
          NavigationDestination(
            icon: Icon(Icons.medication_outlined),
            selectedIcon: Icon(Icons.medication, color: Colors.white),
            label: 'Medications',
          ),
          NavigationDestination(
            icon: Icon(Icons.favorite_border, color: ClinicalColors.criticalRed),
            selectedIcon: Icon(Icons.favorite, color: Colors.white),
            label: 'Emergency ID',
          ),
        ],
      ),
    );
  }

  Widget _buildOverviewTab() {
    final activeVital = _vitals[_selectedVitalIndex];
    final activeMeds = _medications.where((m) => !m.isArchived).toList();
    final completedMeds = activeMeds.where((m) => m.takenToday).length;
    final trendPoints = _selectedTimeframe == '7D'
        ? activeVital.trend7d
        : _selectedTimeframe == '1M'
            ? activeVital.trend1m
            : _selectedTimeframe == '6M'
                ? activeVital.trend6m
                : activeVital.trend1y;

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        // Patient Clinical Header
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: ClinicalColors.borderDivider),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  CircleAvatar(
                    radius: 26,
                    backgroundColor: ClinicalColors.primaryFixed,
                    child: Text(
                      _profile.fullName.isNotEmpty
                          ? _profile.fullName.substring(0, 1)
                          : 'C',
                      style: ClinicalTypography.headlineMd.copyWith(
                        color: ClinicalColors.primaryTeal,
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          _profile.fullName,
                          style: ClinicalTypography.headlineSm,
                        ),
                        Text(
                          'ID: ${_profile.policyMemberId} • Offline Ready',
                          style: ClinicalTypography.bodySm,
                        ),
                      ],
                    ),
                  ),
                  StatusPillBadge(
                    statusType: 'INFO',
                    label: 'Blood ${_profile.bloodType}',
                  ),
                ],
              ),
              const SizedBox(height: 14),
              Row(
                children: [
                  Expanded(
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: ClinicalColors.secondaryOceanic,
                        foregroundColor: Colors.white,
                        minimumSize: const Size.fromHeight(48),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                      onPressed: _openGoogleDriveBackupModal,
                      icon: const Icon(Icons.cloud_sync_outlined, size: 18),
                      label: const Text('Drive Backup & Sync'),
                    ),
                  ),
                  const SizedBox(width: 10),
                  OutlinedButton.icon(
                    style: OutlinedButton.styleFrom(
                      minimumSize: const Size(120, 48),
                      side: const BorderSide(
                          color: ClinicalColors.borderDivider, width: 1.5),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                    onPressed: _showParamedicTriageModal,
                    icon: const Icon(Icons.favorite,
                        size: 18, color: ClinicalColors.criticalRed),
                    label: const Text('Emergency ID'),
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 20),

        // Emergency Medical Record Card (#FECACA border + faint red ambient glow)
        InkWell(
          onTap: _showParamedicTriageModal,
          borderRadius: BorderRadius.circular(16),
          child: Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                  color: ClinicalColors.emergencyBorderTint, width: 1.5),
              boxShadow: const [
                BoxShadow(
                  color: ClinicalColors.emergencyAmbientGlow,
                  blurRadius: 12,
                  offset: Offset(0, 4),
                ),
              ],
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: ClinicalColors.badgeCriticalBg,
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: const Icon(
                            Icons.warning_amber_rounded,
                            color: ClinicalColors.criticalRed,
                            size: 20,
                          ),
                        ),
                        const SizedBox(width: 10),
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'EMERGENCY MEDICAL RECORD',
                              style: ClinicalTypography.labelSm.copyWith(
                                color: ClinicalColors.badgeCriticalText,
                              ),
                            ),
                            Text(
                              'Blood Type ${_profile.bloodType} • Severe Allergies',
                              style: ClinicalTypography.titleMd,
                            ),
                          ],
                        ),
                      ],
                    ),
                    StatusPillBadge(
                      statusType: 'CRITICAL',
                      label: '${_profile.severeAllergies.length} Critical',
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: ClinicalColors.emergencySurfaceTint,
                    borderRadius: BorderRadius.circular(8),
                    border:
                        Border.all(color: ClinicalColors.emergencyBorderTint),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: _profile.severeAllergies
                        .take(2)
                        .map((a) => Padding(
                              padding: const EdgeInsets.symmetric(vertical: 2),
                              child: Text(
                                '• $a',
                                style: ClinicalTypography.bodySm.copyWith(
                                  color: ClinicalColors.badgeCriticalText,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ))
                        .toList(),
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 20),

        // Vital Signs & Biometrics
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: ClinicalColors.borderDivider),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'BIOMETRIC TELEMETRY',
                        style: ClinicalTypography.labelSm.copyWith(
                          color: ClinicalColors.primaryTeal,
                        ),
                      ),
                      const Text(
                        'Vital Signs & Trends',
                        style: ClinicalTypography.headlineSm,
                      ),
                    ],
                  ),
                  OutlinedButton.icon(
                    onPressed: () => _showLogVitalDialog(activeVital),
                    icon: const Icon(Icons.add, size: 16),
                    label: const Text('Log Vital'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              SegmentedTimeframeSelector(
                selected: _selectedTimeframe,
                onSelect: (tf) => setState(() => _selectedTimeframe = tf),
              ),
              const SizedBox(height: 14),
              GridView.builder(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: _vitals.length,
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  mainAxisSpacing: 10,
                  crossAxisSpacing: 10,
                  childAspectRatio: 1.32,
                ),
                itemBuilder: (ctx, idx) {
                  final v = _vitals[idx];
                  final selected = idx == _selectedVitalIndex;
                  return InkWell(
                    onTap: () => setState(() => _selectedVitalIndex = idx),
                    onLongPress: () => _showLogVitalDialog(v),
                    borderRadius: BorderRadius.circular(12),
                    child: Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: selected
                            ? const Color(0xFFF0FDFA)
                            : ClinicalColors.surfaceWhite,
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(
                          color: selected
                              ? ClinicalColors.primaryTeal
                              : ClinicalColors.borderDivider,
                          width: selected ? 2 : 1,
                        ),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Expanded(
                                child: Text(
                                  v.type,
                                  style: ClinicalTypography.labelMd,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                              StatusPillBadge(
                                statusType: v.status,
                                label:
                                    v.status == 'NORMAL' ? 'Stable' : v.status,
                              ),
                            ],
                          ),
                          Row(
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              Text(
                                v.valueDisplay,
                                style: ClinicalTypography.metricNumeral,
                              ),
                              const SizedBox(width: 4),
                              Padding(
                                padding: const EdgeInsets.only(bottom: 4),
                                child: Text(
                                  v.unit,
                                  style: ClinicalTypography.labelSm,
                                ),
                              ),
                            ],
                          ),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Expanded(
                                child: Text(
                                  v.recordedAt,
                                  style: ClinicalTypography.labelSm.copyWith(
                                    color: ClinicalColors.primaryTeal,
                                  ),
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                              GestureDetector(
                                onTap: () => _showLogVitalDialog(v),
                                child: Text(
                                  'Update',
                                  style: ClinicalTypography.labelSm.copyWith(
                                    color: ClinicalColors.secondaryOceanic,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 14),
              Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: ClinicalColors.neutralChipSurface,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: ClinicalColors.borderDivider),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '${activeVital.type} Trend ($_selectedTimeframe)',
                      style: ClinicalTypography.titleMd,
                    ),
                    const SizedBox(height: 12),
                    SizedBox(
                      height: 100,
                      width: double.infinity,
                      child: CustomPaint(
                        painter: BiometricTrendPainter(points: trendPoints),
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      activeVital.physicianNote,
                      style: ClinicalTypography.bodySm,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 20),

        // Medication Adherence Section
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: ClinicalColors.borderDivider),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Medication Adherence',
                    style: ClinicalTypography.headlineSm,
                  ),
                  StatusPillBadge(
                    statusType: 'ADHERENT',
                    label: '$completedMeds of ${activeMeds.length} Taken',
                  ),
                ],
              ),
              const SizedBox(height: 12),
              ...activeMeds.map(
                (med) => Padding(
                  padding: const EdgeInsets.only(bottom: 8),
                  child: Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: med.takenToday
                          ? const Color(0xFFF0FDF4)
                          : Colors.white,
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: ClinicalColors.borderDivider),
                    ),
                    child: Row(
                      children: [
                        AdherenceCheckbox(
                          checked: med.takenToday,
                          onChanged: () => _toggleMedicationAdherence(med),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                '${med.name} (${med.dosage})',
                                style: ClinicalTypography.titleMd,
                              ),
                              Text(
                                med.schedule,
                                style: ClinicalTypography.bodySm,
                              ),
                            ],
                          ),
                        ),
                        StatusPillBadge(
                          statusType:
                              med.pillsRemaining <= 7 ? 'REFILL' : 'ADHERENT',
                          label: med.pillsRemaining <= 7
                              ? '${med.pillsRemaining} left'
                              : (med.takenToday ? 'Adherent' : 'Scheduled'),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 20),

        // Upcoming Consultations Section
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: ClinicalColors.borderDivider),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'CARE COORDINATION',
                        style: ClinicalTypography.labelSm.copyWith(
                          color: ClinicalColors.secondaryOceanic,
                        ),
                      ),
                      const Text(
                        'Upcoming Consultations',
                        style: ClinicalTypography.headlineSm,
                      ),
                    ],
                  ),
                  OutlinedButton.icon(
                    onPressed: _showScheduleVisitDialog,
                    icon: const Icon(Icons.calendar_month, size: 16),
                    label: const Text('Schedule'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              ..._appointments.map(
                (appt) => Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: ClinicalColors.neutralChipSurface,
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: ClinicalColors.borderDivider),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Expanded(
                              child: Text(
                                appt.doctorName,
                                style: ClinicalTypography.titleMd,
                              ),
                            ),
                            StatusPillBadge(
                              statusType: appt.status,
                              label: appt.statusLabel,
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Text(
                          '${appt.dateFormatted} at ${appt.timeFormatted} — ${appt.visitReason}',
                          style: ClinicalTypography.labelMd,
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'Prep: ${appt.preparationInstructions}',
                          style: ClinicalTypography.bodySm,
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildRecordsTab() {
    final filtered = _records.where((r) {
      final matchesCat = _selectedRecordCategory == 'All' ||
          r.category.toLowerCase() == _selectedRecordCategory.toLowerCase();
      final matchesQuery = _searchQuery.isEmpty ||
          r.title.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          r.summary.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          r.treatingPhysician
              .toLowerCase()
              .contains(_searchQuery.toLowerCase());
      return matchesCat && matchesQuery;
    }).toList();

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'DIAGNOSTIC ARCHIVE',
                  style: ClinicalTypography.labelSm.copyWith(
                    color: ClinicalColors.primaryTeal,
                  ),
                ),
                const Text(
                  'Clinical Records',
                  style: ClinicalTypography.headlineMd,
                ),
              ],
            ),
            ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                backgroundColor: ClinicalColors.primaryTeal,
                foregroundColor: Colors.white,
                minimumSize: const Size(120, 48),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              onPressed: _showAddRecordDialog,
              icon: const Icon(Icons.add, size: 18),
              label: const Text('Add Record'),
            ),
          ],
        ),
        const SizedBox(height: 12),
        TextField(
          onChanged: (val) => setState(() => _searchQuery = val),
          decoration: InputDecoration(
            hintText: 'Search diagnoses, labs, physicians…',
            prefixIcon: const Icon(Icons.search),
            filled: true,
            fillColor: Colors.white,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
              borderSide: const BorderSide(color: ClinicalColors.inputBorder),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
              borderSide:
                  const BorderSide(color: ClinicalColors.primaryTeal, width: 2),
            ),
          ),
        ),
        const SizedBox(height: 12),
        SizedBox(
          height: 38,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: _recordCategories.length,
            separatorBuilder: (_, __) => const SizedBox(width: 8),
            itemBuilder: (ctx, idx) {
              final cat = _recordCategories[idx];
              final selected = cat == _selectedRecordCategory;
              return ChoiceChip(
                label: Text(cat),
                selected: selected,
                selectedColor: ClinicalColors.primaryTeal,
                backgroundColor: ClinicalColors.neutralChipSurface,
                labelStyle: ClinicalTypography.labelMd.copyWith(
                  color: selected ? Colors.white : ClinicalColors.slateLabelDark,
                ),
                shape: const StadiumBorder(),
                onSelected: (_) =>
                    setState(() => _selectedRecordCategory = cat),
              );
            },
          ),
        ),
        const SizedBox(height: 16),
        ...filtered.map(
          (rec) => Padding(
            padding: const EdgeInsets.only(bottom: 16),
            child: MedicalRecordCardWidget(
              record: rec,
              onTap: () => _showRecordDetailSheet(rec),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildMedicationsTab() {
    final activeMeds = _medications.where((m) => !m.isArchived).toList();
    final archivedMeds = _medications.where((m) => m.isArchived).toList();

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'PHARMACOTHERAPY & ADHERENCE',
                  style: ClinicalTypography.labelSm.copyWith(
                    color: ClinicalColors.primaryTeal,
                  ),
                ),
                const Text(
                  'Medications',
                  style: ClinicalTypography.headlineMd,
                ),
              ],
            ),
            ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                backgroundColor: ClinicalColors.primaryTeal,
                foregroundColor: Colors.white,
                minimumSize: const Size(110, 48),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              onPressed: _showAddMedicationDialog,
              icon: const Icon(Icons.add, size: 18),
              label: const Text('Add Med'),
            ),
          ],
        ),
        const SizedBox(height: 14),
        ...activeMeds.map(
          (med) => Padding(
            padding: const EdgeInsets.only(bottom: 16),
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: ClinicalColors.borderDivider),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      AdherenceCheckbox(
                        checked: med.takenToday,
                        onChanged: () => _toggleMedicationAdherence(med),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '${med.name} • ${med.dosage}',
                              style: ClinicalTypography.titleMd,
                            ),
                            Text(
                              med.schedule,
                              style: ClinicalTypography.bodySm,
                            ),
                          ],
                        ),
                      ),
                      StatusPillBadge(
                        statusType:
                            med.pillsRemaining <= 7 ? 'REFILL' : 'ADHERENT',
                        label: med.pillsRemaining <= 7
                            ? '${med.pillsRemaining} left'
                            : (med.takenToday ? 'Taken' : 'Active'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  Text(
                    'Indication: ${med.purpose}',
                    style: ClinicalTypography.labelMd,
                  ),
                  const SizedBox(height: 4),
                  Text(
                    med.instructions,
                    style: ClinicalTypography.bodyMd,
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: ElevatedButton.icon(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: ClinicalColors.secondaryOceanic,
                            foregroundColor: Colors.white,
                            minimumSize: const Size.fromHeight(48),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(12),
                            ),
                          ),
                          onPressed: () => med.refillRequested
                              ? _restockMedication(med)
                              : _requestRefill(med),
                          icon: Icon(
                            med.refillRequested
                                ? Icons.inventory_2_outlined
                                : Icons.local_pharmacy_outlined,
                            size: 18,
                          ),
                          label: Text(
                            med.refillRequested
                                ? 'Confirm Pharmacy Restock'
                                : 'Request Prescription Refill',
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      OutlinedButton(
                        style: OutlinedButton.styleFrom(
                          minimumSize: const Size(88, 48),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(12),
                          ),
                        ),
                        onPressed: () => _toggleArchiveMedication(med),
                        child: const Text('Archive'),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ),
        // space-xl (32px) structural separation between Active Medications and Historical Archive
        const SizedBox(height: 32),
        const Text(
          'Historical Archive',
          style: ClinicalTypography.headlineSm,
        ),
        const SizedBox(height: 12),
        ...archivedMeds.map(
          (med) => Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: ClinicalColors.borderDivider),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        '${med.name} (${med.dosage})',
                        style: ClinicalTypography.titleMd,
                      ),
                      const StatusPillBadge(
                        statusType: 'COMPLETED',
                        label: 'Completed',
                      ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  Text(
                    med.instructions,
                    style: ClinicalTypography.bodySm,
                  ),
                  const SizedBox(height: 8),
                  Align(
                    alignment: Alignment.centerRight,
                    child: TextButton.icon(
                      onPressed: () => _toggleArchiveMedication(med),
                      icon: const Icon(Icons.unarchive_outlined, size: 16),
                      label: const Text('Restore Active'),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildEmergencyIdTab() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'ACUTE CARE DISCLOSURE',
                  style: ClinicalTypography.labelSm.copyWith(
                    color: ClinicalColors.criticalRed,
                  ),
                ),
                const Text(
                  'Emergency Medical ID',
                  style: ClinicalTypography.headlineMd,
                ),
              ],
            ),
            OutlinedButton.icon(
              onPressed: _showEditEmergencyProfileDialog,
              icon: const Icon(Icons.edit_outlined, size: 16),
              label: const Text('Edit ID'),
            ),
          ],
        ),
        const SizedBox(height: 14),
        SizedBox(
          width: double.infinity,
          height: 56,
          child: ElevatedButton.icon(
            style: ElevatedButton.styleFrom(
              backgroundColor: ClinicalColors.criticalRed,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(14),
              ),
            ),
            onPressed: _showParamedicTriageModal,
            icon: const Icon(Icons.medical_services_outlined),
            label: const Text(
              'Launch Paramedic / ER Responder Card',
              style: TextStyle(
                fontFamily: ClinicalTypography.displayFont,
                fontWeight: FontWeight.w700,
                fontSize: 16,
              ),
            ),
          ),
        ),
        const SizedBox(height: 20),
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(
                color: ClinicalColors.emergencyBorderTint, width: 1.5),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Severe Allergies & Directives',
                style: ClinicalTypography.titleMd,
              ),
              const SizedBox(height: 10),
              ..._profile.severeAllergies.map(
                (a) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 4),
                  child: Text(
                    '• $a',
                    style: ClinicalTypography.bodyMd.copyWith(
                      color: ClinicalColors.badgeCriticalText,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 10),
              Text(
                _profile.criticalDirectiveNote,
                style: ClinicalTypography.bodyMd,
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: ClinicalColors.borderDivider),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Chronic Conditions & Emergency Contact',
                style: ClinicalTypography.titleMd,
              ),
              const SizedBox(height: 8),
              ..._profile.chronicConditions.map(
                (c) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 2),
                  child: Text('• $c', style: ClinicalTypography.bodyMd),
                ),
              ),
              const Divider(height: 20),
              Text(
                'Primary Proxy: ${_profile.emergencyContactName} (${_profile.emergencyContactRelation})',
                style: ClinicalTypography.labelMd,
              ),
              Text(
                'Phone: ${_profile.emergencyContactPhone}',
                style: ClinicalTypography.bodyMd,
              ),
              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton.icon(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: ClinicalColors.secondaryOceanic,
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                  onPressed: _openGoogleDriveBackupModal,
                  icon: const Icon(Icons.cloud_upload_outlined, size: 18),
                  label: const Text('Backup Emergency ID to Google Drive'),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
