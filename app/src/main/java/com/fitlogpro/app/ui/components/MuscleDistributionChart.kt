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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.domain.model.MuscleGroupDistribution


private val MuscleGroupColors = listOf(
    Color(0xFFFF6D00), // Chest - Orange
    Color(0xFF00E5FF), // Back - Teal
    Color(0xFF7C4DFF), // Quads - Purple
    Color(0xFFFFD600), // Shoulders - Yellow
    Color(0xFF00E676), // Hamstrings - Green
    Color(0xFFFF4081), // Biceps - Pink
    Color(0xFF3D5AFE), // Triceps - Indigo
    Color(0xFF00B0FF), // Glutes - Light Blue
    Color(0xFFFFAB00), // Calves - Amber
    Color(0xFF69F0AE), // Core - Mint
    Color(0xFFB0BEC5)  // Other/Cardio - Slate
)

@Composable
fun MuscleDistributionChart(
    distributions: List<MuscleGroupDistribution>,
    modifier: Modifier = Modifier
) {
    val surfaceColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
    val outlineColor = androidx.compose.material3.MaterialTheme.colorScheme.outline
    val textPrimary = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    val textMuted = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .border(1.dp, outlineColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
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
            return@Box
        }

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
