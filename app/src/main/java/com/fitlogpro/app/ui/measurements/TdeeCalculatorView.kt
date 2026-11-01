package com.fitlogpro.app.ui.measurements

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.domain.calculator.ActivityLevel
import com.fitlogpro.app.domain.calculator.NutritionGoal
import com.fitlogpro.app.domain.calculator.TdeeCalculator
import com.fitlogpro.app.domain.calculator.UnitConverter
import com.fitlogpro.app.ui.theme.ElectricLime
import com.fitlogpro.app.ui.theme.ElectricLimeDark
import com.fitlogpro.app.ui.theme.ElectricLimeText

@Composable
fun TdeeCalculatorView(
    userHeightCm: Float,
    latestWeightKg: Double?,
    initialUnitIsMetric: Boolean
) {
    var isMetric by remember { mutableStateOf(initialUnitIsMetric) }
    var isMale by remember { mutableStateOf(true) }
    var ageState by remember { mutableStateOf("25") }

    val defaultWeight = latestWeightKg ?: 70.0
    var weightInputState by remember(latestWeightKg, isMetric) {
        val displayVal = if (isMetric) defaultWeight else UnitConverter.kgToLbs(defaultWeight)
        mutableStateOf(String.format("%.1f", displayVal))
    }

    var heightInputState by remember(userHeightCm, isMetric) {
        val h = if (userHeightCm > 0f) userHeightCm else 175f
        val displayVal = if (isMetric) h else UnitConverter.cmToInches(h.toDouble()).toFloat()
        mutableStateOf(String.format("%.0f", displayVal))
    }

    var selectedActivity by remember { mutableStateOf(ActivityLevel.MODERATELY_ACTIVE) }
    var selectedGoal by remember { mutableStateOf(NutritionGoal.MAINTENANCE) }

    // Parse input values to metric standard
    val weightKg = if (isMetric) {
        weightInputState.toDoubleOrNull() ?: 70.0
    } else {
        UnitConverter.lbsToKg(weightInputState.toDoubleOrNull() ?: 154.0)
    }

    val heightCm = if (isMetric) {
        heightInputState.toDoubleOrNull() ?: 175.0
    } else {
        UnitConverter.inchesToCm(heightInputState.toDoubleOrNull() ?: 69.0)
    }

    val age = ageState.toIntOrNull() ?: 25

    val macroResult = TdeeCalculator.calculateMacroTargets(
        weightKg = weightKg,
        heightCm = heightCm,
        age = age,
        isMale = isMale,
        activityLevel = selectedActivity,
        goal = selectedGoal
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Biological Profile Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor)
                    .border(1.dp, outlineColor, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Metabolic Profile",
                            color = textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Metric / Imperial pill toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isMetric) ElectricLime else Color.Transparent)
                                    .clickable { isMetric = true }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Metric", color = if (isMetric) ElectricLimeText else textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isMetric) ElectricLime else Color.Transparent)
                                    .clickable { isMetric = false }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Imperial", color = if (!isMetric) ElectricLimeText else textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Sex and Age Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Biological Sex Selector
                        Row(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (isMale) surfaceColor else Color.Transparent)
                                    .clickable { isMale = true }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Male", color = if (isMale) textPrimary else textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (!isMale) surfaceColor else Color.Transparent)
                                    .clickable { isMale = false }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Female", color = if (!isMale) textPrimary else textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Age Field
                        OutlinedTextField(
                            value = ageState,
                            onValueChange = { if (it.length <= 3) ageState = it.filter { c -> c.isDigit() } },
                            label = { Text("Age (yrs)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.8f).height(52.dp)
                        )
                    }

                    // Height & Weight Input Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = weightInputState,
                            onValueChange = { weightInputState = it },
                            label = { Text(if (isMetric) "Weight (kg)" else "Weight (lbs)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = heightInputState,
                            onValueChange = { heightInputState = it },
                            label = { Text(if (isMetric) "Height (cm)" else "Height (in)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Activity Level Selector Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor)
                    .border(1.dp, outlineColor, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Training & Activity Multiplier",
                        color = textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    ActivityLevel.values().forEach { level ->
                        val isSelected = selectedActivity == level
                        val bg = if (isSelected) ElectricLime.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        val borderCol = if (isSelected) ElectricLimeDark else Color.Transparent

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                .clickable { selectedActivity = level }
                                .padding(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(level.displayName, color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(level.description, color = textSecondary, fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ElectricLimeDark else outlineColor)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("×${level.multiplier}", color = if (isSelected) Color.White else textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Goal Selection Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor)
                    .border(1.dp, outlineColor, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Target Nutrition Goal",
                        color = textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NutritionGoal.values().take(3).forEach { goal ->
                            val isSelected = selectedGoal == goal
                            GoalPill(goal = goal, isSelected = isSelected, onClick = { selectedGoal = goal }, modifier = Modifier.weight(1f))
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NutritionGoal.values().drop(3).forEach { goal ->
                            val isSelected = selectedGoal == goal
                            GoalPill(goal = goal, isSelected = isSelected, onClick = { selectedGoal = goal }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 4. Adaptive Targets Output Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(surfaceColor)
                    .border(1.5.dp, ElectricLimeDark.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAILY CALORIE TARGET",
                                color = textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${macroResult.targetCalories}",
                                    color = textPrimary,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "kcal / day",
                                    color = textSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 5.dp)
                                )
                            }
                        }

                        // Calorie delta tag
                        val deltaText = if (selectedGoal.calorieDelta > 0) "+${selectedGoal.calorieDelta} kcal" else if (selectedGoal.calorieDelta < 0) "${selectedGoal.calorieDelta} kcal" else "Maintenance"
                        val deltaColor = if (selectedGoal.calorieDelta > 0) Color(0xFF10B981) else if (selectedGoal.calorieDelta < 0) Color(0xFFF59E0B) else primaryColor

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(deltaColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(deltaText, color = deltaColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // BMR vs TDEE Breakdown
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Basal Rate (BMR)", color = textSecondary, fontSize = 11.sp)
                                Text("${macroResult.bmr} kcal", color = textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Total Burn (TDEE)", color = textSecondary, fontSize = 11.sp)
                                Text("${macroResult.tdee} kcal", color = textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Macronutrient Split
                    Text(
                        text = "Macronutrient Distribution",
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MacroNutrientCard(name = "Protein", grams = macroResult.proteinGrams, color = Color(0xFF2563EB), modifier = Modifier.weight(1f))
                        MacroNutrientCard(name = "Carbs", grams = macroResult.carbGrams, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                        MacroNutrientCard(name = "Fats", grams = macroResult.fatGrams, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                    }

                    // Expected Rate of Change
                    if (selectedGoal != NutritionGoal.MAINTENANCE) {
                        val ratePrefix = if (macroResult.estimatedWeeklyChangeKg > 0) "+" else ""
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = textSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Projected rate: ${ratePrefix}${String.format("%.2f", macroResult.estimatedWeeklyChangeKg)} kg / week",
                                    color = textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun GoalPill(
    goal: NutritionGoal,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) ElectricLime else MaterialTheme.colorScheme.surfaceVariant
    val textCol = if (isSelected) ElectricLimeText else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = goal.displayName,
            color = textCol,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun MacroNutrientCard(
    name: String,
    grams: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text("${grams}g", color = color, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
