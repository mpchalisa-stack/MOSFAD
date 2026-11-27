package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiForensicRepository
import com.example.data.MosfadLocalStore
import com.example.data.PersistedMosfadState
import com.example.data.RemoteBackendAiForensicRepository
import com.example.model.AlertFeedItem
import com.example.model.AlertSeverity
import com.example.model.EnrollmentSettings
import com.example.model.ForensicAnalysisUiState
import com.example.model.MosfadUiState
import com.example.model.ProfileFormat
import com.example.model.RiskLevel
import com.example.model.TacticalDevice
import com.example.model.TacticalLinkMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MosfadViewModel(application: Application) : AndroidViewModel(application) {

    private val localStore = MosfadLocalStore(application.applicationContext)
    private val aiRepository: AiForensicRepository = RemoteBackendAiForensicRepository()

    private val _uiState = MutableStateFlow(
        MosfadUiState(
            devices = MosfadLocalStore.defaultDevices(),
            alerts = emptyList(),
            selectedDeviceId = "dev-viper-03"
        )
    )
    val uiState: StateFlow<MosfadUiState> = _uiState.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    private var analyzeJob: Job? = null

    init {
        viewModelScope.launch {
            val saved = localStore.stateFlow.first()
            _uiState.update {
                it.copy(
                    tacticalLinkMode = saved.tacticalLinkMode,
                    backendBaseUrl = saved.backendBaseUrl,
                    devices = saved.devices,
                    alerts = saved.alerts,
                    selectedDeviceId = saved.selectedDeviceId,
                    enrollmentSettings = saved.enrollmentSettings
                )
            }
        }
    }

    fun toggleTacticalLink() {
        val current = _uiState.value
        val nextMode = if (current.tacticalLinkMode == TacticalLinkMode.ACTIVE) {
            TacticalLinkMode.OFFLINE_CACHE
        } else {
            TacticalLinkMode.ACTIVE
        }
        _uiState.update { it.copy(tacticalLinkMode = nextMode) }
        persistCurrentState()
        val msg = if (nextMode == TacticalLinkMode.ACTIVE) {
            "Tactical Link AKTIF — Siap terhubung ke endpoint /api/analyze."
        } else {
            "Mode OFFLINE CACHE aktif — Menggunakan data lokal tersimpan."
        }
        _snackbarEvents.tryEmit(msg)
    }

    fun updateBackendBaseUrl(url: String) {
        _uiState.update { it.copy(backendBaseUrl = url) }
        persistCurrentState()
    }

    fun selectDevice(deviceId: String) {
        val device = _uiState.value.devices.firstOrNull { it.id == deviceId } ?: return
        val existingReport = _uiState.value.reportsByDevice[device.id]
        _uiState.update {
            it.copy(
                selectedDeviceId = deviceId,
                forensicState = if (existingReport != null) {
                    ForensicAnalysisUiState.Success(existingReport)
                } else {
                    ForensicAnalysisUiState.Idle
                }
            )
        }
        persistCurrentState()
    }

    fun analyzeSelectedDeviceWithBackend(rawLog: String) {
        val state = _uiState.value
        val device = state.selectedDevice ?: return
        if (state.tacticalLinkMode == TacticalLinkMode.OFFLINE_CACHE) {
            _uiState.update {
                it.copy(
                    forensicState = ForensicAnalysisUiState.Error(
                        "Tactical Link dalam mode Offline Cache. Aktifkan Tactical Link untuk memanggil /api/analyze."
                    )
                )
            }
            return
        }
        val cleanLog = rawLog.trim()
        if (cleanLog.isBlank()) {
            _uiState.update {
                it.copy(
                    forensicState = ForensicAnalysisUiState.Error(
                        "Masukkan log telemetri perangkat sebelum menjalankan analisis /api/analyze."
                    )
                )
            }
            return
        }

        analyzeJob?.cancel()
        analyzeJob = viewModelScope.launch {
            _uiState.update { it.copy(forensicState = ForensicAnalysisUiState.Loading) }
            try {
                val report = aiRepository.analyzeDevice(
                    backendBaseUrl = state.backendBaseUrl,
                    device = device,
                    rawLog = cleanLog
                )
                val nowTime = currentClock()
                val syncStamp = currentSyncStamp()

                val updatedDevices = _uiState.value.devices.map { d ->
                    if (d.id == device.id) {
                        d.copy(
                            riskLevel = if (d.isIsolated) RiskLevel.ISOLATED else report.riskLevel,
                            lastSync = syncStamp,
                            defaultLogPayload = cleanLog
                        )
                    } else {
                        d
                    }
                }

                val newAlert = if (report.riskLevel == RiskLevel.CRITICAL || report.riskLevel == RiskLevel.WATCH) {
                    AlertFeedItem(
                        id = "alt-${UUID.randomUUID()}",
                        timestamp = nowTime,
                        title = report.alertTitle.ifBlank { "FORENSIC ALERT" },
                        callsign = device.callsign,
                        severity = if (report.riskLevel == RiskLevel.CRITICAL) {
                            AlertSeverity.CRITICAL
                        } else {
                            AlertSeverity.WATCH
                        },
                        detail = report.alertDetail.ifBlank { report.irtRecommendation }
                    )
                } else {
                    null
                }

                _uiState.update { cur ->
                    cur.copy(
                        devices = updatedDevices,
                        alerts = if (newAlert != null) listOf(newAlert) + cur.alerts else cur.alerts,
                        reportsByDevice = cur.reportsByDevice + (device.id to report),
                        forensicState = ForensicAnalysisUiState.Success(report)
                    )
                }
                persistCurrentState()
                _snackbarEvents.tryEmit(
                    "Analisis /api/analyze (${report.aiModel}) untuk ${device.callsign} selesai."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        forensicState = ForensicAnalysisUiState.Error(
                            e.localizedMessage ?: "Gagal menghubungi endpoint /api/analyze."
                        )
                    )
                }
            }
        }
    }

    fun setRiskFilter(filter: RiskLevel?) {
        _uiState.update {
            val next = if (it.selectedFilter == filter) null else filter
            it.copy(selectedFilter = next)
        }
    }

    fun toggleIsolateSelectedDevice() {
        val device = _uiState.value.selectedDevice ?: return
        val willIsolate = !device.isIsolated
        val nowTime = currentClock()
        val syncStamp = currentSyncStamp()

        val updatedDevices = _uiState.value.devices.map { d ->
            if (d.id == device.id) {
                if (willIsolate) {
                    d.copy(
                        isIsolated = true,
                        riskLevel = RiskLevel.ISOLATED,
                        lastSync = syncStamp
                    )
                } else {
                    d.copy(
                        isIsolated = false,
                        riskLevel = RiskLevel.SECURE,
                        backgroundDataMb = 36,
                        lastSync = syncStamp
                    )
                }
            } else {
                d
            }
        }

        val newAlert = AlertFeedItem(
            id = "alt-${UUID.randomUUID()}",
            timestamp = nowTime,
            title = if (willIsolate) "IRT ISOLATION SENT" else "DEVICE RESTORED",
            callsign = device.callsign,
            severity = AlertSeverity.OPERATIONAL,
            detail = if (willIsolate) {
                "Tim IRT Diskomlekau dikirim; jaringan perangkat diisolasi penuh."
            } else {
                "Karantina dicabut setelah verifikasi MDM bersih."
            }
        )

        _uiState.update {
            it.copy(
                devices = updatedDevices,
                alerts = listOf(newAlert) + it.alerts
            )
        }
        persistCurrentState()
        _snackbarEvents.tryEmit(
            if (willIsolate) {
                "Perintah isolasi & pengiriman Tim IRT untuk ${device.callsign} aktif."
            } else {
                "Koneksi ${device.callsign} dipulihkan ke status SECURE."
            }
        )
    }

    fun updateEnrollmentSettings(transform: (EnrollmentSettings) -> EnrollmentSettings) {
        _uiState.update { state ->
            state.copy(enrollmentSettings = transform(state.enrollmentSettings))
        }
        persistCurrentState()
    }

    fun enrollNewClient(callsignInput: String, modelInput: String, unitInput: String) {
        val cleanCallsign = callsignInput.trim().uppercase(Locale.getDefault())
            .ifBlank { "FALCON-08" }
        val cleanModel = modelInput.trim().ifBlank { "Samsung XCover7 Tactical" }
        val cleanUnit = unitInput.trim().ifBlank { "DISKOMLEKAU-OPS" }
        val syncStamp = currentSyncStamp()
        val nowTime = currentClock()

        val newDevice = TacticalDevice(
            id = "dev-${UUID.randomUUID()}",
            callsign = cleanCallsign,
            model = cleanModel,
            lastSync = syncStamp,
            backgroundDataMb = 0,
            riskLevel = RiskLevel.SECURE,
            unitCode = cleanUnit,
            defaultLogPayload = "enrollment=ZERO_TOUCH unit=$cleanUnit selinux=ENFORCING"
        )

        val enrollAlert = AlertFeedItem(
            id = "alt-${UUID.randomUUID()}",
            timestamp = nowTime,
            title = "CLIENT ENROLLED",
            callsign = cleanCallsign,
            severity = AlertSeverity.OPERATIONAL,
            detail = "Registrasi MDM Zero-Touch ($cleanModel) berhasil ditambahkan."
        )

        _uiState.update { state ->
            state.copy(
                devices = listOf(newDevice) + state.devices,
                alerts = listOf(enrollAlert) + state.alerts,
                selectedDeviceId = newDevice.id,
                forensicState = ForensicAnalysisUiState.Idle
            )
        }
        persistCurrentState()
        _snackbarEvents.tryEmit("Perangkat $cleanCallsign berhasil di-enroll ke MOSFAD.")
    }

    fun triggerProfileDownloadAnimation() {
        if (_uiState.value.isDownloadingProfile) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingProfile = true, profileDownloadProgress = 0.12f) }
            for (step in 2..10) {
                delay(45L)
                _uiState.update { it.copy(profileDownloadProgress = step / 10f) }
            }
            _uiState.update { it.copy(isDownloadingProfile = false, profileDownloadProgress = 1f) }
        }
    }

    fun buildMdmProfileContent(format: ProfileFormat): String {
        val settings = _uiState.value.enrollmentSettings
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        return if (format == ProfileFormat.JSON) {
            """
            {
              "profileName": "MOSFAD-DISKOMLEKAU-MDM-TACTICAL",
              "organization": "Diskomlekau Cyber & Forensic Command",
              "version": "2.4.0",
              "generatedAt": "$timestamp",
              "zeroTouch": {
                "enrollmentEnabled": ${settings.enrollmentEnabled},
                "allowDeviceManagement": ${settings.allowDeviceMgmt},
                "automaticJoin": ${settings.automaticJoin}
              }
            }
            """.trimIndent()
        } else {
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            <dict>
                <key>PayloadDisplayName</key>
                <string>MOSFAD Diskomlekau Tactical MDM</string>
                <key>PayloadIdentifier</key>
                <string>id.mil.tni-au.diskomlekau.mosfad.mdm</string>
                <key>ZeroTouchEnrollment</key>
                <${settings.enrollmentEnabled}/>
                <key>AllowDeviceManagement</key>
                <${settings.allowDeviceMgmt}/>
                <key>AutomaticJoin</key>
                <${settings.automaticJoin}/>
            </dict>
            </plist>
            """.trimIndent()
        }
    }

    private fun persistCurrentState() {
        val s = _uiState.value
        viewModelScope.launch {
            localStore.saveState(
                PersistedMosfadState(
                    tacticalLinkMode = s.tacticalLinkMode,
                    backendBaseUrl = s.backendBaseUrl,
                    devices = s.devices,
                    alerts = s.alerts,
                    selectedDeviceId = s.selectedDeviceId,
                    enrollmentSettings = s.enrollmentSettings
                )
            )
        }
    }

    private fun currentClock(): String =
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    private fun currentSyncStamp(): String =
        SimpleDateFormat("dd/HH:mm 'ARD'", Locale.getDefault()).format(Date())
}
