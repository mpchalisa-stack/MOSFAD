package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AlertFeedItem
import com.example.model.AlertSeverity
import com.example.ui.theme.TacticalAmberDark
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalRed
import com.example.ui.theme.TacticalRedDark
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalTextMuted
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary

@Composable
fun CriticalAlertFeedColumn(
    alerts: List<AlertFeedItem>,
    onAlertClick: (callsign: String) -> Unit,
    modifier: Modifier = Modifier
) {
    TacticalPanelCard(
        title = "CRITICAL ALERT FEED",
        accentColor = TacticalRed,
        trailingHeader = {
            Text(
                text = "${alerts.size} LOG",
                style = MaterialTheme.typography.labelSmall,
                color = TacticalTextMuted
            )
        },
        modifier = modifier
    ) {
        Text(
            text = "Log peringatan diisi oleh hasil analisis backend /api/analyze dan aksi IRT.",
            style = MaterialTheme.typography.bodySmall,
            color = TacticalTextMuted
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (alerts.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = TacticalSurfaceElevated,
                border = BorderStroke(1.dp, TacticalBorder)
            ) {
                Text(
                    text = "Belum ada alert tercatat. Jalankan analisis /api/analyze pada perangkat untuk melihat log temuan real-time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TacticalTextSecondary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                alerts.forEach { alert ->
                    AlertLogEntryCard(
                        alert = alert,
                        onClick = { onAlertClick(alert.callsign) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertLogEntryCard(
    alert: AlertFeedItem,
    onClick: () -> Unit
) {
    val accent = severityColor(alert.severity)
    val bgColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> TacticalRedDark.copy(alpha = 0.45f)
        AlertSeverity.WATCH -> TacticalAmberDark.copy(alpha = 0.35f)
        AlertSeverity.OPERATIONAL -> TacticalSurfaceElevated
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .testTag("alert_item_${alert.id}"),
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        border = BorderStroke(1.dp, TacticalBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(44.dp)
                    .background(accent, RoundedCornerShape(1.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalTextMuted
                    )
                    if (alert.severity == AlertSeverity.CRITICAL) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Peringatan Kritis",
                            tint = TacticalRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TacticalTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${alert.callsign} ${alert.severity.tag}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = alert.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = TacticalTextSecondary
                )
            }
        }
    }
}
