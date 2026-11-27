package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ForensicAnalysisUiState
import com.example.model.ForensicReport
import com.example.model.RiskLevel
import com.example.model.TacticalDevice
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalBgDeep
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalBorderBright
import com.example.ui.theme.TacticalCyan
import com.example.ui.theme.TacticalCyanBorder
import com.example.ui.theme.TacticalCyanDark
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TacticalGreenBorder
import com.example.ui.theme.TacticalGreenDark
import com.example.ui.theme.TacticalRed
import com.example.ui.theme.TacticalRedBorder
import com.example.ui.theme.TacticalRedDark
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalSurfaceHighlight
import com.example.ui.theme.TacticalTextMuted
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary

@Composable
fun DeviceCommandAndForensicColumn(
    devices: List<TacticalDevice>,
    selectedDevice: TacticalDevice?,
    selectedFilter: RiskLevel?,
    backendBaseUrl: String,
    activeReport: ForensicReport?,
    forensicState: ForensicAnalysisUiState,
    isCompact: Boolean,
    onSelectDevice: (String) -> Unit,
    onFilterChange: (RiskLevel?) -> Unit,
    onUpdateBackendUrl: (String) -> Unit,
    onAnalyzeWithBackend: (rawLog: String) -> Unit,
    onToggleIsolate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleDevices = if (selectedFilter == null) {
        devices
    } else {
        devices.filter { it.riskLevel == selectedFilter }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TacticalPanelCard(
            title = "DEVICE COMMAND CENTER",
            accentColor = TacticalCyan,
            trailingHeader = {
                Text(
                    text = "${visibleDevices.size}/${devices.size} UNIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TacticalCyan
                )
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { onFilterChange(null) },
                    label = {
                        Text(
                            text = "SEMUA (${devices.size})",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TacticalCyanDark,
                        selectedLabelColor = TacticalCyan,
                        containerColor = TacticalBgDeep,
                        labelColor = TacticalTextSecondary
                    ),
                    modifier = Modifier.heightIn(min = 36.dp)
                )
                RiskLevel.entries.forEach { level ->
                    val count = devices.count { it.riskLevel == level }
                    FilterChip(
                        selected = selectedFilter == level,
                        onClick = { onFilterChange(level) },
                        label = {
                            Text(
                                text = "${level.label} ($count)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TacticalSurfaceHighlight,
                            selectedLabelColor = TacticalTextPrimary,
                            containerColor = TacticalBgDeep,
                            labelColor = TacticalTextSecondary
                        ),
                        modifier = Modifier.heightIn(min = 36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isCompact) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TacticalBgDeep, RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CALLSIGN",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalTextMuted,
                        modifier = Modifier.weight(1.1f)
                    )
                    Text(
                        text = "MODEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalTextMuted,
                        modifier = Modifier.weight(1.3f)
                    )
                    Text(
                        text = "LAST SYNC",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalTextMuted,
                        modifier = Modifier.weight(1.1f)
                    )
                    Text(
                        text = "BG DATA (MB)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalTextMuted,
                        modifier = Modifier.weight(0.95f)
                    )
                    Text(
                        text = "RISK LEVEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalTextMuted,
                        modifier = Modifier.weight(0.95f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (visibleDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada perangkat dengan filter status ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TacticalTextMuted
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    visibleDevices.forEach { device ->
                        val isSelected = device.id == selectedDevice?.id
                        DeviceRowItem(
                            device = device,
                            isSelected = isSelected,
                            isCompact = isCompact,
                            onClick = { onSelectDevice(device.id) }
                        )
                    }
                }
            }
        }

        AiForensicReportCard(
            device = selectedDevice,
            backendBaseUrl = backendBaseUrl,
            report = activeReport,
            forensicState = forensicState,
            isCompact = isCompact,
            onUpdateBackendUrl = onUpdateBackendUrl,
            onAnalyzeWithBackend = onAnalyzeWithBackend,
            onToggleIsolate = onToggleIsolate
        )
    }
}

@Composable
private fun DeviceRowItem(
    device: TacticalDevice,
    isSelected: Boolean,
    isCompact: Boolean,
    onClick: () -> Unit
) {
    val rowBg = if (isSelected) TacticalSurfaceHighlight else TacticalSurfaceElevated
    val rowBorder = if (isSelected) TacticalCyanBorder else TacticalBorder
    val callsignColor = when (device.riskLevel) {
        RiskLevel.CRITICAL -> TacticalRed
        RiskLevel.WATCH -> TacticalAmber
        RiskLevel.ISOLATED -> TacticalCyan
        RiskLevel.SECURE -> TacticalTextPrimary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .testTag("device_row_${device.callsign}"),
        shape = RoundedCornerShape(4.dp),
        color = rowBg,
        border = BorderStroke(1.dp, rowBorder)
    ) {
        if (isCompact) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.callsign,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = callsignColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = device.model,
                            style = MaterialTheme.typography.bodySmall,
                            color = TacticalTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    RiskLevelBadge(riskLevel = device.riskLevel)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SYNC: ${device.lastSync}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalTextMuted
                    )
                    Text(
                        text = "BG DATA: ${device.backgroundDataMb} MB",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (device.backgroundDataMb > 300) TacticalAmber else TacticalTextSecondary
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = device.callsign,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = callsignColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1.1f)
                )
                Text(
                    text = device.model,
                    style = MaterialTheme.typography.bodySmall,
                    color = TacticalTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = device.lastSync,
                    style = MaterialTheme.typography.bodySmall,
                    color = TacticalTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1.1f)
                )
                Text(
                    text = "${device.backgroundDataMb} MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (device.backgroundDataMb > 300) TacticalAmber else TacticalTextPrimary,
                    maxLines = 1,
                    modifier = Modifier.weight(0.95f)
                )
                Box(modifier = Modifier.weight(0.95f)) {
                    RiskLevelBadge(riskLevel = device.riskLevel)
                }
            }
        }
    }
}

@Composable
private fun AiForensicReportCard(
    device: TacticalDevice?,
    backendBaseUrl: String,
    report: ForensicReport?,
    forensicState: ForensicAnalysisUiState,
    isCompact: Boolean,
    onUpdateBackendUrl: (String) -> Unit,
    onAnalyzeWithBackend: (rawLog: String) -> Unit,
    onToggleIsolate: () -> Unit
) {
    val accent = when (device?.riskLevel) {
        RiskLevel.CRITICAL -> TacticalRed
        RiskLevel.WATCH -> TacticalAmber
        RiskLevel.ISOLATED -> TacticalCyan
        else -> TacticalGreen
    }

    var rawLogInput by remember(device?.id) {
        mutableStateOf(device?.defaultLogPayload.orEmpty())
    }

    TacticalPanelCard(
        title = "AI FORENSIC REPORT (INDONESIAN)",
        accentColor = accent,
        trailingHeader = {
            if (device != null) {
                RiskLevelBadge(riskLevel = device.riskLevel)
            }
        }
    ) {
        if (device == null) {
            Text(
                text = "Pilih perangkat pada Device Command Center untuk menjalankan analisis /api/analyze.",
                style = MaterialTheme.typography.bodyMedium,
                color = TacticalTextSecondary
            )
            return@TacticalPanelCard
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${device.callsign} • ${device.model}",
                    style = MaterialTheme.typography.titleLarge,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "UNIT: ${device.unitCode} | OS: ${device.osVersion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TacticalTextMuted
                )
            }
            if (report != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SKOR: ${report.threatScore}/100",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accent
                    )
                    Text(
                        text = "${report.aiModel} • ${report.generatedAt}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = backendBaseUrl,
            onValueChange = onUpdateBackendUrl,
            label = { Text("URL Backend Vercel (/api/analyze)") },
            placeholder = { Text("https://mosfad-diskomlekau.vercel.app") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("backend_url_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TacticalCyan,
                unfocusedBorderColor = TacticalBorder,
                focusedTextColor = TacticalTextPrimary,
                unfocusedTextColor = TacticalTextPrimary,
                focusedLabelColor = TacticalCyan,
                unfocusedLabelColor = TacticalTextSecondary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = rawLogInput,
            onValueChange = { rawLogInput = it },
            label = { Text("Log Telemetri / Logcat Perangkat") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("forensic_raw_log_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TacticalGreen,
                unfocusedBorderColor = TacticalBorder,
                focusedTextColor = TacticalTextPrimary,
                unfocusedTextColor = TacticalTextPrimary,
                focusedLabelColor = TacticalGreen,
                unfocusedLabelColor = TacticalTextSecondary
            )
        )

        if (forensicState is ForensicAnalysisUiState.Loading) {
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = TacticalCyan,
                trackColor = TacticalBgDeep
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Mengirim telemetri ${device.callsign} ke POST /api/analyze...",
                style = MaterialTheme.typography.bodySmall,
                color = TacticalCyan
            )
        }

        if (forensicState is ForensicAnalysisUiState.Error) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = TacticalRedDark,
                border = BorderStroke(1.dp, TacticalRedBorder)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = "Kesalahan Analisis",
                        tint = TacticalRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = forensicState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = TacticalRed
                    )
                }
            }
        }

        if (report != null) {
            Spacer(modifier = Modifier.height(10.dp))
            ForensicSectionBlock(
                header = "DETEKSI PENYADAPAN & BACKDOOR",
                body = report.wiretapBackdoorStatus,
                accentColor = if (report.riskLevel == RiskLevel.CRITICAL) TacticalRed else TacticalTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            ForensicSectionBlock(
                header = "ANOMALI TRAFIK & TRANSAKSI OTP",
                body = report.trafficOtpAnomalyStatus,
                accentColor = when (report.riskLevel) {
                    RiskLevel.CRITICAL -> TacticalRed
                    RiskLevel.WATCH -> TacticalAmber
                    else -> TacticalTextPrimary
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            ForensicSectionBlock(
                header = "FILTER PRIVASI",
                body = report.privacyFilterStatus,
                accentColor = TacticalGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = if (report.riskLevel == RiskLevel.CRITICAL) TacticalRedDark else TacticalSurfaceElevated,
                border = BorderStroke(
                    1.dp,
                    if (report.riskLevel == RiskLevel.CRITICAL) TacticalRedBorder else TacticalBorderBright
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Rekomendasi IRT",
                            tint = accent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REKOMENDASI TINDAKAN IRT DISKOMLEKAU",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = accent
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.irtRecommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TacticalTextPrimary
                    )
                }
            }
        } else if (forensicState is ForensicAnalysisUiState.Idle) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Tekan 'ANALISIS VIA /api/analyze' untuk menjalankan inspeksi forensik Gemini secara real-time melalui backend Vercel.",
                style = MaterialTheme.typography.bodySmall,
                color = TacticalTextMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = TacticalBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        if (isCompact) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AnalyzeBackendButton(
                    isLoading = forensicState is ForensicAnalysisUiState.Loading,
                    onClick = { onAnalyzeWithBackend(rawLogInput) },
                    modifier = Modifier.fillMaxWidth()
                )
                IsolateIrtActionButton(
                    isIsolated = device.isIsolated,
                    onClick = onToggleIsolate,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyzeBackendButton(
                    isLoading = forensicState is ForensicAnalysisUiState.Loading,
                    onClick = { onAnalyzeWithBackend(rawLogInput) },
                    modifier = Modifier.weight(1f)
                )
                IsolateIrtActionButton(
                    isIsolated = device.isIsolated,
                    onClick = onToggleIsolate,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AnalyzeBackendButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag("analyze_backend_button"),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TacticalCyanDark,
            contentColor = TacticalCyan
        ),
        border = BorderStroke(1.dp, TacticalCyanBorder)
    ) {
        Icon(
            imageVector = Icons.Filled.CloudUpload,
            contentDescription = "Analisis via /api/analyze",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isLoading) "MENGANALISIS..." else "ANALISIS VIA /api/analyze",
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun IsolateIrtActionButton(
    isIsolated: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag("isolate_irt_button"),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isIsolated) TacticalGreenDark else TacticalRedDark,
            contentColor = if (isIsolated) TacticalGreen else TacticalRed
        ),
        border = BorderStroke(1.dp, if (isIsolated) TacticalGreenBorder else TacticalRedBorder)
    ) {
        Icon(
            imageVector = if (isIsolated) Icons.Filled.CheckCircle else Icons.Filled.Lock,
            contentDescription = if (isIsolated) "Pulihkan Perangkat" else "Isolasi Perangkat atau Kirim IRT",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isIsolated) "PULIHKAN KONEKSI" else "ISOLASI PERANGKAT / KIRIM IRT",
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ForensicSectionBlock(
    header: String,
    body: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TacticalBgDeep, RoundedCornerShape(4.dp))
            .border(1.dp, TacticalBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = header,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TacticalTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = TacticalTextPrimary
        )
    }
}
