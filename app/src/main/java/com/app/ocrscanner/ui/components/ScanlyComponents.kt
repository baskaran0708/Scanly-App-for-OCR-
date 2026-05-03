package com.app.ocrscanner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.ocrscanner.ui.theme.Accent
import com.app.ocrscanner.ui.theme.OnAccent
import com.app.ocrscanner.ui.theme.Primary
import com.app.ocrscanner.ui.theme.OnPrimary

@Composable
fun ScanlyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(999.dp),
        color = Primary,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = OnPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text = text, color = OnPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
fun ScanlyAccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(999.dp),
        color = Accent,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = OnAccent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text = text, color = OnAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun ScanlyOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(999.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(text = text, color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
fun ScanlyFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    size: Int = 64,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(
                brush = Brush.linearGradient(listOf(Primary, Accent)),
                shape = CircleShape,
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size((size * 0.44f).dp),
        )
    }
}

@Composable
fun ScanlyChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) Primary else MaterialTheme.colorScheme.surface,
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = if (selected) OnPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
        )
    }
}

@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp,
    )
}

@Composable
fun ConfidenceBadge(confidence: Float, modifier: Modifier = Modifier) {
    val (bg, fg) = when {
        confidence >= 0.9f -> Pair(
            Color(0xFFD1FAE5),
            Color(0xFF10B981),
        )
        confidence >= 0.7f -> Pair(
            Color(0xFFFEF3C7),
            Color(0xFFF59E0B),
        )
        else -> Pair(
            Color(0xFFFEE2E2),
            Color(0xFFDC2626),
        )
    }
    StatusBadge(
        text = "${(confidence * 100).toInt()}% confidence",
        backgroundColor = bg,
        textColor = fg,
        modifier = modifier,
    )
}
