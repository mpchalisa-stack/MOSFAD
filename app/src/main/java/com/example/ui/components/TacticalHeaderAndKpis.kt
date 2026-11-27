package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.MosfadUiState
import com.example.model.TacticalLinkMode
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalAmberBorder
import com.example.ui.theme.TacticalAmberDark
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalCyan
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TacticalGreenBorder
import com.example.ui.theme.TacticalGreenDark
import com.example.ui.theme.TacticalRed
import com.example.ui.theme.TacticalRedBorder
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalTextMuted
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary

@Composable
fun MosfadTopBar(
    linkMode: TacticalLinkMode,
    isCompact: Boolean,
    onToggleLinkMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TacticalSurfaceElevated,
        border = BorderStroke(1.dp, TacticalBorder)
    ) {
        if (isCompact) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DiskomlekauEmblem()
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MOSFAD DISKOMLEKAU",
                            style = MaterialTheme.typography.titleLarge,
                            color = TacticalTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Mobile Security & Forensic Analyzer",
                            style = MaterialTheme.typography.bodySmall,
                            color = TacticalTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                TacticalLinkStatusPill(
                    linkMode = linkMode,
                    onClick = onToggleLinkMode,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DiskomlekauEmblem()
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "MOSFAD: Mobile Security and Forensic Analyzer Diskomlekau",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TacticalTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                TacticalLinkStatusPill(
                    linkMode = linkMode,
                    onClick = onToggleLinkMode
                )
            }
        }
    }
}

@Composable
private fun DiskomlekauEmblem() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, TacticalGreenBorder, RoundedCornerShape(6.dp))
            .background(Color(0xFF0D1418)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.mosfad_diskomlekau_logo),
            contentDescription = "Logo Diskomlekau MOSFAD",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(38.dp)
        )
    }
}

@Composable
fun TacticalLinkStatusPill(
    linkMode: TacticalLinkMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = linkMode == TacticalLinkMode.ACTIVE
    val bgColor by animateColorAsState(
        targetValue = if (isActive) TacticalGreenDark else TacticalAmberDark,
        label = "linkBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) TacticalGreenBorder else TacticalAmberBorder,
        label = "linkBorder"
    )
    val accentColor = if (isActive) TacticalGreen else TacticalAmber

    val pulse = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .testTag("tactical_link_toggle"),
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(if (isActive) dotAlpha else 1f)
                    .background(accentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = linkMode.label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun KpiCardsRow(
    state: MosfadUiState,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val statusAccent = when {
        state.criticalThreatsCount > 1 -> TacticalRed
        state.tacticalLinkMode == TacticalLinkMode.OFFLINE_CACHE -> TacticalAmber
        else -> TacticalGreen
    }

    if (isCompact) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiMetricCard(
                    tagText = "RUGGED",
                    title = "TOTAL DEVICES",
                    value = state.totalDevicesCount.toString(),
                    subtitle = "DEPLOYED • RUGGED",
                    icon = Icons.Filled.PhoneAndroid,
                    accentColor = TacticalCyan,
                    borderColor = TacticalBorder,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kpi_total_devices")
                )
                KpiMetricCard(
                    tagText = "RUGGED",
                    title = "SYSTEM STATUS",
                    value = state.systemStatusLabel,
                    subtitle = state.systemStatusSubLabel,
                    icon = Icons.Filled.Security,
                    accentColor = statusAccent,
                    borderColor = if (statusAccent == TacticalGreen) TacticalGreenBorder else TacticalAmberBorder,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kpi_system_status")
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiMetricCard(
                    tagText = "RUGGED",
                    title = "ANOMALY ALERTS",
                    value = state.watchAlertsCount.toString(),
                    subtitle = "WARNING",
                    icon = Icons.Filled.Warning,
                    accentColor = TacticalAmber,
                    borderColor = TacticalAmberBorder,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kpi_anomaly_alerts")
                )
                KpiMetricCard(
                    tagText = "RUGGED",
                    title = "CRITICAL THREATS",
                    value = state.criticalThreatsCount.toString(),
                    subtitle = if (state.criticalThreatsCount > 0) "IMMEDIATE ACTION" else "CONTAINED",
                    icon = Icons.Outlined.ReportProblem,
                    accentColor = if (state.criticalThreatsCount > 0) TacticalRed else TacticalGreen,
                    borderColor = if (state.criticalThreatsCount > 0) TacticalRedBorder else TacticalGreenBorder,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kpi_critical_threats")
                )
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricCard(
                tagText = "RUGGED",
                title = "TOTAL DEVICES",
                value = state.totalDevicesCount.toString(),
                subtitle = "DEPLOYED • RUGGED",
                icon = Icons.Filled.PhoneAndroid,
                accentColor = TacticalCyan,
                borderColor = TacticalBorder,
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_total_devices")
            )
            KpiMetricCard(
                tagText = "RUGGED",
                title = "SYSTEM STATUS",
                value = state.systemStatusLabel,
                subtitle = state.systemStatusSubLabel,
                icon = Icons.Filled.Security,
                accentColor = statusAccent,
                borderColor = if (statusAccent == TacticalGreen) TacticalGreenBorder else TacticalAmberBorder,
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_system_status")
            )
            KpiMetricCard(
                tagText = "RUGGED",
                title = "ANOMALY ALERTS",
                value = state.watchAlertsCount.toString(),
                subtitle = "WARNING",
                icon = Icons.Filled.Warning,
                accentColor = TacticalAmber,
                borderColor = TacticalAmberBorder,
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_anomaly_alerts")
            )
            KpiMetricCard(
                tagText = "RUGGED",
                title = "CRITICAL THREATS",
                value = state.criticalThreatsCount.toString(),
                subtitle = if (state.criticalThreatsCount > 0) "IMMEDIATE ACTION REQUIRED" else "ALL THREATS ISOLATED",
                icon = Icons.Outlined.ReportProblem,
                accentColor = if (state.criticalThreatsCount > 0) TacticalRed else TacticalGreen,
                borderColor = if (state.criticalThreatsCount > 0) TacticalRedBorder else TacticalGreenBorder,
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_critical_threats")
            )
        }
    }
}

@Composable
private fun KpiMetricCard(
    tagText: String,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = TacticalSurface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(TacticalSurfaceElevated, RoundedCornerShape(3.dp))
                        .border(1.dp, TacticalBorder, RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tagText,
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalTextMuted
                    )
                }
                Text(
                    text = "//",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accentColor.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (accentColor == TacticalCyan) TacticalTextSecondary else accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
