package com.fitlogpro.app.ui.settings

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.data.backup.BackupValidationResult
import com.fitlogpro.app.domain.model.DistanceUnit
import com.fitlogpro.app.domain.model.WeightUnit
import com.fitlogpro.app.ui.theme.AccentOrange
import com.fitlogpro.app.ui.theme.AccentTeal
import com.fitlogpro.app.ui.theme.DarkBackground
import com.fitlogpro.app.ui.theme.DarkBorder
import com.fitlogpro.app.ui.theme.DarkSurface
import com.fitlogpro.app.ui.theme.DarkTextPrimary
import com.fitlogpro.app.ui.theme.DarkTextSecondary
import com.fitlogpro.app.ui.theme.DarkTextTertiary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val weightUnit by viewModel.weightUnit.collectAsState()
    val distanceUnit by viewModel.distanceUnit.collectAsState()
    val darkTheme by viewModel.darkTheme.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val defaultRestSeconds by viewModel.defaultRestSeconds.collectAsState()
    val restVibration by viewModel.restVibration.collectAsState()
    val restSound by viewModel.restSound.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var showExportResultDialog by remember { mutableStateOf<Pair<String, String>?>(null) } // Title to content
    var showRestoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Settings & Data Safety",
                    color = DarkTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Units & Preferences Section
            item {
                SettingsSection(title = "Units & Measurements") {
                    // Weight Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Weight Unit", color = DarkTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Applies to exercises, 1RM, volume, and body weight", color = DarkTextSecondary, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF222731))
                                .padding(2.dp)
                        ) {
                            UnitToggleChip("kg", weightUnit == WeightUnit.KG) { viewModel.setWeightUnit(WeightUnit.KG) }
                            UnitToggleChip("lbs", weightUnit == WeightUnit.LBS) { viewModel.setWeightUnit(WeightUnit.LBS) }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Distance Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Distance Unit", color = DarkTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Applies to cardio running, cycling, and rowing", color = DarkTextSecondary, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF222731))
                                .padding(2.dp)
                        ) {
                            UnitToggleChip("km", distanceUnit == DistanceUnit.KM) { viewModel.setDistanceUnit(DistanceUnit.KM) }
                            UnitToggleChip("mi", distanceUnit == DistanceUnit.MILES) { viewModel.setDistanceUnit(DistanceUnit.MILES) }
                        }
                    }
                }
            }

            // Display & Workout Environment
            item {
                SettingsSection(title = "Workout Experience") {
                    // Dark Theme
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("High-Contrast Dark Theme", color = DarkTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Gym-optimized high-contrast aesthetic", color = DarkTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { viewModel.setDarkTheme(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = AccentOrange)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Keep Screen On
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Keep Screen On", color = DarkTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Prevent device sleep during an active workout session", color = DarkTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = keepScreenOn,
                            onCheckedChange = { viewModel.setKeepScreenOn(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = AccentOrange)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Default Rest Seconds
                    Column {
                        Text("Default Rest Timer Duration", color = DarkTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(45, 60, 90, 120, 180).forEach { sec ->
                                val isSel = defaultRestSeconds == sec
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) AccentOrange else Color(0xFF222731))
                                        .border(1.dp, if (isSel) AccentOrange else DarkBorder, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.setDefaultRestSeconds(sec) }
                                        .padding(vertical = 8.dp),
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Vibration Alert
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Timer Vibration Feedback", color = DarkTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Haptic pulse on rest timer completion", color = DarkTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = restVibration,
                            onCheckedChange = { viewModel.setRestVibration(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = AccentOrange)
                        )
                    }
                }
            }

            // CSV Export Section
            item {
                SettingsSection(title = "CSV Data Export") {
                    Text(
                        text = "Export standard comma-separated files compatible with Excel, Google Sheets, or Apple Numbers.",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExportButton("Workouts CSV", modifier = Modifier.weight(1f)) {
                            scope.launch {
                                val csv = viewModel.generateWorkoutsCsv()
                                showExportResultDialog = Pair("Workouts CSV Export", csv)
                            }
                        }
                        ExportButton("Exercises CSV", modifier = Modifier.weight(1f)) {
                            scope.launch {
                                val csv = viewModel.generateExercisesCsv()
                                showExportResultDialog = Pair("Exercises CSV Export", csv)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ExportButton("Body Measurements CSV", modifier = Modifier.fillMaxWidth()) {
                        scope.launch {
                            val csv = viewModel.generateMeasurementsCsv()
                            showExportResultDialog = Pair("Measurements CSV Export", csv)
                        }
                    }
                }
            }

            // JSON Backup & Restore
            item {
                SettingsSection(title = "Database Backup & Restore") {
                    Text(
                        text = "Complete database snapshot in validated JSON schema. 100% offline and portable.",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val backupJson = viewModel.createJsonBackup()
                                    showExportResultDialog = Pair("FitLog Pro JSON Backup", backupJson)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Backup", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showRestoreDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222731)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                        ) {
                            Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore Backup", color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // About & Architecture
            item {
                SettingsSection(title = "About FitLog Pro") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("FitLog Pro • v1.0.0", color = DarkTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("100% Offline • Zero Analytics • Zero Cloud Sync • Room & SQLite", color = DarkTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Export Result Dialog with Copy to Clipboard
    if (showExportResultDialog != null) {
        val (title, content) = showExportResultDialog!!
        AlertDialog(
            onDismissRequest = { showExportResultDialog = null },
            title = { Text(title, color = DarkTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Generated payload ready for export:", color = DarkTextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF161920))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = content.take(1000) + if (content.length > 1000) "\n... (${content.length} characters)" else "",
                            color = DarkTextPrimary,
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(content))
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportResultDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Copy to Clipboard", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportResultDialog = null }) {
                    Text("Close", color = DarkTextPrimary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Restore JSON Backup Dialog
    if (showRestoreDialog) {
        var restorePayload by remember { mutableStateOf("") }
        var validationStatus by remember { mutableStateOf<String?>(null) }
        var isValid by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore JSON Backup", color = DarkTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Paste your FitLog Pro backup JSON payload below to validate and restore.",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = restorePayload,
                        onValueChange = {
                            restorePayload = it
                            if (it.isNotBlank()) {
                                when (val result = viewModel.validateBackup(it)) {
                                    is BackupValidationResult.Valid -> {
                                        isValid = true
                                        validationStatus = "Valid Backup: ${result.workoutCount} workouts, ${result.exerciseCount} exercises"
                                    }
                                    is BackupValidationResult.Invalid -> {
                                        isValid = false
                                        validationStatus = "Error: ${result.reason}"
                                    }
                                }
                            } else {
                                isValid = false
                                validationStatus = null
                            }
                        },
                        placeholder = { Text("Paste JSON here...") },
                        minLines = 4,
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentOrange,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (validationStatus != null) {
                        Text(
                            text = validationStatus!!,
                            color = if (isValid) AccentTeal else Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = viewModel.restoreBackup(restorePayload, overwrite = false)
                            if (success) {
                                Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
                                showRestoreDialog = false
                            } else {
                                Toast.makeText(context, "Failed to restore backup.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Confirm Restore", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel", color = DarkTextPrimary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                color = AccentOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun UnitToggleChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) AccentOrange else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else DarkTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ExportButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF222731))
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = DarkTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
