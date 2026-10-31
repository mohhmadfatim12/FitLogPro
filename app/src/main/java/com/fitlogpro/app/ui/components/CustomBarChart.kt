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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.ui.theme.AccentTeal
import com.fitlogpro.app.ui.theme.DarkBorder
import com.fitlogpro.app.ui.theme.DarkSurface
import com.fitlogpro.app.ui.theme.DarkTextTertiary
import kotlin.math.max

data class BarChartItem(
    val label: String,
    val value: Double,
    val subtext: String? = null
)

@Composable
fun CustomBarChart(
    items: List<BarChartItem>,
    barColor: Color = AccentTeal,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (items.isEmpty()) {
            Text(
                text = "No volume data logged yet.\nLog workouts to view weekly volume chart.",
                color = DarkTextTertiary,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            return@Box
        }

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
