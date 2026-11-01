package com.fitlogpro.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

data class ChartDataPoint(
    val label: String,
    val value: Double
)

@Composable
fun CustomLineChart(
    points: List<ChartDataPoint>,
    unitSuffix: String = "kg",
    lineColor: Color = AccentOrange,
    modifier: Modifier = Modifier
) {
    val surfaceColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
    val outlineColor = androidx.compose.material3.MaterialTheme.colorScheme.outline
    val textMuted = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .border(1.dp, outlineColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Critical requirement: empty history safety
        if (points.isEmpty()) {
            Text(
                text = "No history recorded yet.\nComplete workouts to view progress chart.",
                color = textMuted,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            return@Box
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val bottomPadding = 30f
            val topPadding = 20f
            val graphHeight = height - bottomPadding - topPadding

            val rawMin = points.minOf { it.value }
            val rawMax = points.maxOf { it.value }

            // Guard against identical min and max to prevent divide-by-zero
            val minVal = if (rawMin == rawMax) max(0.0, rawMin - 10.0) else rawMin * 0.9
            val maxVal = if (rawMin == rawMax) rawMax + 10.0 else rawMax * 1.1
            val valueRange = maxVal - minVal

            val stepX = if (points.size > 1) width / (points.size - 1) else width / 2

            // Draw horizontal subtle grid lines (3 levels)
            for (i in 0..2) {
                val gridY = topPadding + (graphHeight / 2f) * i
                drawLine(
                    color = outlineColor,
                    start = Offset(0f, gridY),
                    end = Offset(width, gridY),
                    strokeWidth = 1f
                )
            }

            val path = Path()
            val fillPath = Path()
            val offsets = mutableListOf<Offset>()

            points.forEachIndexed { index, point ->
                val x = if (points.size > 1) index * stepX else width / 2
                val normalizedY = ((point.value - minVal) / valueRange).toFloat()
                val y = topPadding + (graphHeight * (1f - normalizedY))
                val offset = Offset(x, y)
                offsets.add(offset)

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height - bottomPadding)
                    fillPath.lineTo(x, y)
                } else {
                    val prevOffset = offsets[index - 1]
                    val controlX = (prevOffset.x + x) / 2
                    path.cubicTo(controlX, prevOffset.y, controlX, y, x, y)
                    fillPath.cubicTo(controlX, prevOffset.y, controlX, y, x, y)
                }

                if (index == points.size - 1) {
                    fillPath.lineTo(x, height - bottomPadding)
                    fillPath.close()
                }
            }

            // Draw gradient fill under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.35f),
                        lineColor.copy(alpha = 0.0f)
                    ),
                    startY = topPadding,
                    endY = height - bottomPadding
                )
            )

            // Draw line curve
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw data points
            offsets.forEachIndexed { idx, offset ->
                drawCircle(
                    color = lineColor,
                    radius = 5.dp.toPx(),
                    center = offset
                )
                drawCircle(
                    color = surfaceColor,
                    radius = 2.5.dp.toPx(),
                    center = offset
                )

                // Draw X-axis label for first, middle, last points
                if (idx == 0 || idx == points.size - 1 || idx == points.size / 2) {
                    drawContext.canvas.nativeCanvas.apply {
                        val text = points[idx].label
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 28f
                            textAlign = when (idx) {
                                0 -> android.graphics.Paint.Align.LEFT
                                points.size - 1 -> android.graphics.Paint.Align.RIGHT
                                else -> android.graphics.Paint.Align.CENTER
                            }
                            isAntiAlias = true
                        }
                        drawText(text, offset.x, height - 5f, paint)
                    }
                }
            }
        }
    }
}
