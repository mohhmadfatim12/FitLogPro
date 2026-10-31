package com.fitlogpro.app.ui.workouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.data.local.entity.RoutineEntity
import com.fitlogpro.app.domain.model.ActiveWorkoutSession
import com.fitlogpro.app.ui.components.EmptyStateView
import com.fitlogpro.app.ui.theme.AccentOrange
import com.fitlogpro.app.ui.theme.AccentTeal
import com.fitlogpro.app.ui.theme.DarkBackground
import com.fitlogpro.app.ui.theme.DarkBorder
import com.fitlogpro.app.ui.theme.DarkSurface
import com.fitlogpro.app.ui.theme.DarkTextPrimary
import com.fitlogpro.app.ui.theme.DarkTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RoutineListScreen(
    viewModel: RoutinesViewModel,
    activeSession: ActiveWorkoutSession?,
    onStartEmptyWorkout: () -> Unit,
    onResumeActiveWorkout: () -> Unit,
    onStartRoutine: (Long) -> Unit,
    onRoutineDetail: (Long) -> Unit,
    onCreateRoutine: () -> Unit
) {
    val routines by viewModel.routines.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRoutine,
                containerColor = AccentOrange,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Routine")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Active Workout Recovery Alert Banner (Highest Priority)
            if (activeSession != null) {
                item {
                    ActiveRecoveryBanner(
                        session = activeSession,
                        onResume = onResumeActiveWorkout
                    )
                }
            }

            // 2. Quick Start Empty Workout Card
            item {
                QuickStartCard(onStart = onStartEmptyWorkout)
            }

            // 3. Section Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Routines",
                        color = DarkTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${routines.size} templates",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // 4. Routines List or Empty State
            if (routines.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.FitnessCenter,
                        title = "No Routines Yet",
                        message = "Build your custom workout routines with target sets, reps, and supersets.",
                        actionLabel = "Create Routine",
                        onActionClick = onCreateRoutine
                    )
                }
            } else {
                items(routines, key = { it.id }) { routine ->
                    RoutineCard(
                        routine = routine,
                        onCardClick = { onRoutineDetail(routine.id) },
                        onStart = { onStartRoutine(routine.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveRecoveryBanner(
    session: ActiveWorkoutSession,
    onResume: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF2C1E08))
            .border(1.5.dp, AccentOrange, RoundedCornerShape(14.dp))
            .clickable(onClick = onResume)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = AccentOrange,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Workout In Progress",
                        color = AccentOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${session.workoutName} • Tap to resume",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Resume", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun QuickStartCard(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Quick Start",
                    color = DarkTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Log an empty workout without a template",
                    color = DarkTextSecondary,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Start",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun RoutineCard(
    routine: RoutineEntity,
    onCardClick: () -> Unit,
    onStart: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d", Locale.US)
    val lastDoneText = routine.lastPerformedEpochMs?.let {
        "Last performed: ${dateFormat.format(Date(it))}"
    } ?: "Never performed"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onCardClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.name,
                    color = DarkTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (routine.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = routine.description,
                        color = DarkTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lastDoneText,
                    color = Color(0xFF6B7280),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onStart,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222731))
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Routine",
                    tint = AccentOrange
                )
            }
        }
    }
}
