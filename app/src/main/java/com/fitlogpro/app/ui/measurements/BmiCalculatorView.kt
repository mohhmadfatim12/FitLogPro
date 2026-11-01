package com.fitlogpro.app.ui.measurements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.domain.calculator.BmiCalculator
import com.fitlogpro.app.domain.calculator.BmiCategory
import com.fitlogpro.app.domain.calculator.UnitConverter
import com.fitlogpro.app.domain.model.WeightUnit
import com.fitlogpro.app.ui.theme.ElectricLime
import com.fitlogpro.app.ui.theme.ElectricLimeText
import kotlin.math.roundToInt

@Composable
fun BmiCalculatorView(
    userHeightCm: Float,
    latestWeightKg: Double?,
    initialUnitIsMetric: Boolean,
    onHeightChanged: (Float) -> Unit,
    onSaveWeight: (Double, String) -> Unit
) {
    var isMetric by remember { mutableStateOf(initialUnitIsMetric) }

    // Height state (internal storage in cm for metric, or ft/in for imperial)
    var heightCmState by remember(userHeightCm) {
        val initial = if (userHeightCm > 0f) userHeightCm else 175f
        mutableStateOf(String.format("%.0f", initial))
    }
    
    // Imperial height state
    var heightFeetState by remember(userHeightCm) {
        val totalInches = UnitConverter.cmToInches(userHeightCm.toDouble())
        val feet = (totalInches / 12).toInt().coerceAtLeast(3)
        mutableStateOf(feet.toString())
    }
    var heightInchesState by remember(userHeightCm) {
        val totalInches = UnitConverter.cmToInches(userHeightCm.toDouble())
        val inches = (totalInches % 12).roundToInt().coerceIn(0, 11)
        mutableStateOf(inches.toString())
    }

    // Weight state
    val defaultWeight = latestWeightKg ?: 70.0
    var weightInputState by remember(latestWeightKg, isMetric) {
        val displayVal = if (isMetric) defaultWeight else UnitConverter.kgToLbs(defaultWeight)
        mutableStateOf(String.format("%.1f", displayVal))
    }

    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Parse active height in cm and total inches
    val currentHeightCm = if (isMetric) {
        heightCmState.toDoubleOrNull() ?: 175.0
    } else {
        val ft = heightFeetState.toDoubleOrNull() ?: 5.0
        val inc = heightInchesState.toDoubleOrNull() ?: 9.0
        UnitConverter.inchesToCm(ft * 12.0 + inc)
    }

    val currentHeightInches = if (isMetric) {
        UnitConverter.cmToInches(currentHeightCm)
    } else {
        val ft = heightFeetState.toDoubleOrNull() ?: 5.0
        val inc = heightInchesState.toDoubleOrNull() ?: 9.0
        ft * 12.0 + inc
    }

    // Parse active weight
    val inputWeight = weightInputState.toDoubleOrNull() ?: 0.0
    val weightKg = if (isMetric) inputWeight else UnitConverter.lbsToKg(inputWeight)
    val weightLbs = if (isMetric) UnitConverter.kgToLbs(inputWeight) else inputWeight

    // Evaluate BMI
    val bmiResult = if (isMetric) {
        BmiCalculator.evaluateMetric(weightKg, currentHeightCm)
    } else {
        BmiCalculator.evaluateImperial(weightLbs, currentHeightInches)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Metric / Imperial Unit Toggle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                UnitToggleOption(
                    title = "Metric (kg, cm)",
                    isSelected = isMetric,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (!isMetric) {
                            // Convert inputs to metric
                            val currentWeightLbs = weightInputState.toDoubleOrNull() ?: 154.0
                            val convKg = UnitConverter.lbsToKg(currentWeightLbs)
                            weightInputState = String.format("%.1f", convKg)
                            heightCmState = String.format("%.0f", currentHeightCm)
                            isMetric = true
                        }
                    }
                )
                UnitToggleOption(
                    title = "Imperial (lbs, ft/in)",
                    isSelected = !isMetric,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (isMetric) {
                            // Convert inputs to imperial
                            val currentWeightKg = weightInputState.toDoubleOrNull() ?: 70.0
                            val convLbs = UnitConverter.kgToLbs(currentWeightKg)
                            weightInputState = String.format("%.1f", convLbs)
                            val totalInches = UnitConverter.cmToInches(currentHeightCm)
                            heightFeetState = (totalInches / 12).toInt().coerceAtLeast(3).toString()
                            heightInchesState = (totalInches % 12).roundToInt().coerceIn(0, 11).toString()
                            isMetric = false
                        }
                    }
                )
            }
        }

        // 2. BMI Result Hero Gauge Card
        item {
            BmiHeroCard(bmiResult = bmiResult)
        }

        // 3. Height Input Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Height,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Height",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isMetric) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = heightCmState,
                                onValueChange = { newVal ->
                                    val filtered = newVal.filter { it.isDigit() || it == '.' }
                                    heightCmState = filtered
                                    filtered.toFloatOrNull()?.let { onHeightChanged(it) }
                                },
                                label = { Text("Height in cm") },
                                trailingIcon = { Text("cm", modifier = Modifier.padding(end = 12.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            // Quick adjust stepper
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                StepperButton(label = "-5") {
                                    val current = heightCmState.toDoubleOrNull() ?: 175.0
                                    val updated = (current - 5.0).coerceAtLeast(80.0)
                                    heightCmState = String.format("%.0f", updated)
                                    onHeightChanged(updated.toFloat())
                                }
                                StepperButton(label = "+5") {
                                    val current = heightCmState.toDoubleOrNull() ?: 175.0
                                    val updated = (current + 5.0).coerceAtMost(250.0)
                                    heightCmState = String.format("%.0f", updated)
                                    onHeightChanged(updated.toFloat())
                                }
                            }
                        }
                    } else {
                        // Imperial: Feet and Inches
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = heightFeetState,
                                onValueChange = { newVal ->
                                    val filtered = newVal.filter { it.isDigit() }
                                    heightFeetState = filtered
                                    val ft = filtered.toDoubleOrNull() ?: 5.0
                                    val inc = heightInchesState.toDoubleOrNull() ?: 9.0
                                    onHeightChanged(UnitConverter.inchesToCm(ft * 12.0 + inc).toFloat())
                                },
                                label = { Text("Feet") },
                                trailingIcon = { Text("ft", modifier = Modifier.padding(end = 10.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = heightInchesState,
                                onValueChange = { newVal ->
                                    val filtered = newVal.filter { it.isDigit() }
                                    heightInchesState = filtered
                                    val ft = heightFeetState.toDoubleOrNull() ?: 5.0
                                    val inc = filtered.toDoubleOrNull() ?: 0.0
                                    onHeightChanged(UnitConverter.inchesToCm(ft * 12.0 + inc).toFloat())
                                },
                                label = { Text("Inches") },
                                trailingIcon = { Text("in", modifier = Modifier.padding(end = 10.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 4. Weight Input Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonitorWeight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weight",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Quick chip to use latest logged weight if present
                        if (latestWeightKg != null && latestWeightKg > 0.0) {
                            val latestDisplay = if (isMetric) latestWeightKg else UnitConverter.kgToLbs(latestWeightKg)
                            val unitStr = if (isMetric) "kg" else "lbs"
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        weightInputState = String.format("%.1f", latestDisplay)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Latest: ${String.format("%.1f", latestDisplay)} $unitStr",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weightInputState,
                            onValueChange = { newVal ->
                                weightInputState = newVal.filter { it.isDigit() || it == '.' }
                            },
                            label = { Text(if (isMetric) "Weight in kg" else "Weight in lbs") },
                            trailingIcon = {
                                Text(
                                    text = if (isMetric) "kg" else "lbs",
                                    modifier = Modifier.padding(end = 12.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Stepper buttons for weight +/- 1
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            StepperButton(label = "-1") {
                                val current = weightInputState.toDoubleOrNull() ?: 70.0
                                val updated = (current - 1.0).coerceAtLeast(20.0)
                                weightInputState = String.format("%.1f", updated)
                            }
                            StepperButton(label = "+1") {
                                val current = weightInputState.toDoubleOrNull() ?: 70.0
                                val updated = (current + 1.0).coerceAtMost(300.0)
                                weightInputState = String.format("%.1f", updated)
                            }
                        }
                    }
                }
            }
        }

        // 5. Healthy Weight Range & Classification Card
        item {
            HealthyWeightInsightCard(bmiResult = bmiResult)
        }

        // 6. Action Button: Save to Body Measurements Log
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (weightKg > 0.0) {
                            onSaveWeight(weightKg, "Logged via BMI Calculator (BMI ${bmiResult.bmi})")
                            saveSuccessMessage = "Saved ${String.format("%.1f", if (isMetric) weightKg else weightLbs)} ${if (isMetric) "kg" else "lbs"} to Body Logs!"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricLime,
                        contentColor = ElectricLimeText
                    )
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Weight to History Log",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Temporary confirmation feedback
                AnimatedVisibility(
                    visible = saveSuccessMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    saveSuccessMessage?.let { msg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun UnitToggleOption(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ElectricLime else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) ElectricLimeText else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StepperButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 42.dp, height = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BmiHeroCard(bmiResult: com.fitlogpro.app.domain.calculator.BmiResult) {
    val category = bmiResult.category
    val categoryColor = Color(category.colorHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "YOUR BODY MASS INDEX",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Large BMI number
            Text(
                text = if (bmiResult.bmi > 0.0) String.format("%.1f", bmiResult.bmi) else "—",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Category Badge Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(categoryColor.copy(alpha = 0.16f))
                    .border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${category.label} (${category.rangeText})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Multi-segment Visual Meter with Indicator Needle
            BmiGaugeBar(bmi = bmiResult.bmi)
        }
    }
}

@Composable
private fun BmiGaugeBar(bmi: Double) {
    val clampedBmi = bmi.coerceIn(15.0, 35.0)
    // Map BMI range [15.0, 35.0] to ratio [0f, 1f]
    val ratio = ((clampedBmi - 15.0) / (35.0 - 15.0)).toFloat()

    val underweightColor = Color(0xFF38BDF8) // Blue
    val normalColor = Color(0xFF22C55E)      // Green
    val overweightColor = Color(0xFFF59E0B)  // Amber
    val obeseColor = Color(0xFFEF4444)       // Red

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            val width = size.width
            val barHeight = 10.dp.toPx()
            val corner = 5.dp.toPx()
            val yOffset = 10.dp.toPx()

            // Segments relative width on 15..35 scale (span = 20):
            // Underweight: 15..18.5 = 3.5 / 20 = 17.5%
            // Normal: 18.5..25.0 = 6.5 / 20 = 32.5%
            // Overweight: 25.0..30.0 = 5.0 / 20 = 25.0%
            // Obese: 30.0..35.0 = 5.0 / 20 = 25.0%
            val w1 = width * 0.175f
            val w2 = width * 0.325f
            val w3 = width * 0.25f
            val w4 = width * 0.25f

            // Underweight section
            drawRoundRect(
                color = underweightColor,
                topLeft = Offset(0f, yOffset),
                size = Size(w1, barHeight),
                cornerRadius = CornerRadius(corner, corner)
            )
            // Normal section
            drawRoundRect(
                color = normalColor,
                topLeft = Offset(w1 + 2f, yOffset),
                size = Size(w2 - 2f, barHeight),
                cornerRadius = CornerRadius(corner, corner)
            )
            // Overweight section
            drawRoundRect(
                color = overweightColor,
                topLeft = Offset(w1 + w2 + 2f, yOffset),
                size = Size(w3 - 2f, barHeight),
                cornerRadius = CornerRadius(corner, corner)
            )
            // Obese section
            drawRoundRect(
                color = obeseColor,
                topLeft = Offset(w1 + w2 + w3 + 2f, yOffset),
                size = Size(w4 - 2f, barHeight),
                cornerRadius = CornerRadius(corner, corner)
            )

            // Current BMI Needle / Triangle
            if (bmi > 0.0) {
                val needleX = (width * ratio).coerceIn(4f, width - 4f)
                val trianglePath = Path().apply {
                    moveTo(needleX, yOffset + 2.dp.toPx())
                    lineTo(needleX - 5.dp.toPx(), 0f)
                    lineTo(needleX + 5.dp.toPx(), 0f)
                    close()
                }
                drawPath(path = trianglePath, color = Color(0xFF0F172A))
            }
        }

        // Zone threshold labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "15.0", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "18.5", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "25.0", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "30.0", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "35+", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HealthyWeightInsightCard(bmiResult: com.fitlogpro.app.domain.calculator.BmiResult) {
    val unitStr = if (bmiResult.isMetric) "kg" else "lbs"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Healthy Weight Recommendation",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Range display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target Range (18.5 – 24.9):",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format("%.1f", bmiResult.minHealthyWeight)} – ${String.format("%.1f", bmiResult.maxHealthyWeight)} $unitStr",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Delta status
            val delta = bmiResult.weightDeltaToHealthy
            val deltaText = when {
                bmiResult.bmi <= 0.0 -> "Enter your height and weight to evaluate."
                delta == 0.0 -> "🎉 You are currently in the healthy weight zone!"
                delta < 0.0 -> "You are ${String.format("%.1f", kotlin.math.abs(delta))} $unitStr below the healthy range."
                else -> "You are ${String.format("%.1f", delta)} $unitStr above the healthy range."
            }
            Text(
                text = deltaText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (delta == 0.0 && bmiResult.bmi > 0.0) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Advice from category
            Text(
                text = bmiResult.category.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
