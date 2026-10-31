package com.fitlogpro.app.ui.exercises

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.domain.model.Equipment
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.MuscleGroup
import com.fitlogpro.app.ui.theme.AccentOrange
import com.fitlogpro.app.ui.theme.DarkBackground
import com.fitlogpro.app.ui.theme.DarkBorder
import com.fitlogpro.app.ui.theme.DarkSurface
import com.fitlogpro.app.ui.theme.DarkTextPrimary
import com.fitlogpro.app.ui.theme.DarkTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExerciseScreen(
    viewModel: ExerciseViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf(MuscleGroup.CHEST) }
    var selectedEquipment by remember { mutableStateOf(Equipment.BARBELL) }
    var selectedType by remember { mutableStateOf(ExerciseType.WEIGHT_AND_REPS) }
    var restSeconds by remember { mutableIntStateOf(90) }
    var instructions by remember { mutableStateOf("") }

    var showMuscleMenu by remember { mutableStateOf(false) }
    var showEquipMenu by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("New Custom Exercise", color = DarkTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkTextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.saveCustomExercise(
                                    name = name,
                                    muscleGroup = selectedMuscle,
                                    equipment = selectedEquipment,
                                    exerciseType = selectedType,
                                    defaultRestSeconds = restSeconds,
                                    instructions = instructions,
                                    onSuccess = onSaved
                                )
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save", tint = if (name.isNotBlank()) AccentOrange else Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Muscle Group Picker Dropdown
            item {
                Column {
                    Text("Target Muscle Group", color = DarkTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { showMuscleMenu = true }
                                .padding(14.dp)
                        ) {
                            Text(selectedMuscle.displayName, color = DarkTextPrimary, fontSize = 14.sp)
                        }
                        DropdownMenu(
                            expanded = showMuscleMenu,
                            onDismissRequest = { showMuscleMenu = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            MuscleGroup.entries.forEach { muscle ->
                                DropdownMenuItem(
                                    text = { Text(muscle.displayName, color = DarkTextPrimary) },
                                    onClick = {
                                        selectedMuscle = muscle
                                        showMuscleMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Equipment Picker Dropdown
            item {
                Column {
                    Text("Equipment", color = DarkTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { showEquipMenu = true }
                                .padding(14.dp)
                        ) {
                            Text(selectedEquipment.displayName, color = DarkTextPrimary, fontSize = 14.sp)
                        }
                        DropdownMenu(
                            expanded = showEquipMenu,
                            onDismissRequest = { showEquipMenu = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            Equipment.entries.forEach { equip ->
                                DropdownMenuItem(
                                    text = { Text(equip.displayName, color = DarkTextPrimary) },
                                    onClick = {
                                        selectedEquipment = equip
                                        showEquipMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Exercise Modality Type
            item {
                Column {
                    Text("Modality / Exercise Type", color = DarkTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { showTypeMenu = true }
                                .padding(14.dp)
                        ) {
                            Text(selectedType.displayName, color = DarkTextPrimary, fontSize = 14.sp)
                        }
                        DropdownMenu(
                            expanded = showTypeMenu,
                            onDismissRequest = { showTypeMenu = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            ExerciseType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName, color = DarkTextPrimary) },
                                    onClick = {
                                        selectedType = type
                                        showTypeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Default Rest Timer Selector
            item {
                Column {
                    Text("Default Rest Timer", color = DarkTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(45, 60, 90, 120, 180).forEach { sec ->
                            val isSel = restSeconds == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) AccentOrange else Color(0xFF222731))
                                    .border(1.dp, if (isSel) AccentOrange else DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable { restSeconds = sec }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${sec}s",
                                    color = if (isSel) Color.Black else DarkTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Instructions text area
            item {
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions / Coaching Cues") },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentOrange,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Submit Button
            item {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.saveCustomExercise(
                                name = name,
                                muscleGroup = selectedMuscle,
                                equipment = selectedEquipment,
                                exerciseType = selectedType,
                                defaultRestSeconds = restSeconds,
                                instructions = instructions,
                                onSuccess = onSaved
                            )
                        }
                    },
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Create Exercise", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
