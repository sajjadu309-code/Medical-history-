class VitalSignModel {
  final String id;
  final String type;
  final String valueDisplay;
  final String unit;
  final String referenceRange;
  final String status; // NORMAL, PENDING, CRITICAL
  final double numericPrimary;
  final List<double> trend7d;
  final List<double> trend1m;
  final List<double> trend6m;
  final List<double> trend1y;
  final String recordedAt;
  final String physicianNote;

  VitalSignModel({
    required this.id,
    required this.type,
    required this.valueDisplay,
    required this.unit,
    required this.referenceRange,
    required this.status,
    required this.numericPrimary,
    required this.trend7d,
    required this.trend1m,
    required this.trend6m,
    required this.trend1y,
    required this.recordedAt,
    required this.physicianNote,
  });

  VitalSignModel copyWith({
    String? valueDisplay,
    String? status,
    double? numericPrimary,
    List<double>? trend7d,
    String? recordedAt,
    String? physicianNote,
  }) {
    return VitalSignModel(
      id: id,
      type: type,
      valueDisplay: valueDisplay ?? this.valueDisplay,
      unit: unit,
      referenceRange: referenceRange,
      status: status ?? this.status,
      numericPrimary: numericPrimary ?? this.numericPrimary,
      trend7d: trend7d ?? this.trend7d,
      trend1m: trend1m,
      trend6m: trend6m,
      trend1y: trend1y,
      recordedAt: recordedAt ?? this.recordedAt,
      physicianNote: physicianNote ?? this.physicianNote,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'type': type,
        'valueDisplay': valueDisplay,
        'unit': unit,
        'referenceRange': referenceRange,
        'status': status,
        'numericPrimary': numericPrimary,
        'trend7d': trend7d,
        'trend1m': trend1m,
        'trend6m': trend6m,
        'trend1y': trend1y,
        'recordedAt': recordedAt,
        'physicianNote': physicianNote,
      };

  factory VitalSignModel.fromJson(Map<String, dynamic> json) => VitalSignModel(
        id: json['id'] as String,
        type: json['type'] as String,
        valueDisplay: json['valueDisplay'] as String,
        unit: json['unit'] as String,
        referenceRange: json['referenceRange'] as String,
        status: json['status'] as String,
        numericPrimary: (json['numericPrimary'] as num).toDouble(),
        trend7d: (json['trend7d'] as List).map((e) => (e as num).toDouble()).toList(),
        trend1m: (json['trend1m'] as List).map((e) => (e as num).toDouble()).toList(),
        trend6m: (json['trend6m'] as List).map((e) => (e as num).toDouble()).toList(),
        trend1y: (json['trend1y'] as List).map((e) => (e as num).toDouble()).toList(),
        recordedAt: json['recordedAt'] as String,
        physicianNote: json['physicianNote'] as String,
      );
}

class MedicationModel {
  final String id;
  final String name;
  final String dosage;
  final String schedule;
  final String purpose;
  final String prescribingDoctor;
  final int pillsRemaining;
  final int totalPills;
  final bool takenToday;
  final bool refillRequested;
  final bool isArchived;
  final String startDate;
  final String? endDate;
  final String instructions;

  MedicationModel({
    required this.id,
    required this.name,
    required this.dosage,
    required this.schedule,
    required this.purpose,
    required this.prescribingDoctor,
    required this.pillsRemaining,
    required this.totalPills,
    required this.takenToday,
    this.refillRequested = false,
    this.isArchived = false,
    required this.startDate,
    this.endDate,
    required this.instructions,
  });

  MedicationModel copyWith({
    int? pillsRemaining,
    bool? takenToday,
    bool? refillRequested,
    bool? isArchived,
  }) {
    return MedicationModel(
      id: id,
      name: name,
      dosage: dosage,
      schedule: schedule,
      purpose: purpose,
      prescribingDoctor: prescribingDoctor,
      pillsRemaining: pillsRemaining ?? this.pillsRemaining,
      totalPills: totalPills,
      takenToday: takenToday ?? this.takenToday,
      refillRequested: refillRequested ?? this.refillRequested,
      isArchived: isArchived ?? this.isArchived,
      startDate: startDate,
      endDate: endDate,
      instructions: instructions,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'dosage': dosage,
        'schedule': schedule,
        'purpose': purpose,
        'prescribingDoctor': prescribingDoctor,
        'pillsRemaining': pillsRemaining,
        'totalPills': totalPills,
        'takenToday': takenToday,
        'refillRequested': refillRequested,
        'isArchived': isArchived,
        'startDate': startDate,
        'endDate': endDate,
        'instructions': instructions,
      };

  factory MedicationModel.fromJson(Map<String, dynamic> json) => MedicationModel(
        id: json['id'] as String,
        name: json['name'] as String,
        dosage: json['dosage'] as String,
        schedule: json['schedule'] as String,
        purpose: json['purpose'] as String,
        prescribingDoctor: json['prescribingDoctor'] as String,
        pillsRemaining: json['pillsRemaining'] as int,
        totalPills: json['totalPills'] as int,
        takenToday: json['takenToday'] as bool,
        refillRequested: json['refillRequested'] as bool? ?? false,
        isArchived: json['isArchived'] as bool? ?? false,
        startDate: json['startDate'] as String,
        endDate: json['endDate'] as String?,
        instructions: json['instructions'] as String,
      );
}

class MedicalRecordModel {
  final String id;
  final String title;
  final String category;
  final String status;
  final String statusBadgeText;
  final String summary;
  final String keyMetricsSummary;
  final String treatingPhysician;
  final String facility;
  final String dateFormatted;
  final String attachmentFileName;
  final String clinicalNotes;

  MedicalRecordModel({
    required this.id,
    required this.title,
    required this.category,
    required this.status,
    required this.statusBadgeText,
    required this.summary,
    required this.keyMetricsSummary,
    required this.treatingPhysician,
    required this.facility,
    required this.dateFormatted,
    required this.attachmentFileName,
    required this.clinicalNotes,
  });

  Map<String, dynamic> toJson() => {
        'id': id,
        'title': title,
        'category': category,
        'status': status,
        'statusBadgeText': statusBadgeText,
        'summary': summary,
        'keyMetricsSummary': keyMetricsSummary,
        'treatingPhysician': treatingPhysician,
        'facility': facility,
        'dateFormatted': dateFormatted,
        'attachmentFileName': attachmentFileName,
        'clinicalNotes': clinicalNotes,
      };

  factory MedicalRecordModel.fromJson(Map<String, dynamic> json) =>
      MedicalRecordModel(
        id: json['id'] as String,
        title: json['title'] as String,
        category: json['category'] as String,
        status: json['status'] as String,
        statusBadgeText: json['statusBadgeText'] as String,
        summary: json['summary'] as String,
        keyMetricsSummary: json['keyMetricsSummary'] as String,
        treatingPhysician: json['treatingPhysician'] as String,
        facility: json['facility'] as String,
        dateFormatted: json['dateFormatted'] as String,
        attachmentFileName: json['attachmentFileName'] as String,
        clinicalNotes: json['clinicalNotes'] as String,
      );
}

class AppointmentModel {
  final String id;
  final String doctorName;
  final String specialty;
  final String facility;
  final String dateFormatted;
  final String timeFormatted;
  final String visitReason;
  final String status;
  final String statusLabel;
  final String preparationInstructions;

  AppointmentModel({
    required this.id,
    required this.doctorName,
    required this.specialty,
    required this.facility,
    required this.dateFormatted,
    required this.timeFormatted,
    required this.visitReason,
    required this.status,
    required this.statusLabel,
    required this.preparationInstructions,
  });

  Map<String, dynamic> toJson() => {
        'id': id,
        'doctorName': doctorName,
        'specialty': specialty,
        'facility': facility,
        'dateFormatted': dateFormatted,
        'timeFormatted': timeFormatted,
        'visitReason': visitReason,
        'status': status,
        'statusLabel': statusLabel,
        'preparationInstructions': preparationInstructions,
      };

  factory AppointmentModel.fromJson(Map<String, dynamic> json) => AppointmentModel(
        id: json['id'] as String,
        doctorName: json['doctorName'] as String,
        specialty: json['specialty'] as String,
        facility: json['facility'] as String,
        dateFormatted: json['dateFormatted'] as String,
        timeFormatted: json['timeFormatted'] as String,
        visitReason: json['visitReason'] as String,
        status: json['status'] as String,
        statusLabel: json['statusLabel'] as String,
        preparationInstructions: json['preparationInstructions'] as String,
      );
}

class EmergencyProfileModel {
  final String fullName;
  final String dateOfBirth;
  final int age;
  final String bloodType;
  final String weightHeight;
  final bool organDonor;
  final List<String> severeAllergies;
  final List<String> chronicConditions;
  final List<String> surgicalHistory;
  final String emergencyContactName;
  final String emergencyContactRelation;
  final String emergencyContactPhone;
  final String primaryPhysicianName;
  final String primaryPhysicianClinic;
  final String primaryPhysicianPhone;
  final String policyMemberId;
  final String criticalDirectiveNote;
  final String lastDriveBackupAt;

  EmergencyProfileModel({
    required this.fullName,
    required this.dateOfBirth,
    required this.age,
    required this.bloodType,
    required this.weightHeight,
    required this.organDonor,
    required this.severeAllergies,
    required this.chronicConditions,
    required this.surgicalHistory,
    required this.emergencyContactName,
    required this.emergencyContactRelation,
    required this.emergencyContactPhone,
    required this.primaryPhysicianName,
    required this.primaryPhysicianClinic,
    required this.primaryPhysicianPhone,
    required this.policyMemberId,
    required this.criticalDirectiveNote,
    this.lastDriveBackupAt = 'Synced Locally • Ready for Google Drive',
  });

  EmergencyProfileModel copyWith({
    String? fullName,
    String? bloodType,
    List<String>? severeAllergies,
    List<String>? chronicConditions,
    String? emergencyContactName,
    String? emergencyContactPhone,
    String? criticalDirectiveNote,
    String? lastDriveBackupAt,
  }) {
    return EmergencyProfileModel(
      fullName: fullName ?? this.fullName,
      dateOfBirth: dateOfBirth,
      age: age,
      bloodType: bloodType ?? this.bloodType,
      weightHeight: weightHeight,
      organDonor: organDonor,
      severeAllergies: severeAllergies ?? this.severeAllergies,
      chronicConditions: chronicConditions ?? this.chronicConditions,
      surgicalHistory: surgicalHistory,
      emergencyContactName: emergencyContactName ?? this.emergencyContactName,
      emergencyContactRelation: emergencyContactRelation,
      emergencyContactPhone: emergencyContactPhone ?? this.emergencyContactPhone,
      primaryPhysicianName: primaryPhysicianName,
      primaryPhysicianClinic: primaryPhysicianClinic,
      primaryPhysicianPhone: primaryPhysicianPhone,
      policyMemberId: policyMemberId,
      criticalDirectiveNote: criticalDirectiveNote ?? this.criticalDirectiveNote,
      lastDriveBackupAt: lastDriveBackupAt ?? this.lastDriveBackupAt,
    );
  }

  Map<String, dynamic> toJson() => {
        'fullName': fullName,
        'dateOfBirth': dateOfBirth,
        'age': age,
        'bloodType': bloodType,
        'weightHeight': weightHeight,
        'organDonor': organDonor,
        'severeAllergies': severeAllergies,
        'chronicConditions': chronicConditions,
        'surgicalHistory': surgicalHistory,
        'emergencyContactName': emergencyContactName,
        'emergencyContactRelation': emergencyContactRelation,
        'emergencyContactPhone': emergencyContactPhone,
        'primaryPhysicianName': primaryPhysicianName,
        'primaryPhysicianClinic': primaryPhysicianClinic,
        'primaryPhysicianPhone': primaryPhysicianPhone,
        'policyMemberId': policyMemberId,
        'criticalDirectiveNote': criticalDirectiveNote,
        'lastDriveBackupAt': lastDriveBackupAt,
      };

  factory EmergencyProfileModel.fromJson(Map<String, dynamic> json) =>
      EmergencyProfileModel(
        fullName: json['fullName'] as String,
        dateOfBirth: json['dateOfBirth'] as String,
        age: json['age'] as int,
        bloodType: json['bloodType'] as String,
        weightHeight: json['weightHeight'] as String,
        organDonor: json['organDonor'] as bool,
        severeAllergies: List<String>.from(json['severeAllergies'] as List),
        chronicConditions: List<String>.from(json['chronicConditions'] as List),
        surgicalHistory: List<String>.from(json['surgicalHistory'] as List),
        emergencyContactName: json['emergencyContactName'] as String,
        emergencyContactRelation: json['emergencyContactRelation'] as String,
        emergencyContactPhone: json['emergencyContactPhone'] as String,
        primaryPhysicianName: json['primaryPhysicianName'] as String,
        primaryPhysicianClinic: json['primaryPhysicianClinic'] as String,
        primaryPhysicianPhone: json['primaryPhysicianPhone'] as String,
        policyMemberId: json['policyMemberId'] as String,
        criticalDirectiveNote: json['criticalDirectiveNote'] as String,
        lastDriveBackupAt: json['lastDriveBackupAt'] as String? ?? 'Synced Locally',
      );
}
