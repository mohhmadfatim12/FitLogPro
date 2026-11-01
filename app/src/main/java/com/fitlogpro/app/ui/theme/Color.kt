package com.fitlogpro.app.ui.theme

import androidx.compose.ui.graphics.Color

// Modern Light Palette (Default - Clean Porcelain & Royal Blue)
val LightBackground = Color(0xFFF8FAFC)        // Slate 50
val LightSurface = Color(0xFFFFFFFF)           // Pure White
val LightSurfaceVariant = Color(0xFFF1F5F9)     // Slate 100
val LightBorder = Color(0xFFE2E8F0)            // Slate 200
val LightTextPrimary = Color(0xFF0F172A)        // Slate 900
val LightTextSecondary = Color(0xFF475569)      // Slate 600
val LightTextTertiary = Color(0xFF94A3B8)       // Slate 400

// Modern Dark Palette (Obsidian & Electric Blue)
val DarkBackground = Color(0xFF0B0E14)         // Deep Obsidian Pitch Black
val DarkSurface = Color(0xFF131722)            // Elevated Dark Card
val DarkSurfaceVariant = Color(0xFF1C2230)     // Dark Pill/Chip
val DarkBorder = Color(0xFF262E40)             // Subtle Hairline Dark Border
val DarkTextPrimary = Color(0xFFF8FAFC)        // Crisp White
val DarkTextSecondary = Color(0xFF94A3B8)      // Slate 400
val DarkTextTertiary = Color(0xFF64748B)       // Slate 500

// Core Accents (Modern Hevy / Strong Inspired)
val PrimaryBlue = Color(0xFF2563EB)            // Royal Cobalt Blue
val PrimaryBlueLight = Color(0xFF3B82F6)       // Electric Blue
val PrimaryBlueContainer = Color(0xFFEFF6FF)   // Soft Blue Pill (Light)

val AccentGreen = Color(0xFF10B981)            // Vibrant Emerald (Success/Completed)
val AccentGreenContainer = Color(0xFFECFDF5)   // Soft Mint Pill (Light)
val AccentGold = Color(0xFFF59E0B)             // Trophy Gold / Warmup Amber
val AccentGoldContainer = Color(0xFFFEF3C7)    // Soft Gold Pill (Light)
val AccentPurple = Color(0xFF8B5CF6)           // Drop-set Violet
val AccentPurpleContainer = Color(0xFFEDE9FE)  // Soft Violet Pill (Light)
val AccentRed = Color(0xFFEF4444)              // Failure / Delete Rose
val AccentRedContainer = Color(0xFFFEE2E2)     // Soft Red Pill (Light)

// Legacy Aliases for instant UI upgrade across all components
val AccentOrange = PrimaryBlue
val AccentOrangeLight = PrimaryBlueLight
val AccentTeal = PrimaryBlue

// Set Type Colors (Clean, distinct, vibrant)
val SetColorNormal = PrimaryBlue
val SetColorWarmup = AccentGold
val SetColorDropSet = AccentPurple
val SetColorFailure = AccentRed
