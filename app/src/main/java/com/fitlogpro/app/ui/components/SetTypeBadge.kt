package com.fitlogpro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.fitlogpro.app.ui.theme.SetColorDropSet
import com.fitlogpro.app.ui.theme.SetColorFailure
import com.fitlogpro.app.ui.theme.SetColorWarmup

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
            Color(0xFF222731),
            Color(0xFFF0F2F5),
            Color(0xFF333B4A)
        )
        SetType.WARMUP -> Quad(
            "W",
            SetColorWarmup.copy(alpha = 0.2f),
            SetColorWarmup,
            SetColorWarmup
        )
        SetType.DROP_SET -> Quad(
            "D",
            SetColorDropSet.copy(alpha = 0.2f),
            SetColorDropSet,
            SetColorDropSet
        )
        SetType.FAILURE -> Quad(
            "F",
            SetColorFailure.copy(alpha = 0.2f),
            SetColorFailure,
            SetColorFailure
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
