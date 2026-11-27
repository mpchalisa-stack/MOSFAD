package com.example.data

import com.example.model.ForensicReport
import com.example.model.RiskLevel
import com.example.model.TacticalDevice
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Contract for live AI forensic analysis routed through the Vercel /api/analyze backend.
 * No GEMINI_API_KEY is stored or accessed on the client.
 */
interface AiForensicRepository {
    suspend fun analyzeDevice(
        backendBaseUrl: String,
        device: TacticalDevice,
        rawLog: String
    ): ForensicReport
}

class RemoteBackendAiForensicRepository : AiForensicRepository {

    override suspend fun analyzeDevice(
        backendBaseUrl: String,
        device: TacticalDevice,
        rawLog: String
    ): ForensicReport = withContext(Dispatchers.IO) {
        val trimmedBase = backendBaseUrl.trim().trimEnd('/')
        if (trimmedBase.isBlank()) {
            throw IllegalStateException(
                "URL backend Vercel belum dikonfigurasi. Masukkan URL deployment (mis. https://mosfad-app.vercel.app) untuk memanggil /api/analyze."
            )
        }
        if (!trimmedBase.startsWith("https://") && !trimmedBase.startsWith("http://")) {
            throw IllegalArgumentException(
                "URL backend harus diawali https:// atau http://"
            )
        }

        val endpoint = URL("$trimmedBase/api/analyze")
        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 45_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
        }

        try {
            val payload = JSONObject().apply {
                put("callsign", device.callsign)
                put("model", device.model)
                put("backgroundDataMb", device.backgroundDataMb)
                put("lastSync", device.lastSync)
                put("unitCode", device.unitCode)
                put("osVersion", device.osVersion)
                put("rawLog", rawLog)
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val bodyText = stream?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText).orEmpty()

            if (code !in 200..299) {
                val errMsg = runCatching { JSONObject(bodyText).optString("error") }
                    .getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?: "Backend /api/analyze mengembalikan HTTP $code"
                throw IllegalStateException(errMsg)
            }

            val json = JSONObject(bodyText)
            val riskStr = json.optString("riskLevel", device.riskLevel.name).uppercase(Locale.US)
            val parsedRisk = runCatching { RiskLevel.valueOf(riskStr) }.getOrDefault(device.riskLevel)
            val nowClock = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            ForensicReport(
                deviceId = device.id,
                callsign = json.optString("callsign", device.callsign),
                generatedAt = nowClock,
                aiModel = json.optString("aiModel", "gemini-2.5-flash"),
                wiretapBackdoorStatus = json.getString("wiretapBackdoorStatus"),
                trafficOtpAnomalyStatus = json.getString("trafficOtpAnomalyStatus"),
                privacyFilterStatus = json.getString("privacyFilterStatus"),
                irtRecommendation = json.getString("irtRecommendation"),
                riskLevel = parsedRisk,
                threatScore = json.optInt("threatScore", 0),
                alertTitle = json.optString("alertTitle", "FORENSIC AUDIT"),
                alertDetail = json.optString("alertDetail", "")
            )
        } finally {
            connection.disconnect()
        }
    }
}
