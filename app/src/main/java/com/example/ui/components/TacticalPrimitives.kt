package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AlertSeverity
import com.example.model.RiskLevel
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalAmberBorder
import com.example.ui.theme.TacticalAmberDark
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalCyan
import com.example.ui.theme.TacticalCyanBorder
import com.example.ui.theme.TacticalCyanDark
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TacticalGreenBorder
import com.example.ui.theme.TacticalGreenDark
import com.example.ui.theme.TacticalRed
import com.example.ui.theme.TacticalRedBorder
import com.example.ui.theme.TacticalRedDark
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalTextPrimary

fun Modifier.tacticalGridBackground(): Modifier = this.drawBehind {
    val stepPx = 36.dp.toPx()
    val gridColor = Color(0xFF19242C)
    var x = 0f
    while (x <= size.width) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += stepPx
    }
    var y = 0f
    while (y <= size.height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += stepPx
    }
}

@Composable
fun TacticalPanelCard(
    title: String,
    modifier: Modifier = Modifier,
    accentColor: Color = TacticalGreen,
    trailingHeader: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = TacticalSurface,
        border = BorderStroke(1.dp, TacticalBorder),
        tonalElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TacticalSurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(14.dp)
                        .background(accentColor, RoundedCornerShape(1.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TacticalTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (trailingHeader != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingHeader()
                }
            }
            HorizontalDivider(color = TacticalBorder, thickness = 1.dp)
            Column(
                modifier = Modifier.padding(12.dp),
                content = content
            )
        }
    }
}

@Composable
fun RiskLevelBadge(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val (bg, border, text) = when (riskLevel) {
        RiskLevel.SECURE -> Triple(TacticalGreenDark, TacticalGreenBorder, TacticalGreen)
        RiskLevel.WATCH -> Triple(TacticalAmberDark, TacticalAmberBorder, TacticalAmber)
        RiskLevel.CRITICAL -> Triple(TacticalRedDark, TacticalRedBorder, TacticalRed)
        RiskLevel.ISOLATED -> Triple(TacticalCyanDark, TacticalCyanBorder, TacticalCyan)
    }

    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(3.dp))
            .border(1.dp, border, RoundedCornerShape(3.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = riskLevel.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = text,
            maxLines = 1
        )
    }
}

fun severityColor(severity: AlertSeverity): Color = when (severity) {
    AlertSeverity.CRITICAL -> TacticalRed
    AlertSeverity.WATCH -> TacticalAmber
    AlertSeverity.OPERATIONAL -> TacticalGreen
}
