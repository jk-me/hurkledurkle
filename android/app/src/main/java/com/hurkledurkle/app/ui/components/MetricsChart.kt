package com.hurkledurkle.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hurkledurkle.app.data.model.DayMetrics
import com.hurkledurkle.app.ui.theme.HurkleDurkleColor
import com.hurkledurkle.app.ui.theme.PreSleepColor
import com.hurkledurkle.app.ui.theme.RestColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val monthDayFmt = DateTimeFormatter.ofPattern("M/d")

/**
 * Grouped bar chart for calculated sleep metrics: rest time, pre-sleep time, hurkle-durkle time.
 * Y-axis: hours. Each day has three side-by-side bars (one per metric).
 */
@Composable
fun MetricsChart(
    dayMetrics: List<DayMetrics>,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    Column {
        Canvas(modifier = modifier) {
            if (dayMetrics.isEmpty()) return@Canvas

            val leftPad = 36.dp.toPx()
            val rightPad = 8.dp.toPx()
            val topPad = 8.dp.toPx()
            val bottomPad = 22.dp.toPx()
            val chartW = size.width - leftPad - rightPad
            val chartH = size.height - topPad - bottomPad

            val allMins = dayMetrics.flatMap { d ->
                listOfNotNull(d.restMinutes, d.preSleepMinutes, d.hurkledurkleMinutes)
            }
            if (allMins.isEmpty()) return@Canvas

            // Round y-max up to nearest whole hour, with a ceiling of at least 1 h
            val maxMinutes = allMins.max()
            val yMaxMins = ((maxMinutes / 60 + 1) * 60).coerceAtLeast(60L)
            val yMaxHours = yMaxMins / 60f

            fun yOf(minutes: Long) =
                topPad + (1f - minutes.toFloat() / yMaxMins) * chartH

            // Horizontal grid lines every hour
            for (h in 0..yMaxHours.toInt()) {
                val yPx = topPad + (1f - h.toFloat() / yMaxHours) * chartH
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, yPx),
                    end = Offset(leftPad + chartW, yPx),
                    strokeWidth = 0.5.dp.toPx()
                )
                val measured = textMeasurer.measure(
                    "${h}h",
                    style = TextStyle(fontSize = 9.sp, color = labelColor)
                )
                drawText(
                    measured,
                    topLeft = Offset(leftPad - measured.size.width - 3.dp.toPx(), yPx - measured.size.height / 2f)
                )
            }

            // Bars
            val n = dayMetrics.size
            val groupW = chartW / n
            val gap = 1.dp.toPx()
            val barW = ((groupW - gap * 4) / 3).coerceAtLeast(1f)
            val barColors = listOf(RestColor, PreSleepColor, HurkleDurkleColor)

            val labelStep = when {
                n <= 10 -> 1; n <= 21 -> 2; n <= 45 -> 7; else -> 14
            }

            dayMetrics.forEachIndexed { i, d ->
                val xBase = leftPad + i * groupW + gap
                val values = listOf(d.restMinutes, d.preSleepMinutes, d.hurkledurkleMinutes)
                values.forEachIndexed { bi, minutes ->
                    if (minutes != null && minutes > 0) {
                        val barH = minutes.toFloat() / yMaxMins * chartH
                        drawRect(
                            color = barColors[bi],
                            topLeft = Offset(xBase + bi * (barW + gap), topPad + chartH - barH),
                            size = Size(barW, barH)
                        )
                    }
                }

                if (i % labelStep == 0) {
                    val date = LocalDate.parse(d.date)
                    val measured = textMeasurer.measure(
                        monthDayFmt.format(date),
                        style = TextStyle(fontSize = 9.sp, color = labelColor, textAlign = TextAlign.Center)
                    )
                    drawText(
                        measured,
                        topLeft = Offset(
                            xBase + groupW / 2 - measured.size.width / 2f,
                            topPad + chartH + 3.dp.toPx()
                        )
                    )
                }
            }
        }

        ChartLegend(
            items = listOf(
                "Rest" to RestColor,
                "Pre-sleep" to PreSleepColor,
                "Hurkle-durkle" to HurkleDurkleColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
        )
    }
}
