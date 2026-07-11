package com.thytrack.android.data.local.room

import com.thytrack.android.domain.model.Drug
import com.thytrack.android.domain.model.LabRecord
import com.thytrack.android.domain.model.MedicationChange
import com.thytrack.android.domain.model.RecordSource
import java.util.Date

fun LabRecordEntity.toDomain(): LabRecord = LabRecord(
    id = id, date = Date(date), hospital = hospital, notes = notes,
    createdAt = Date(createdAt), updatedAt = Date(updatedAt),
    source = RecordSource.fromValue(source), schemaVersion = schemaVersion,
    tsh = tsh, ft3 = ft3, ft4 = ft4, tt3 = tt3, tt4 = tt4, tg = tg, tgab = tgab, tpoab = tpoab,
    fpg = fpg, twoHpg = twoHpg, hba1c = hba1c,
    tc = tc, tgLipid = tgLipid, hdl = hdl, ldl = ldl,
    alt = alt, ast = ast, tbil = tbil,
    cr = cr, bun = bun, ua = ua, egfr = egfr,
    calcium = calcium, phosphorus = phosphorus, pth = pth, vitaminD = vitaminD,
    levothyroxineDose = levothyroxineDose, calciumDose = calciumDose, calcitriolDose = calcitriolDose,
    customRefRanges = customRefRanges ?: emptyMap(),
)

fun LabRecord.toEntity(): LabRecordEntity = LabRecordEntity(
    id = id, date = date.time, hospital = hospital, notes = notes,
    createdAt = createdAt.time, updatedAt = updatedAt.time,
    source = source.value, schemaVersion = schemaVersion,
    tsh = tsh, ft3 = ft3, ft4 = ft4, tt3 = tt3, tt4 = tt4, tg = tg, tgab = tgab, tpoab = tpoab,
    fpg = fpg, twoHpg = twoHpg, hba1c = hba1c,
    tc = tc, tgLipid = tgLipid, hdl = hdl, ldl = ldl,
    alt = alt, ast = ast, tbil = tbil,
    cr = cr, bun = bun, ua = ua, egfr = egfr,
    calcium = calcium, phosphorus = phosphorus, pth = pth, vitaminD = vitaminD,
    levothyroxineDose = levothyroxineDose, calciumDose = calciumDose, calcitriolDose = calcitriolDose,
    customRefRanges = customRefRanges.ifEmpty { null },
)

fun MedicationEntity.toDomain(): MedicationChange = MedicationChange(
    id = id, date = Date(date), drug = Drug.fromName(drug),
    oldDose = oldDose, newDose = newDose, reason = reason,
    createdAt = Date(createdAt), updatedAt = Date(updatedAt), recordId = recordId,
)

fun MedicationChange.toEntity(): MedicationEntity = MedicationEntity(
    id = id, date = date.time, drug = drug.name,
    oldDose = oldDose, newDose = newDose, reason = reason,
    createdAt = createdAt.time, updatedAt = updatedAt.time, recordId = recordId,
)
