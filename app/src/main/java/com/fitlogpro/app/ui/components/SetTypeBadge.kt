package com.fitlogpro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.domain.model.SetType

@Composable
fun SetTypeBadge(
    setNumber: Int,
    setType: SetType,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (badgeText, bgColor, textColor, borderColor) = when (setType) {
        SetType.NORMAL -> Quad(
            setNumber.toString(),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.outline
        )
        SetType.WARMUP -> Quad(
            "W",
            Color(0xFFFEF3C7),
            Color(0xFFD97706),
            Color(0xFFFDE68A)
        )
        SetType.DROP_SET -> Quad(
            "D",
            Color(0xFFEDE9FE),
            Color(0xFF7C3AED),
            Color(0xFFDDD6FE)
        )
        SetType.FAILURE -> Quad(
            "F",
            Color(0xFFFEE2E2),
            Color(0xFFDC2626),
            Color(0xFFFECACA)
        )
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badgeText,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
