package com.fitlogpro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.domain.model.MuscleGroupDistribution

val MuscleGroupColors = listOf(
    Color(0xFF2563EB), // Primary Royal Blue (Chest)
    Color(0xFF10B981), // Vibrant Emerald (Back)
    Color(0xFFF59E0B), // Trophy Gold (Legs)
    Color(0xFF8B5CF6), // Violet (Shoulders)
    Color(0xFFEC4899), // Pink (Arms)
    Color(0xFF06B6D4), // Cyan (Core)
    Color(0xFF64748B)  // Slate (Other)
)

@Composable
fun MuscleDistributionChart(
    distributions: List<MuscleGroupDistribution>,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .border(1.dp, outlineColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (distributions.isEmpty()) {
            Text(
                text = "No muscle distribution data available.\nLog workouts to view muscle group analytics.",
                color = textMuted,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )
        } else {
            Column {
                Text(
                    text = "Muscle Group Distribution",
                    color = textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Multi-segment horizontal bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                ) {
                    distributions.forEachIndexed { index, dist ->
                        val color = MuscleGroupColors[index % MuscleGroupColors.size]
                        val weightFraction = (dist.percentage / 100f).coerceIn(0.01f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(weightFraction)
                                .height(14.dp)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detailed Legend Grid
                Column {
                    distributions.forEachIndexed { index, dist ->
                        val color = MuscleGroupColors[index % MuscleGroupColors.size]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dist.muscleGroup.displayName,
                                color = textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${dist.totalSets} sets (${String.format("%.1f", dist.percentage)}%)",
                                color = textMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
