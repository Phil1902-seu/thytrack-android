package com.thytrack.android.domain.model

import java.util.Date

/** 患者信息（DataStore 持久化），供 PDF 报告页眉使用。 */
data class PatientInfo(
    val name: String = "",
    val age: Int = 0,
    val surgeryDate: Date = Date(),
    val pathology: String = "",
)
