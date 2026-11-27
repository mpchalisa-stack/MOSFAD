package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.EnrollmentSettings
import com.example.model.ProfileFormat
import com.example.ui.theme.TacticalBgDeep
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalCyan
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TacticalGreenBorder
import com.example.ui.theme.TacticalGreenDark
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalTextMuted
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary

@Composable
fun ClientEnrollmentColumn(
    enrollmentSettings: EnrollmentSettings,
    downloadProgress: Float,
    isDownloadingProfile: Boolean,
    onDownloadProfileClick: (ProfileFormat) -> Unit,
    onUpdateSettings: ((EnrollmentSettings) -> EnrollmentSettings) -> Unit,
    onEnrollClient: (callsign: String, model: String, unitCode: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEnrollDialog by remember { mutableStateOf(false) }
    var showDownloadFormatDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TacticalPanelCard(
            title = "CLIENT ENROLLMENT",
            accentColor = TacticalGreen
        ) {
            Text(
                text = "MDM management profile installation",
                style = MaterialTheme.typography.bodyMedium,
                color = TacticalTextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { showDownloadFormatDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("download_profile_button"),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TacticalGreenDark,
                    contentColor = TacticalGreen
                ),
                border = BorderStroke(1.dp, TacticalGreenBorder)
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Unduh Profil MDM",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDownloadingProfile) "PREPARING PROFILE..." else "DOWNLOAD PROFILE",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { downloadProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = TacticalGreen,
                trackColor = TacticalBgDeep
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ZERO-TOUCH QR TOKEN",
                    style = MaterialTheme.typography.titleSmall,
                    color = TacticalTextPrimary
                )
                Text(
                    text = "AES-256",
                    style = MaterialTheme.typography.labelSmall,
                    color = TacticalGreen
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            TacticalQrTokenBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 200.dp)
                    .align(Alignment.CenterHorizontally)
                    .testTag("qr_token_box")
            )
        }

        TacticalPanelCard(
            title = "ZERO-TOUCH / AUTOMATIC",
            accentColor = TacticalCyan
        ) {
            TacticalToggleRow(
                label = "ENROLLMENT",
                checked = enrollmentSettings.enrollmentEnabled,
                onCheckedChange = { enabled ->
                    onUpdateSettings { it.copy(enrollmentEnabled = enabled) }
                },
                testTag = "toggle_enrollment"
            )
            Text(
                text = "AUTOMATIC ENROLLMENT TO GRANT SYSTEM MANAGEMENT PERMISSIONS",
                style = MaterialTheme.typography.labelSmall,
                color = TacticalTextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TacticalToggleRow(
                label = "ALLOW DEVICE MGMT",
                checked = enrollmentSettings.allowDeviceMgmt,
                onCheckedChange = { enabled ->
                    onUpdateSettings { it.copy(allowDeviceMgmt = enabled) }
                },
                testTag = "toggle_allow_device_mgmt"
            )

            TacticalToggleRow(
                label = "AUTOMATIC JOIN",
                checked = enrollmentSettings.automaticJoin,
                onCheckedChange = { enabled ->
                    onUpdateSettings { it.copy(automaticJoin = enabled) }
                },
                testTag = "toggle_automatic_join"
            )

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { showEnrollDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("enroll_new_client_button"),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TacticalSurfaceElevated,
                    contentColor = TacticalTextPrimary
                ),
                border = BorderStroke(1.dp, TacticalGreenBorder)
            ) {
                Icon(
                    imageVector = Icons.Filled.AddCircleOutline,
                    contentDescription = "Tambah Perangkat Baru",
                    tint = TacticalGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ENROLL NEW CLIENT",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    if (showDownloadFormatDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadFormatDialog = false },
            containerColor = TacticalSurface,
            titleContentColor = TacticalTextPrimary,
            textContentColor = TacticalTextSecondary,
            title = {
                Text(
                    text = "UNDUH PROFIL MDM DISKOMLEKAU",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pilih format paket konfigurasi MDM taktis untuk disimpan:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = {
                            showDownloadFormatDialog = false
                            onDownloadProfileClick(ProfileFormat.JSON)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("export_profile_json"),
                        border = BorderStroke(1.dp, TacticalGreenBorder),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "SIMPAN PROFIL (.JSON)",
                            style = MaterialTheme.typography.labelLarge,
                            color = TacticalGreen
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            showDownloadFormatDialog = false
                            onDownloadProfileClick(ProfileFormat.MOBILECONFIG)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("export_profile_mobileconfig"),
                        border = BorderStroke(1.dp, TacticalBorder),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "SIMPAN KONFIGURASI (.MOBILECONFIG)",
                            style = MaterialTheme.typography.labelLarge,
                            color = TacticalCyan
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDownloadFormatDialog = false }) {
                    Text("TUTUP", color = TacticalTextSecondary)
                }
            }
        )
    }

    if (showEnrollDialog) {
        EnrollClientDialog(
            onDismiss = { showEnrollDialog = false },
            onConfirm = { callsign, model, unitCode ->
                onEnrollClient(callsign, model, unitCode)
                showEnrollDialog = false
            }
        )
    }
}

@Composable
private fun TacticalToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = TacticalTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = TacticalBgDeep,
                checkedTrackColor = TacticalGreen,
                uncheckedThumbColor = TacticalTextSecondary,
                uncheckedTrackColor = TacticalBgDeep,
                uncheckedBorderColor = TacticalBorder
            )
        )
    }
}

@Composable
private fun TacticalQrTokenBox(modifier: Modifier = Modifier) {
    val matrix = remember {
        listOf(
            "111111101011111",
            "100000100110001",
            "101110101010101",
            "101110101110101",
            "101110100010101",
            "100000101010001",
            "111111101011111",
            "000000001100000",
            "110110110101101",
            "011001001010010",
            "101101110110110",
            "000000001001011",
            "111111101101101",
            "100000100010010",
            "111111101011011"
        )
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(TacticalBgDeep)
            .border(1.dp, TacticalBorder, RoundedCornerShape(6.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F6F8), RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val count = matrix.size
                val cellW = size.width / count
                val cellH = size.height / count
                for (r in 0 until count) {
                    val rowStr = matrix[r]
                    for (c in 0 until count) {
                        if (rowStr[c] == '1') {
                            drawRect(
                                color = Color(0xFF0D1418),
                                topLeft = Offset(c * cellW, r * cellH),
                                size = Size(cellW + 0.5f, cellH + 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnrollClientDialog(
    onDismiss: () -> Unit,
    onConfirm: (callsign: String, model: String, unitCode: String) -> Unit
) {
    var callsign by remember { mutableStateOf("FALCON-08") }
    var model by remember { mutableStateOf("XCover7 Tactical") }
    var unitCode by remember { mutableStateOf("DISKOMLEKAU-OPS") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TacticalSurface,
        titleContentColor = TacticalTextPrimary,
        textContentColor = TacticalTextSecondary,
        title = {
            Text(
                text = "ENROLL PERANGKAT TAKTIS BARU",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = callsign,
                    onValueChange = { callsign = it },
                    label = { Text("Callsign Unit") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enroll_callsign_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TacticalGreen,
                        unfocusedBorderColor = TacticalBorder,
                        focusedTextColor = TacticalTextPrimary,
                        unfocusedTextColor = TacticalTextPrimary,
                        focusedLabelColor = TacticalGreen,
                        unfocusedLabelColor = TacticalTextSecondary
                    )
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model Perangkat Rugged") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enroll_model_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TacticalGreen,
                        unfocusedBorderColor = TacticalBorder,
                        focusedTextColor = TacticalTextPrimary,
                        unfocusedTextColor = TacticalTextPrimary,
                        focusedLabelColor = TacticalGreen,
                        unfocusedLabelColor = TacticalTextSecondary
                    )
                )
                OutlinedTextField(
                    value = unitCode,
                    onValueChange = { unitCode = it },
                    label = { Text("Unit / Kesatuan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TacticalGreen,
                        unfocusedBorderColor = TacticalBorder,
                        focusedTextColor = TacticalTextPrimary,
                        unfocusedTextColor = TacticalTextPrimary,
                        focusedLabelColor = TacticalGreen,
                        unfocusedLabelColor = TacticalTextSecondary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(callsign, model, unitCode) },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("confirm_enroll_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TacticalGreen,
                    contentColor = TacticalBgDeep
                )
            ) {
                Text("ENROLL", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("BATAL", color = TacticalTextSecondary)
            }
        }
    )
}
