package com.fitlogpro.app.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fitlogpro.app.domain.timer.RestTimerSnapshot
import com.fitlogpro.app.ui.theme.AccentOrange
import com.fitlogpro.app.ui.theme.DarkBorder
import com.fitlogpro.app.ui.theme.DarkSurface
import com.fitlogpro.app.ui.theme.DarkTextPrimary
import com.fitlogpro.app.ui.theme.DarkTextSecondary

@Composable
fun RestTimerDialog(
    snapshot: RestTimerSnapshot,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onAddTime: (Int) -> Unit,
    onSubtractTime: (Int) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit
) {
    val minutes = snapshot.remainingSeconds / 60
    val seconds = snapshot.remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rest Timer",
                        color = DarkTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DarkTextSecondary
                        )
                    }
                }

                if (snapshot.exerciseName != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = snapshot.exerciseName,
                        color = AccentOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Circular Timer Gauge
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                    CircularProgressIndicator(
                        progress = { snapshot.progressFraction },
                        modifier = Modifier.size(180.dp),
                        color = AccentOrange,
                        trackColor = DarkBorder,
                        strokeWidth = 10.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedTime,
                            color = if (snapshot.remainingSeconds <= 10) Color(0xFFFF5252) else DarkTextPrimary,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (snapshot.isPaused) "PAUSED" else "REMAINING",
                            color = DarkTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Increment / Decrement Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TimeAdjustmentChip("-15s") { onSubtractTime(15) }
                    TimeAdjustmentChip("+15s") { onAddTime(15) }
                    TimeAdjustmentChip("+30s") { onAddTime(30) }
                    TimeAdjustmentChip("+60s") { onAddTime(60) }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Control Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip
                    IconButton(
                        onClick = {
                            onSkip()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222731))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip",
                            tint = DarkTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Play / Pause Primary
                    IconButton(
                        onClick = { if (snapshot.isPaused) onResume() else onPause() },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AccentOrange)
                    ) {
                        Icon(
                            imageVector = if (snapshot.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Toggle Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeAdjustmentChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF222731))
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = DarkTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
