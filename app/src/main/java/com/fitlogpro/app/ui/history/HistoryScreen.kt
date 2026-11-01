package com.fitlogpro.app.ui.history

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.data.local.relation.WorkoutWithDetails
import com.fitlogpro.app.ui.components.EmptyStateView
import com.fitlogpro.app.ui.theme.AccentGold
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onWorkoutClick: (Long) -> Unit
) {
    val workouts by viewModel.workouts.collectAsState()
    val calendarDays by viewModel.calendarDays.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = List, 1 = Calendar
    var selectedCalendarDay by remember { mutableStateOf<CalendarWorkoutDay?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // View Mode Toggle Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabButton(
                    label = "History Log",
                    icon = Icons.Default.History,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    label = "Calendar",
                    icon = Icons.Default.CalendarMonth,
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // List View
                if (workouts.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.History,
                        title = "No Logged Workouts",
                        message = "Your completed workouts and personal records will appear here.",
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(workouts, key = { it.workout.id }) { item ->
                            WorkoutHistoryCard(
                                item = item,
                                onClick = { onWorkoutClick(item.workout.id) }
                            )
                        }
                    }
                }
            } else {
                // Calendar View
                CalendarViewSection(
                    year = selectedYear,
                    month = selectedMonth,
                    calendarDays = calendarDays,
                    allWorkouts = workouts,
                    selectedDay = selectedCalendarDay,
                    onSelectDay = { selectedCalendarDay = it },
                    onPrevMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onWorkoutClick = onWorkoutClick
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WorkoutHistoryCard(
    item: WorkoutWithDetails,
    onClick: () -> Unit
) {
    val workout = item.workout
    val dateFormat = SimpleDateFormat("EEEE, MMM d • h:mm a", Locale.US)
    val dateText = dateFormat.format(Date(workout.startTimeEpochMs))
    val durationMin = workout.durationSeconds / 60
    val prCount = item.exercises.flatMap { it.sets }.count { it.isPr }

    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workout.name,
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (prCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("$prCount PRs", color = Color(0xFFB45309), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = dateText, color = textSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics row: Duration & Total Volume
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = textSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${durationMin}m", color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Text(
                    text = "${String.format("%,.0f", workout.totalVolumeKg)} kg volume",
                    color = primaryColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${item.exercises.size} exercises",
                    color = textSecondary,
                    fontSize = 12.sp
                )
            }

            // Exercise names summary
            if (item.exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val exerciseNames = item.exercises.take(3).joinToString(", ") { it.exercise.name }
                val suffix = if (item.exercises.size > 3) " +${item.exercises.size - 3} more" else ""
                Text(
                    text = exerciseNames + suffix,
                    color = textSecondary.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CalendarViewSection(
    year: Int,
    month: Int,
    calendarDays: List<CalendarWorkoutDay>,
    allWorkouts: List<WorkoutWithDetails>,
    selectedDay: CalendarWorkoutDay?,
    onSelectDay: (CalendarWorkoutDay) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onWorkoutClick: (Long) -> Unit
) {
    val monthName = DateFormatSymbols.getInstance(Locale.US).months[month]
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxSize()) {
        // Month Selector Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month", tint = textPrimary)
            }
            Text(
                text = "$monthName $year",
                color = textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNextMonth) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", tint = textPrimary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weekday Column Headers
        val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            weekDays.forEach { dayName ->
                Text(
                    text = dayName,
                    color = textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        val firstDayCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed (Sunday = 0)

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Empty offset slots before the 1st
            items(firstDayOfWeek) {
                Box(modifier = Modifier.size(38.dp))
            }

            items(calendarDays) { day ->
                val isSelected = selectedDay?.epochDay == day.epochDay
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> primaryColor
                                day.hasWorkout -> MaterialTheme.colorScheme.primaryContainer
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                isSelected -> primaryColor
                                day.hasWorkout -> primaryColor.copy(alpha = 0.5f)
                                else -> Color.Transparent
                            },
                            CircleShape
                        )
                        .clickable { onSelectDay(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            color = when {
                                isSelected -> Color.White
                                day.hasWorkout -> primaryColor
                                else -> textPrimary
                            },
                            fontSize = 13.sp,
                            fontWeight = if (day.hasWorkout || isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (day.hasWorkout && !isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Day Workouts List
        if (selectedDay != null && selectedDay.hasWorkout) {
            Text(
                text = "Workouts on ${selectedDay.dayOfMonth} $monthName:",
                color = textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val cal = Calendar.getInstance()
            val dayWorkouts = allWorkouts.filter { w ->
                cal.timeInMillis = w.workout.startTimeEpochMs
                cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month && cal.get(Calendar.DAY_OF_MONTH) == selectedDay.dayOfMonth
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dayWorkouts) { w ->
                    WorkoutHistoryCard(item = w, onClick = { onWorkoutClick(w.workout.id) })
                }
            }
        }
    }
}
