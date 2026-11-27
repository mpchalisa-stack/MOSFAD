package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AlertFeedItem
import com.example.model.AlertSeverity
import com.example.model.EnrollmentSettings
import com.example.model.RiskLevel
import com.example.model.TacticalDevice
import com.example.model.TacticalLinkMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.mosfadDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mosfad_tactical_cache_v2"
)

data class PersistedMosfadState(
    val tacticalLinkMode: TacticalLinkMode,
    val backendBaseUrl: String,
    val devices: List<TacticalDevice>,
    val alerts: List<AlertFeedItem>,
    val selectedDeviceId: String,
    val enrollmentSettings: EnrollmentSettings
)

class MosfadLocalStore(private val context: Context) {

    private object Keys {
        val TACTICAL_LINK = stringPreferencesKey("tactical_link_mode")
        val BACKEND_BASE_URL = stringPreferencesKey("backend_base_url")
        val DEVICES_JSON = stringPreferencesKey("devices_json")
        val ALERTS_JSON = stringPreferencesKey("alerts_json")
        val SELECTED_DEVICE_ID = stringPreferencesKey("selected_device_id")
        val ENROLLMENT_ENABLED = booleanPreferencesKey("enrollment_enabled")
        val ALLOW_DEVICE_MGMT = booleanPreferencesKey("allow_device_mgmt")
        val AUTOMATIC_JOIN = booleanPreferencesKey("automatic_join")
    }

    val stateFlow: Flow<PersistedMosfadState> = context.mosfadDataStore.data.map { prefs ->
        val linkMode = prefs[Keys.TACTICAL_LINK]
            ?.let { runCatching { TacticalLinkMode.valueOf(it) }.getOrNull() }
            ?: TacticalLinkMode.ACTIVE

        val backendUrl = prefs[Keys.BACKEND_BASE_URL] ?: ""

        val devices = prefs[Keys.DEVICES_JSON]
            ?.let { decodeDevices(it) }
            ?.takeIf { it.isNotEmpty() }
            ?: defaultDevices()

        val alerts = prefs[Keys.ALERTS_JSON]
            ?.let { decodeAlerts(it) }
            ?: emptyList()

        val selectedId = prefs[Keys.SELECTED_DEVICE_ID] ?: devices.first().id

        val settings = EnrollmentSettings(
            enrollmentEnabled = prefs[Keys.ENROLLMENT_ENABLED] ?: true,
            allowDeviceMgmt = prefs[Keys.ALLOW_DEVICE_MGMT] ?: true,
            automaticJoin = prefs[Keys.AUTOMATIC_JOIN] ?: true
        )

        PersistedMosfadState(
            tacticalLinkMode = linkMode,
            backendBaseUrl = backendUrl,
            devices = devices,
            alerts = alerts,
            selectedDeviceId = selectedId,
            enrollmentSettings = settings
        )
    }

    suspend fun saveState(state: PersistedMosfadState) {
        context.mosfadDataStore.edit { prefs ->
            prefs[Keys.TACTICAL_LINK] = state.tacticalLinkMode.name
            prefs[Keys.BACKEND_BASE_URL] = state.backendBaseUrl
            prefs[Keys.DEVICES_JSON] = encodeDevices(state.devices)
            prefs[Keys.ALERTS_JSON] = encodeAlerts(state.alerts)
            prefs[Keys.SELECTED_DEVICE_ID] = state.selectedDeviceId
            prefs[Keys.ENROLLMENT_ENABLED] = state.enrollmentSettings.enrollmentEnabled
            prefs[Keys.ALLOW_DEVICE_MGMT] = state.enrollmentSettings.allowDeviceMgmt
            prefs[Keys.AUTOMATIC_JOIN] = state.enrollmentSettings.automaticJoin
        }
    }

    private fun encodeDevices(devices: List<TacticalDevice>): String {
        val array = JSONArray()
        devices.forEach { d ->
            array.put(
                JSONObject().apply {
                    put("id", d.id)
                    put("callsign", d.callsign)
                    put("model", d.model)
                    put("lastSync", d.lastSync)
                    put("backgroundDataMb", d.backgroundDataMb)
                    put("riskLevel", d.riskLevel.name)
                    put("unitCode", d.unitCode)
                    put("osVersion", d.osVersion)
                    put("defaultLogPayload", d.defaultLogPayload)
                    put("isIsolated", d.isIsolated)
                }
            )
        }
        return array.toString()
    }

    private fun decodeDevices(raw: String): List<TacticalDevice> = runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    TacticalDevice(
                        id = obj.getString("id"),
                        callsign = obj.getString("callsign"),
                        model = obj.getString("model"),
                        lastSync = obj.getString("lastSync"),
                        backgroundDataMb = obj.getInt("backgroundDataMb"),
                        riskLevel = RiskLevel.valueOf(obj.getString("riskLevel")),
                        unitCode = obj.optString("unitCode", "DISKOMLEKAU-OPS"),
                        osVersion = obj.optString("osVersion", "Android 15 Hardened"),
                        defaultLogPayload = obj.optString("defaultLogPayload", ""),
                        isIsolated = obj.optBoolean("isIsolated", false)
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    private fun encodeAlerts(alerts: List<AlertFeedItem>): String {
        val array = JSONArray()
        alerts.forEach { a ->
            array.put(
                JSONObject().apply {
                    put("id", a.id)
                    put("timestamp", a.timestamp)
                    put("title", a.title)
                    put("callsign", a.callsign)
                    put("severity", a.severity.name)
                    put("detail", a.detail)
                }
            )
        }
        return array.toString()
    }

    private fun decodeAlerts(raw: String): List<AlertFeedItem> = runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    AlertFeedItem(
                        id = obj.getString("id"),
                        timestamp = obj.getString("timestamp"),
                        title = obj.getString("title"),
                        callsign = obj.getString("callsign"),
                        severity = AlertSeverity.valueOf(obj.getString("severity")),
                        detail = obj.getString("detail")
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    companion object {
        fun defaultDevices(): List<TacticalDevice> = listOf(
            TacticalDevice(
                id = "dev-ghost-01",
                callsign = "GHOST-01",
                model = "Rugged Tab S9",
                lastSync = "20/15:29 ARD",
                backgroundDataMb = 42,
                riskLevel = RiskLevel.SECURE,
                unitCode = "SAT-SIBER-01",
                osVersion = "Android 15 Hardened",
                defaultLogPayload = "SELinux=Enforcing; VPN=Diskomlekau-IPSec UP; bg_upload=42MB; sms_otp_perm=NONE"
            ),
            TacticalDevice(
                id = "dev-viper-03",
                callsign = "VIPER-03",
                model = "XCover7 Tactical",
                lastSync = "20/16:29 ARD",
                backgroundDataMb = 894,
                riskLevel = RiskLevel.CRITICAL,
                unitCode = "KOOPSUD-03",
                osVersion = "Android 14 Rugged",
                defaultLogPayload = "proc=com.sys.tactical.bridge outbound_socket=185.220.101.4:8443; bg_upload=894MB; perm_req=READ_SMS,RECEIVE_SMS(OTP)"
            ),
            TacticalDevice(
                id = "dev-shadow-09",
                callsign = "SHADOW-09",
                model = "Sonim XP10",
                lastSync = "20/15:29 ARD",
                backgroundDataMb = 318,
                riskLevel = RiskLevel.WATCH,
                unitCode = "DISK-OPS-09",
                osVersion = "Android 14 Mil",
                defaultLogPayload = "proc=svc.telemetri.relay udp_burst=318MB port=51822; cert_pinning=MISMATCH"
            ),
            TacticalDevice(
                id = "dev-elang-04",
                callsign = "ELANG-04",
                model = "Pixel 9 Pro Mil",
                lastSync = "20/15:41 ARD",
                backgroundDataMb = 245,
                riskLevel = RiskLevel.WATCH,
                unitCode = "LANUD-HLM-04",
                osVersion = "Android 15 Graphene",
                defaultLogPayload = "proc=audiodaemon_ext bg_data=245MB; notif_listener=OTP_INTERCEPT_ATTEMPT"
            ),
            TacticalDevice(
                id = "dev-rajawali-02",
                callsign = "RAJAWALI-02",
                model = "CAT S75 Sat",
                lastSync = "20/15:18 ARD",
                backgroundDataMb = 64,
                riskLevel = RiskLevel.SECURE,
                unitCode = "SATKOM-02",
                osVersion = "Android 14 Sat-Link",
                defaultLogPayload = "ntn_sat_link=ENCRYPTED_AES256; bg_data=64MB; integrity=VERIFIED"
            ),
            TacticalDevice(
                id = "dev-garuda-07",
                callsign = "GARUDA-07",
                model = "Toughbook FZ-N1",
                lastSync = "20/14:55 ARD",
                backgroundDataMb = 51,
                riskLevel = RiskLevel.SECURE,
                unitCode = "DISKOM-HQ-07",
                osVersion = "Android 14 Rugged",
                defaultLogPayload = "bootloader=LOCKED; selinux=ENFORCING; bg_data=51MB; zero_touch=ACTIVE"
            ),
            TacticalDevice(
                id = "dev-cakra-05",
                callsign = "CAKRA-05",
                model = "Armor 24 Tactical",
                lastSync = "20/15:10 ARD",
                backgroundDataMb = 88,
                riskLevel = RiskLevel.SECURE,
                unitCode = "RADAR-05",
                osVersion = "Android 14 Hardened",
                defaultLogPayload = "vpn_diskomlekau=STABLE; bg_data=88MB; anomalies=0"
            )
        )
    }
}
