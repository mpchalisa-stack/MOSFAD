package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ProfileFormat
import com.example.ui.components.ClientEnrollmentColumn
import com.example.ui.components.CriticalAlertFeedColumn
import com.example.ui.components.DeviceCommandAndForensicColumn
import com.example.ui.components.KpiCardsRow
import com.example.ui.components.MosfadTopBar
import com.example.ui.components.tacticalGridBackground
import com.example.ui.theme.TacticalBgDeep
import com.example.ui.theme.TacticalCyan
import com.example.ui.theme.TacticalCyanDark
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TacticalGreenDark
import com.example.ui.theme.TacticalRed
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalTextMuted
import com.example.ui.theme.TacticalTextPrimary
import kotlinx.coroutines.launch

private enum class CompactSectionTab {
    COMMAND,
    ENROLLMENT,
    ALERTS
}

@Composable
fun MosfadDashboardScreen(
    viewModel: MosfadViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingProfileFormat by remember { mutableStateOf(ProfileFormat.JSON) }
    var compactTab by rememberSaveable { mutableStateOf(CompactSectionTab.COMMAND) }

    val saveProfileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val payload = viewModel.buildMdmProfileContent(pendingProfileFormat)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(payload.toByteArray(Charsets.UTF_8))
                }
            }.onSuccess {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Profil MDM (${pendingProfileFormat.extension}) berhasil disimpan."
                    )
                }
            }.onFailure { err ->
                Toast.makeText(
                    context,
                    "Gagal menyimpan profil: ${err.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { msg ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    val handleDownloadProfile: (ProfileFormat) -> Unit = { format ->
        pendingProfileFormat = format
        viewModel.triggerProfileDownloadAnimation()
        val fileName = "mosfad-diskomlekau-mdm.${format.extension}"
        saveProfileLauncher.launch(fileName)
    }

    val handleAlertSelected: (String) -> Unit = { callsign ->
        val matched = state.devices.firstOrNull {
            it.callsign.equals(callsign, ignoreCase = true)
        }
        if (matched != null) {
            viewModel.selectDevice(matched.id)
            compactTab = CompactSectionTab.COMMAND
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(TacticalBgDeep)
            .tacticalGridBackground()
    ) {
        val isCompact = maxWidth < 600.dp
        val isExpanded = maxWidth >= 960.dp

        if (isCompact && compactTab != CompactSectionTab.COMMAND) {
            BackHandler {
                compactTab = CompactSectionTab.COMMAND
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = TacticalSurfaceElevated,
                        contentColor = TacticalTextPrimary,
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            },
            bottomBar = {
                if (isCompact) {
                    NavigationBar(
                        containerColor = TacticalSurfaceElevated,
                        tonalElevation = 4.dp
                    ) {
                        NavigationBarItem(
                            selected = compactTab == CompactSectionTab.COMMAND,
                            onClick = { compactTab = CompactSectionTab.COMMAND },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = "Command Center"
                                )
                            },
                            label = {
                                Text(
                                    text = "COMMAND",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TacticalCyan,
                                selectedTextColor = TacticalCyan,
                                indicatorColor = TacticalCyanDark,
                                unselectedIconColor = TacticalTextMuted,
                                unselectedTextColor = TacticalTextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_command")
                        )
                        NavigationBarItem(
                            selected = compactTab == CompactSectionTab.ENROLLMENT,
                            onClick = { compactTab = CompactSectionTab.ENROLLMENT },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.QrCode2,
                                    contentDescription = "Client Enrollment"
                                )
                            },
                            label = {
                                Text(
                                    text = "ENROLL",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TacticalGreen,
                                selectedTextColor = TacticalGreen,
                                indicatorColor = TacticalGreenDark,
                                unselectedIconColor = TacticalTextMuted,
                                unselectedTextColor = TacticalTextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_enrollment")
                        )
                        NavigationBarItem(
                            selected = compactTab == CompactSectionTab.ALERTS,
                            onClick = { compactTab = CompactSectionTab.ALERTS },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = "Critical Alert Feed"
                                )
                            },
                            label = {
                                Text(
                                    text = "ALERTS (${state.alerts.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TacticalRed,
                                selectedTextColor = TacticalRed,
                                indicatorColor = TacticalSurface,
                                unselectedIconColor = TacticalTextMuted,
                                unselectedTextColor = TacticalTextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_alerts")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                MosfadTopBar(
                    linkMode = state.tacticalLinkMode,
                    isCompact = isCompact,
                    onToggleLinkMode = viewModel::toggleTacticalLink
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = if (isCompact) 10.dp else 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCardsRow(
                        state = state,
                        isCompact = isCompact
                    )

                    when {
                        isExpanded -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ClientEnrollmentColumn(
                                    enrollmentSettings = state.enrollmentSettings,
                                    downloadProgress = state.profileDownloadProgress,
                                    isDownloadingProfile = state.isDownloadingProfile,
                                    onDownloadProfileClick = handleDownloadProfile,
                                    onUpdateSettings = viewModel::updateEnrollmentSettings,
                                    onEnrollClient = viewModel::enrollNewClient,
                                    modifier = Modifier.weight(0.24f)
                                )
                                DeviceCommandAndForensicColumn(
                                    devices = state.devices,
                                    selectedDevice = state.selectedDevice,
                                    selectedFilter = state.selectedFilter,
                                    backendBaseUrl = state.backendBaseUrl,
                                    activeReport = state.activeReport,
                                    forensicState = state.forensicState,
                                    isCompact = false,
                                    onSelectDevice = viewModel::selectDevice,
                                    onFilterChange = viewModel::setRiskFilter,
                                    onUpdateBackendUrl = viewModel::updateBackendBaseUrl,
                                    onAnalyzeWithBackend = viewModel::analyzeSelectedDeviceWithBackend,
                                    onToggleIsolate = viewModel::toggleIsolateSelectedDevice,
                                    modifier = Modifier.weight(0.51f)
                                )
                                CriticalAlertFeedColumn(
                                    alerts = state.alerts,
                                    onAlertClick = handleAlertSelected,
                                    modifier = Modifier.weight(0.25f)
                                )
                            }
                        }

                        !isCompact -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DeviceCommandAndForensicColumn(
                                    devices = state.devices,
                                    selectedDevice = state.selectedDevice,
                                    selectedFilter = state.selectedFilter,
                                    backendBaseUrl = state.backendBaseUrl,
                                    activeReport = state.activeReport,
                                    forensicState = state.forensicState,
                                    isCompact = false,
                                    onSelectDevice = viewModel::selectDevice,
                                    onFilterChange = viewModel::setRiskFilter,
                                    onUpdateBackendUrl = viewModel::updateBackendBaseUrl,
                                    onAnalyzeWithBackend = viewModel::analyzeSelectedDeviceWithBackend,
                                    onToggleIsolate = viewModel::toggleIsolateSelectedDevice,
                                    modifier = Modifier.weight(0.60f)
                                )
                                Column(
                                    modifier = Modifier.weight(0.40f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CriticalAlertFeedColumn(
                                        alerts = state.alerts,
                                        onAlertClick = handleAlertSelected
                                    )
                                    ClientEnrollmentColumn(
                                        enrollmentSettings = state.enrollmentSettings,
                                        downloadProgress = state.profileDownloadProgress,
                                        isDownloadingProfile = state.isDownloadingProfile,
                                        onDownloadProfileClick = handleDownloadProfile,
                                        onUpdateSettings = viewModel::updateEnrollmentSettings,
                                        onEnrollClient = viewModel::enrollNewClient
                                    )
                                }
                            }
                        }

                        else -> {
                            when (compactTab) {
                                CompactSectionTab.COMMAND -> {
                                    DeviceCommandAndForensicColumn(
                                        devices = state.devices,
                                        selectedDevice = state.selectedDevice,
                                        selectedFilter = state.selectedFilter,
                                        backendBaseUrl = state.backendBaseUrl,
                                        activeReport = state.activeReport,
                                        forensicState = state.forensicState,
                                        isCompact = true,
                                        onSelectDevice = viewModel::selectDevice,
                                        onFilterChange = viewModel::setRiskFilter,
                                        onUpdateBackendUrl = viewModel::updateBackendBaseUrl,
                                        onAnalyzeWithBackend = viewModel::analyzeSelectedDeviceWithBackend,
                                        onToggleIsolate = viewModel::toggleIsolateSelectedDevice,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                CompactSectionTab.ENROLLMENT -> {
                                    ClientEnrollmentColumn(
                                        enrollmentSettings = state.enrollmentSettings,
                                        downloadProgress = state.profileDownloadProgress,
                                        isDownloadingProfile = state.isDownloadingProfile,
                                        onDownloadProfileClick = handleDownloadProfile,
                                        onUpdateSettings = viewModel::updateEnrollmentSettings,
                                        onEnrollClient = viewModel::enrollNewClient,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                CompactSectionTab.ALERTS -> {
                                    CriticalAlertFeedColumn(
                                        alerts = state.alerts,
                                        onAlertClick = handleAlertSelected,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
