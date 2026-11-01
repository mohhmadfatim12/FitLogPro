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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.ui.theme.PrimaryBlue
import kotlin.math.max

data class BarChartItem(
    val label: String,
    val value: Double,
    val subtext: String = ""
)

@Composable
fun CustomBarChart(
    items: List<BarChartItem>,
    barColor: Color = PrimaryBlue,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .border(1.dp, outlineColor, RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Skeleton preview bars behind
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 32f
                    val topPadding = 20f
                    val availableHeight = height - bottomPadding - topPadding
                    val previewFractions = listOf(0.35f, 0.65f, 0.45f, 0.85f, 0.55f, 0.70f, 0.40f)
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    val barCount = previewFractions.size
                    val barSpacing = width * 0.05f
                    val totalSpacing = barSpacing * (barCount + 1)
                    val barWidth = (width - totalSpacing) / barCount

                    previewFractions.forEachIndexed { index, fraction ->
                        val x = barSpacing + index * (barWidth + barSpacing)
                        val barHeight = availableHeight * fraction
                        val y = topPadding + (availableHeight - barHeight)
                        drawRoundRect(
                            color = outlineColor.copy(alpha = 0.35f),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.LTGRAY
                                textSize = 24f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            drawText(days[index], x + barWidth / 2f, height - 6f, paint)
                        }
                    }
                }

                // Frosted center pill badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceColor.copy(alpha = 0.94f))
                        .border(1.dp, outlineColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(com.fitlogpro.app.ui.theme.ElectricLimeContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.BarChart,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(10.dp))
                        androidx.compose.foundation.layout.Column {
                            Text(
                                text = "Weekly Volume",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "Log your first session to track volume",
                                color = textMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val bottomPadding = 32f
                val topPadding = 20f
                val availableHeight = height - bottomPadding - topPadding

                val maxValue = max(1.0, items.maxOf { it.value })
                val barCount = items.size
                val barSpacing = width * 0.05f
                val totalSpacing = barSpacing * (barCount + 1)
                val barWidth = (width - totalSpacing) / barCount

                items.forEachIndexed { index, item ->
                    val x = barSpacing + index * (barWidth + barSpacing)
                    val barHeight = ((item.value / maxValue) * availableHeight).toFloat()
                    val y = topPadding + (availableHeight - barHeight)

                    // Draw Bar with rounded top
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Draw X-axis label
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 26f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        drawText(item.label, x + barWidth / 2f, height - 6f, paint)
                    }
                }
            }
        }
    }
}
