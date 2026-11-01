package com.fitlogpro.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.domain.model.DailyReadiness
import com.fitlogpro.app.domain.model.MuscleRecoveryStatus
import com.fitlogpro.app.domain.model.RecoveryState
import com.fitlogpro.app.ui.theme.ElectricLime
import com.fitlogpro.app.ui.theme.ElectricLimeDark
import com.fitlogpro.app.ui.theme.ElectricLimeText

@Composable
fun MuscleRecoveryHeatmap(
    readiness: DailyReadiness,
    recoveryList: List<MuscleRecoveryStatus>,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    // Soreness / Joint check-in state
    val defaultJoints = listOf(
        "Left Shoulder",
        "Right Shoulder",
        "Lower Back",
        "Left Knee",
        "Right Knee",
        "Elbows",
        "Wrists"
    )
    var tenderJoints by remember { mutableStateOf(setOf<String>()) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. Daily Readiness Gauge Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(surfaceColor)
                .border(1.dp, outlineColor, RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricLime.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = ElectricLimeDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Daily Readiness Score",
                                color = textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = readiness.headline,
                                color = if (readiness.score >= 85) Color(0xFF10B981) else if (readiness.score >= 65) Color(0xFFF59E0B) else Color(0xFFEF4444),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Circular arc score badge
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val scoreColor = if (readiness.score >= 85) ElectricLimeDark else if (readiness.score >= 65) Color(0xFFF59E0B) else Color(0xFFEF4444)
                        Canvas(modifier = Modifier.size(54.dp)) {
                            drawArc(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                            )
                            val sweep = (readiness.score / 100f) * 270f
                            drawArc(
                                color = scoreColor,
                                startAngle = 135f,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "${readiness.score}",
                            color = textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Status breakdown pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReadinessPill(label = "Fresh", count = readiness.freshMuscleCount, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                    ReadinessPill(label = "Recovering", count = readiness.recoveringMuscleCount, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                    ReadinessPill(label = "Fatigued", count = readiness.fatiguedMuscleCount, color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
                }

                // Recommendation note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = readiness.recommendation,
                            color = textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // 2. Muscle Recovery Heatmap Breakdown
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Muscle Group Recovery",
                color = textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            recoveryList.forEach { item ->
                MuscleRecoveryCard(item = item)
            }
        }

        // 3. Pre-Workout Soreness & Joint Check-In
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(surfaceColor)
                .border(1.dp, outlineColor, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = if (tenderJoints.isNotEmpty()) Color(0xFFF59E0B) else Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Joint & Soreness Check-In",
                        color = textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Tap any joints or areas feeling stiff or tender today to adapt your training safety:",
                    color = textSecondary,
                    fontSize = 12.sp
                )

                // Multi-row joint pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    defaultJoints.take(4).forEach { joint ->
                        val isTender = tenderJoints.contains(joint)
                        JointChip(
                            name = joint,
                            isTender = isTender,
                            onClick = {
                                tenderJoints = if (isTender) tenderJoints - joint else tenderJoints + joint
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    defaultJoints.drop(4).forEach { joint ->
                        val isTender = tenderJoints.contains(joint)
                        JointChip(
                            name = joint,
                            isTender = isTender,
                            onClick = {
                                tenderJoints = if (isTender) tenderJoints - joint else tenderJoints + joint
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                AnimatedVisibility(visible = tenderJoints.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Caution on: ${tenderJoints.joinToString(", ")}. Warm up joints with 2 light ramp-up sets and avoid explosive lockouts.",
                                color = Color(0xFF92400E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadinessPill(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$count $label",
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MuscleRecoveryCard(item: MuscleRecoveryStatus) {
    val stateColor = Color(item.state.colorHex)
    val cardBg = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.muscleGroup.displayName,
                    color = textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(stateColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${item.recoveryPercentage}% • ${item.state.displayName}",
                        color = stateColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Visual Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val fraction = (item.recoveryPercentage / 100f).coerceIn(0.01f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(stateColor)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val timeText = item.hoursSinceLastTrained?.let { "${it}h ago (${item.recentSetsCount} sets)" } ?: "Rested (> 7 days)"
                Text(
                    text = timeText,
                    color = textSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = item.recommendation,
                    color = textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun JointChip(
    name: String,
    isTender: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isTender) Color(0xFFF59E0B) else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isTender) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = if (isTender) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}
