package com.fitlogpro.app.ui.theme

import androidx.compose.ui.graphics.Color

// "Fitness in Full Motion" / Dribbble Modern Fitness Palette
// 1. Canvas & Surfaces
val LightBackground = Color(0xFFF7F9F2)        // Soft warm ivory-cream canvas
val LightSurface = Color(0xFFFFFFFF)           // Pure crisp white card surface
val LightSurfaceVariant = Color(0xFFF0F4E8)     // Soft pistachio/lime-tinted surface
val LightBorder = Color(0xFFE2E8D5)            // Subtle hairline card border
val LightTextPrimary = Color(0xFF0F172A)        // Deep slate high-contrast typography
val LightTextSecondary = Color(0xFF475569)      // Slate 600 secondary text
val LightTextTertiary = Color(0xFF94A3B8)       // Slate 400 captions & hints

// 2. Signature Electric Lime Accents
val ElectricLime = Color(0xFFD4F637)           // Vibrant Dribbble Fitness Lime
val ElectricLimeBright = Color(0xFFE2FF4A)     // Luminous Neon Lime
val ElectricLimeDark = Color(0xFFAFD618)       // Pressed state lime
val ElectricLimeContainer = Color(0xFFF1FADC)  // Soft lime pill background
val ElectricLimeText = Color(0xFF0F172A)       // Crisp black text on lime buttons

// 3. Status, Set Types & Accents
val AccentGreen = Color(0xFF10B981)            // Emerald (Success & Completed checkmarks)
val AccentGreenContainer = Color(0xFFECFDF5)   // Mint pill
val AccentGold = Color(0xFFF59E0B)             // Warmup gold
val AccentGoldContainer = Color(0xFFFEF3C7)    // Soft gold pill
val AccentPurple = Color(0xFF8B5CF6)           // Dropset violet
val AccentPurpleContainer = Color(0xFFEDE9FE)  // Soft violet pill
val AccentRed = Color(0xFFEF4444)              // Failure / Danger red
val AccentRedContainer = Color(0xFFFEE2E2)     // Soft red pill

// 4. Dark Theme Palette (Obsidian & Neon Lime)
val DarkBackground = Color(0xFF0D1117)         // Deep obsidian canvas
val DarkSurface = Color(0xFF161B22)            // Elevated dark card
val DarkSurfaceVariant = Color(0xFF21262D)     // Dark chip/pill
val DarkBorder = Color(0xFF30363D)             // Subtle dark hairline border
val DarkTextPrimary = Color(0xFFF8FAFC)        // Crisp white text
val DarkTextSecondary = Color(0xFF94A3B8)      // Slate 400
val DarkTextTertiary = Color(0xFF64748B)       // Slate 500

// 5. Component Compatibility Aliases
val PrimaryBlue = ElectricLime
val PrimaryBlueLight = ElectricLimeBright
val PrimaryBlueContainer = ElectricLimeContainer
val AccentOrange = ElectricLime
val AccentOrangeLight = ElectricLimeBright
val AccentTeal = ElectricLime

// Set Type Colors
val SetColorNormal = ElectricLimeDark
val SetColorWarmup = AccentGold
val SetColorDropSet = AccentPurple
val SetColorFailure = AccentRed
