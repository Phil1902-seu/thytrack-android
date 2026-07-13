package com.thytrack.android.ui

import android.graphics.Paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.*
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.thytrack.android.data.local.MetricDefinitions
import com.thytrack.android.domain.model.LabRecord
import com.thytrack.android.domain.model.MedicationChange
import com.thytrack.android.util.LabRecordFields
import com.thytrack.android.util.ValueValidator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Vico 趋势图封装（Phase 3）。
 * - 正常蓝线 + 数据点；异常点用红色叠加层强调。
 * - 参考范围半透明绿带（自定义 Decoration，绘制于图层之下）。
 * - 用药变更竖线标记（自定义 Decoration）。
 * - TSH 指标叠加优甲乐剂量右轴（双轴）。
 * - 点选 Tooltip（默认 CartesianMarker）。
 */
@Composable
fun MetricChart(
    records: List<LabRecord>,
    medications: List<MedicationChange>,
    metricKey: String,
    modifier: Modifier = Modifier,
) {
    val field = MetricDefinitions.ALL.firstOrNull { it.key == metricKey }
        ?: MetricDefinitions.ALL.first()
    val ref = field.defaultRefRange
    val isTsh = metricKey == "tsh"

    val points = remember(records, metricKey) {
        records.mapIndexedNotNull { i, r ->
            val v = LabRecordFields.get(r, metricKey)
            if (v != null) i to v else null
        }
    }
    val xList = points.map { it.first.toDouble() }
    val yList = points.map { it.second }

    val abnormalPoints = remember(points, records, metricKey) {
        points.filter { (i, v) -> ValueValidator.isAbnormal(v, metricKey, records[i].customRefRanges) }
    }
    val abnormalX = abnormalPoints.map { it.first.toDouble() }
    val abnormalY = abnormalPoints.map { it.second }

    val dosePoints = remember(records) {
        records.mapIndexedNotNull { i, r ->
            if (r.levothyroxineDose != null) i to r.levothyroxineDose else null
        }
    }
    val doseX = dosePoints.map { it.first.toDouble() }
    val doseY = dosePoints.map { it.second }

    // Vico 的 lineSeries {} 不允许传入空列表，否则抛 IllegalArgumentException: Series can't be empty。
    // 因此必须预先判断每条序列是否有数据；为空时用透明占位点 (placeholderX, placeholderY) 替代，
    // 既保证模型始终合法（≥1 条序列），又通过透明线/点保持图层与配色索引对齐、且不产生视觉噪点。
    val hasMain = xList.isNotEmpty() && yList.isNotEmpty()
    val hasAbnormal = abnormalX.isNotEmpty() && abnormalY.isNotEmpty()
    val hasDose = isTsh && doseX.isNotEmpty() && doseY.isNotEmpty()
    val placeholderX = 0.0
    val placeholderY = if (ref.high > ref.low) (ref.low + ref.high) / 2.0 else 0.0

    // 没有任何可绘制的序列（无检验记录 / 所选指标无数值 / TSH 无剂量）时，直接展示空状态，
    // 不构建 Vico 图表，彻底规避任何空数据 / 边界区间导致的崩溃。
    val hasAnyData = hasMain || hasDose
    if (!hasAnyData) {
        Box(
            modifier = modifier.fillMaxWidth().height(280.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "暂无检验数据，无法绘制趋势",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        return
    }

    val dateFmt = remember { SimpleDateFormat("yy/MM/dd", Locale.CHINA) }
    val xLabels = remember(records) { records.map { dateFmt.format(it.date) } }
    val medXs = remember(records, medications) { medications.map { xForDate(records, it.date.time) } }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(points, abnormalPoints, dosePoints, isTsh) {
        modelProducer.runTransaction {
            // 序列数量必须与图层数一致（main + abnormal + [dose]），故空序列用占位点补齐而非跳过。
            if (hasMain) lineSeries { series(xList, yList) }
            else lineSeries { series(listOf(placeholderX), listOf(placeholderY)) }

            if (hasAbnormal) lineSeries { series(abnormalX, abnormalY) }
            else lineSeries { series(listOf(placeholderX), listOf(placeholderY)) }

            // dose 图层仅 TSH 存在，故 dose 序列也仅在此条件下发射，避免序列数/图层数不一致。
            if (isTsh) {
                if (hasDose) lineSeries { series(doseX, doseY) }
                else lineSeries { series(listOf(placeholderX), listOf(placeholderY)) }
            }
        }
    }

    val mainColor = if (hasMain) Color(0xFF4A90D9) else Color.Transparent
    val abnormalColor = if (hasAbnormal) Color(0xFFE53935) else Color.Transparent
    val doseColor = if (hasDose) Color(0xFF8E24AA) else Color.Transparent

    val bottomAxis = HorizontalAxis.rememberBottom(
        valueFormatter = CartesianValueFormatter { _, value, _ ->
            val idx = value.toInt().coerceIn(0, xLabels.lastIndex.coerceAtLeast(0))
            xLabels.getOrElse(idx) { "" }
        },
    )
    val startAxis = VerticalAxis.rememberStart(
        valueFormatter = CartesianValueFormatter { _, value, _ -> "%.1f".format(Locale.US, value) },
    )

    val mainLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(mainColor)),
        pointProvider = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.point(ShapeComponent(fill = fill(mainColor), shape = Shape.Rectangle), 5.dp),
        ),
    )
    val abnormalLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(Color.Transparent)),
        pointProvider = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.point(ShapeComponent(fill = fill(abnormalColor), shape = Shape.Rectangle), 7.dp),
        ),
    )

    val mainLayer = rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(listOf(mainLine)),
    )
    val abnormalLayer = rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(listOf(abnormalLine)),
    )
    val doseLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(doseColor)),
        pointProvider = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.point(
                ShapeComponent(fill = fill(doseColor), shape = Shape.Rectangle),
                5.dp,
            ),
        ),
    )
    val doseLayer = rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(listOf(doseLine)),
        verticalAxisPosition = Axis.Position.Vertical.End,
    )

    val layers = buildList {
        add(mainLayer)
        add(abnormalLayer)
        if (isTsh) add(doseLayer)
    }

    val endAxis = if (isTsh) {
        VerticalAxis.rememberEnd(
            valueFormatter = CartesianValueFormatter { _, value, _ -> "%.0f".format(Locale.US, value) },
        )
    } else {
        null
    }

    val marker = rememberDefaultCartesianMarker(label = rememberAxisLabelComponent())

    val decorations = remember(ref.low, ref.high, medXs) {
        listOf<Decoration>(
            ReferenceBandDecoration(ref.low, ref.high, 0x334CAF50.toInt()),
            MedicationMarkerDecoration(medXs, 0x809E9E9E.toInt()),
        )
    }

    val chart = rememberCartesianChart(
        *layers.toTypedArray(),
        startAxis = startAxis,
        bottomAxis = bottomAxis,
        endAxis = endAxis,
        marker = marker,
        decorations = decorations,
    )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth().height(280.dp),
    )
}

/** 参考范围半透明绿带（绘制于图层之下）。 */
private class ReferenceBandDecoration(
    private val low: Double,
    private val high: Double,
    private val argb: Int,
) : Decoration {
    private val paint = Paint().apply {
        color = argb
        style = Paint.Style.FILL
    }

    override fun drawUnderLayers(context: CartesianDrawingContext) {
        if (high <= low) return
        val b = context.layerBounds
        // getYRange 在 Vico 2.1.0 中为非空返回；图表数据提交后绘制阶段可安全直接取值。
        val yRange = context.ranges.getYRange(Axis.Position.Vertical.Start)
        val len = yRange.length
        if (len <= 0.0) return
        val yLow = b.bottom - (((low - yRange.minY) / len) * b.height()).toFloat()
        val yHigh = b.bottom - (((high - yRange.minY) / len) * b.height()).toFloat()
        context.canvas.drawRect(b.left, yHigh, b.right, yLow, paint)
    }
}

/** 用药变更竖线标记（绘制于图层之下）。x 位置为记录索引空间的插值。 */
private class MedicationMarkerDecoration(
    private val xPositions: List<Double>,
    private val argb: Int,
) : Decoration {
    private val paint = Paint().apply {
        color = argb
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    override fun drawUnderLayers(context: CartesianDrawingContext) {
        val b = context.layerBounds
        val span = context.ranges.maxX - context.ranges.minX
        if (span <= 0.0 || xPositions.isEmpty()) return
        for (xp in xPositions) {
            val px = b.left + (((xp - context.ranges.minX) / span) * b.width()).toFloat()
            context.canvas.drawLine(px, b.top, px, b.bottom, paint)
        }
    }
}

/** 将日期时间戳插值到记录索引空间（0..n-1），用于对齐用药竖线标记。 */
private fun xForDate(records: List<LabRecord>, time: Long): Double {
    if (records.isEmpty()) return 0.0
    val times = records.map { it.date.time }
    if (time <= times.first()) return 0.0
    if (time >= times.last()) return records.lastIndex.toDouble()
    for (i in 0 until records.lastIndex) {
        if (time >= times[i] && time <= times[i + 1]) {
            val span = (times[i + 1] - times[i]).toDouble()
            val frac = if (span == 0.0) 0.0 else (time - times[i]) / span
            return i + frac
        }
    }
    return 0.0
}
