package com.example.data.local

import androidx.room.TypeConverter

class MedicalConverters {

    @TypeConverter
    fun fromClinicalStatus(status: ClinicalStatus): String = status.name

    @TypeConverter
    fun toClinicalStatus(value: String): ClinicalStatus =
        runCatching { ClinicalStatus.valueOf(value) }.getOrDefault(ClinicalStatus.NORMAL)

    @TypeConverter
    fun fromFloatList(values: List<Float>): String =
        values.joinToString(",")

    @TypeConverter
    fun toFloatList(csv: String): List<Float> =
        if (csv.isBlank()) emptyList()
        else csv.split(",").mapNotNull { it.trim().toFloatOrNull() }

    @TypeConverter
    fun fromStringList(values: List<String>): String =
        values.joinToString("|")

    @TypeConverter
    fun toStringList(serialized: String): List<String> =
        if (serialized.isBlank()) emptyList()
        else serialized.split("|").map { it.trim() }.filter { it.isNotEmpty() }
}
