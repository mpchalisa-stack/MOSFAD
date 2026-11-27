package com.example.model

enum class RiskLevel(val label: String) {
    SECURE("SECURE"),
    WATCH("WATCH"),
    CRITICAL("CRITICAL"),
    ISOLATED("ISOLATED")
}

enum class AlertSeverity(val tag: String) {
    CRITICAL("// CRITICAL"),
    WATCH("// WATCH"),
    OPERATIONAL("// IRT-OPS")
}

enum class TacticalLinkMode(val label: String) {
    ACTIVE("TACTICAL LINK: ACTIVE"),
    OFFLINE_CACHE("TACTICAL LINK: OFFLINE CACHE")
}

enum class ProfileFormat(val extension: String, val mimeType: String, val label: String) {
    JSON("json", "application/json", "MDM Profile (.json)"),
    MOBILECONFIG("mobileconfig", "application/x-apple-aspen-config", "MDM Config (.mobileconfig)")
}

data class TacticalDevice(
    val id: String,
    val callsign: String,
    val model: String,
    val lastSync: String,
    val backgroundDataMb: Int,
    val riskLevel: RiskLevel,
    val unitCode: String = "DISKOMLEKAU-OPS",
    val osVersion: String = "Android 15 Hardened",
    val defaultLogPayload: String = "",
    val isIsolated: Boolean = false
)

data class ForensicReport(
    val deviceId: String,
    val callsign: String,
    val generatedAt: String,
    val aiModel: String,
    val wiretapBackdoorStatus: String,
    val trafficOtpAnomalyStatus: String,
    val privacyFilterStatus: String,
    val irtRecommendation: String,
    val riskLevel: RiskLevel,
    val threatScore: Int,
    val alertTitle: String,
    val alertDetail: String
)

sealed interface ForensicAnalysisUiState {
    data object Idle : ForensicAnalysisUiState
    data object Loading : ForensicAnalysisUiState
    data class Success(val report: ForensicReport) : ForensicAnalysisUiState
    data class Error(val message: String) : ForensicAnalysisUiState
}

data class AlertFeedItem(
    val id: String,
    val timestamp: String,
    val title: String,
    val callsign: String,
    val severity: AlertSeverity,
    val detail: String
)

data class EnrollmentSettings(
    val enrollmentEnabled: Boolean = true,
    val allowDeviceMgmt: Boolean = true,
    val automaticJoin: Boolean = true
)

data class MosfadUiState(
    val tacticalLinkMode: TacticalLinkMode = TacticalLinkMode.ACTIVE,
    val backendBaseUrl: String = "",
    val devices: List<TacticalDevice> = emptyList(),
    val alerts: List<AlertFeedItem> = emptyList(),
    val selectedDeviceId: String = "",
    val selectedFilter: RiskLevel? = null,
    val enrollmentSettings: EnrollmentSettings = EnrollmentSettings(),
    val reportsByDevice: Map<String, ForensicReport> = emptyMap(),
    val forensicState: ForensicAnalysisUiState = ForensicAnalysisUiState.Idle,
    val profileDownloadProgress: Float = 0.0f,
    val isDownloadingProfile: Boolean = false
) {
    val selectedDevice: TacticalDevice?
        get() = devices.firstOrNull { it.id == selectedDeviceId } ?: devices.firstOrNull()

    val activeReport: ForensicReport?
        get() = selectedDevice?.let { reportsByDevice[it.id] }

    val totalDevicesCount: Int
        get() = devices.size

    val watchAlertsCount: Int
        get() = devices.count { it.riskLevel == RiskLevel.WATCH }

    val criticalThreatsCount: Int
        get() = devices.count { it.riskLevel == RiskLevel.CRITICAL && !it.isIsolated }

    val systemStatusLabel: String
        get() = if (criticalThreatsCount > 1) "SIAGA" else "SECURE"

    val systemStatusSubLabel: String
        get() = when {
            tacticalLinkMode == TacticalLinkMode.OFFLINE_CACHE -> "OFFLINE CACHE"
            criticalThreatsCount > 1 -> "ACTION REQUIRED"
            else -> "OPERATIONAL"
        }
}
