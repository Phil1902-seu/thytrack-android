package com.thytrack.android.domain.model

import java.util.Date

/** 用药变更记录。 */
data class MedicationChange(
    val id: String,
    val date: Date,
    val drug: Drug,
    val oldDose: Double,
    val newDose: Double,
    val reason: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val recordId: String? = null,
)
