package com.hurkledurkle.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hurkledurkle.app.data.model.DayMetrics
import com.hurkledurkle.app.ui.theme.RiseColor
import com.hurkledurkle.app.ui.theme.SleepColor
import com.hurkledurkle.app.ui.theme.WakeColor
import com.hurkledurkle.app.ui.theme.WindDownColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val monthDayFmt = DateTimeFormatter.ofPattern("M/d")

/**
 * Line chart that plots wind-down, sleep, wake, and rise times across days.
 *
 * Y-axis: time of day in minutes from midnight.
 * Values before [MIDNIGHT_THRESHOLD] minutes are assumed to be "past midnight" and are
 * normalised by adding 1440, so that 1 AM plots after 11 PM on the same axis.
 */
@Composable
fun SleepLineChart(
    dayMetrics: List<DayMetrics>,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    Column {
        Canvas(modifier = modifier) {
            if (dayMetrics.isEmpty()) return@Canvas

            val leftPad = 40.dp.toPx()
            val rightPad = 8.dp.toPx()
            val topPad = 8.dp.toPx()
            val bottomPad = 22.dp.toPx()
            val chartW = size.width - leftPad - rightPad
            val chartH = size.height - topPad - bottomPad
            val n = dayMetrics.size

            // Normalise minutes so that early-morning hours plot above midnight
            val allRaw = dayMetrics.flatMap { d ->
                listOfNotNull(d.windDownMinutes, d.sleepMinutes, d.wakeMinutes, d.riseMinutes)
            }
            if (allRaw.isEmpty()) return@Canvas

            fun normalize(m: Int) = if (m < MIDNIGHT_THRESHOLD) m + 1440 else m

            val normalised = allRaw.map { normalize(it) }
            val yMin = ((normalised.min() - 30).coerceAtLeast(0) / 60) * 60
            val yMax = ((normalised.max() + 90) / 60) * 60

            fun xOf(i: Int) = leftPad + i.toFloat() / (n - 1).coerceAtLeast(1) * chartW
            fun yOf(raw: Int): Float {
                val norm = normalize(raw)
                return topPad + (1f - (norm - yMin).toFloat() / (yMax - yMin)) * chartH
            }

            // Horizontal grid lines every 2 hours
            var gridMin = yMin
            while (gridMin <= yMax) {
                val yPx = topPad + (1f - (gridMin - yMin).toFloat() / (yMax - yMin)) * chartH
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, yPx),
                    end = Offset(leftPad + chartW, yPx),
                    strokeWidth = 0.5.dp.toPx()
                )
                val displayMin = gridMin % 1440
                val hour = displayMin / 60
                val h = if (hour % 12 == 0) 12 else hour % 12
                val ampm = if (hour < 12) "a" else "p"
                val measured = textMeasurer.measure(
                    "${h}${ampm}",
                    style = TextStyle(fontSize = 9.sp, color = labelColor)
                )
                drawText(
                    measured,
                    topLeft = Offset(leftPad - measured.size.width - 3.dp.toPx(), yPx - measured.size.height / 2f)
                )
                gridMin += 120
            }

            // X-axis date labels
            val labelStep = when {
                n <= 10 -> 1; n <= 21 -> 2; n <= 45 -> 7; else -> 14
            }
            for (i in 0 until n step labelStep) {
                val date = LocalDate.parse(dayMetrics[i].date)
                val measured = textMeasurer.measure(
                    monthDayFmt.format(date),
                    style = TextStyle(fontSize = 9.sp, color = labelColor, textAlign = TextAlign.Center)
                )
                drawText(
                    measured,
                    topLeft = Offset(xOf(i) - measured.size.width / 2f, topPad + chartH + 3.dp.toPx())
                )
            }

            // Draw series: replace local function with a loop so DrawScope methods are in scope
            val allSeries = listOf(
                WindDownColor to { d: DayMetrics -> d.windDownMinutes },
                SleepColor to { d: DayMetrics -> d.sleepMinutes },
                WakeColor to { d: DayMetrics -> d.wakeMinutes },
                RiseColor to { d: DayMetrics -> d.riseMinutes }
            )
            for ((color, getValue) in allSeries) {
                val pts = dayMetrics.mapIndexedNotNull { i, d ->
                    getValue(d)?.let { Offset(xOf(i), yOf(it)) }
                }
                if (pts.size >= 2) {
                    val path = Path().apply {
                        moveTo(pts[0].x, pts[0].y)
                        pts.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(path, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                    pts.forEach { drawCircle(color, radius = 3.dp.toPx(), center = it) }
                } else {
                    pts.forEach { drawCircle(color, radius = 4.dp.toPx(), center = it) }
                }
            }
        }

        // Legend row
        ChartLegend(
            items = listOf(
                "Wind-down" to WindDownColor,
                "Sleep" to SleepColor,
                "Wake" to WakeColor,
                "Rise" to RiseColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ChartLegend(items: List<Pair<String, Color>>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
        items.forEach { (label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color, radius = size.minDimension / 2)
                }
                Spacer(Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private const val MIDNIGHT_THRESHOLD = 480 // 8 AM: values before this are treated as post-midnight
