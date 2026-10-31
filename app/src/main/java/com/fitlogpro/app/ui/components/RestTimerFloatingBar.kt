package com.fitlogpro.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.domain.timer.RestTimerSnapshot
import com.fitlogpro.app.ui.theme.AccentOrange
import com.fitlogpro.app.ui.theme.AccentTeal
import com.fitlogpro.app.ui.theme.DarkBorder
import com.fitlogpro.app.ui.theme.DarkSurface
import com.fitlogpro.app.ui.theme.DarkTextPrimary
import com.fitlogpro.app.ui.theme.DarkTextSecondary

@Composable
fun RestTimerFloatingBar(
    snapshot: RestTimerSnapshot,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onAddTime: (Int) -> Unit,
    onSkip: () -> Unit,
    onOpenModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = snapshot.isRunning || snapshot.isPaused

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        val minutes = snapshot.remainingSeconds / 60
        val seconds = snapshot.remainingSeconds % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .border(1.dp, AccentOrange.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .clickable(onClick = onOpenModal)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AccentOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (snapshot.isPaused) "Timer Paused" else (snapshot.exerciseName?.let { "Rest: $it" } ?: "Resting..."),
                            color = DarkTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                        Text(
                            text = timeFormatted,
                            color = if (snapshot.remainingSeconds <= 10) Color(0xFFFF5252) else DarkTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // +30s Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF222731))
                            .clickable { onAddTime(30) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+30s",
                            color = AccentTeal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Pause / Resume
                    IconButton(
                        onClick = { if (snapshot.isPaused) onResume() else onPause() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (snapshot.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Toggle Rest Timer",
                            tint = DarkTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Skip Button
                    IconButton(
                        onClick = onSkip,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Skip Timer",
                            tint = DarkTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { snapshot.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = AccentOrange,
                    trackColor = DarkBorder,
                )
            }
        }
    }
}
