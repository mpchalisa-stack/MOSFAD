import { GoogleGenAI, Type } from "@google/genai";

const FORENSIC_RESPONSE_SCHEMA = {
  type: Type.OBJECT,
  properties: {
    wiretapBackdoorStatus: {
      type: Type.STRING,
      description: "Analisis deteksi penyadapan, rootkit, atau backdoor dalam Bahasa Indonesia."
    },
    trafficOtpAnomalyStatus: {
      type: Type.STRING,
      description: "Analisis anomali trafik jaringan latar belakang dan akses SMS/OTP ilegal dalam Bahasa Indonesia."
    },
    privacyFilterStatus: {
      type: Type.STRING,
      description: "Status filter privasi terhadap pesan pribadi personel dalam Bahasa Indonesia."
    },
    irtRecommendation: {
      type: Type.STRING,
      description: "Rekomendasi tindakan taktis bagi Tim Respon Insiden (IRT) Diskomlekau dalam Bahasa Indonesia."
    },
    riskLevel: {
      type: Type.STRING,
      enum: ["SECURE", "WATCH", "CRITICAL"],
      description: "Klasifikasi tingkat risiko perangkat berdasarkan hasil forensik."
    },
    threatScore: {
      type: Type.INTEGER,
      description: "Skor ancaman numerik antara 0 hingga 100."
    },
    alertTitle: {
      type: Type.STRING,
      description: "Judul ringkas alert taktis dalam huruf kapital, misalnya DATA SPIKE atau CLEAN AUDIT."
    },
    alertDetail: {
      type: Type.STRING,
      description: "Ringkasan temuan singkat satu kalimat untuk Critical Alert Feed."
    }
  },
  required: [
    "wiretapBackdoorStatus",
    "trafficOtpAnomalyStatus",
    "privacyFilterStatus",
    "irtRecommendation",
    "riskLevel",
    "threatScore",
    "alertTitle",
    "alertDetail"
  ]
};

export default async function handler(req, res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    return res.status(204).end();
  }

  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed. Gunakan POST /api/analyze." });
  }

  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey || !apiKey.trim()) {
    return res.status(500).json({
      error: "GEMINI_API_KEY belum dikonfigurasi pada Environment Variables Vercel."
    });
  }

  try {
    const body = typeof req.body === "string" ? JSON.parse(req.body) : req.body || {};
    const callsign = String(body.callsign || "").trim();
    const model = String(body.model || "").trim();
    const backgroundDataMb = Number(body.backgroundDataMb ?? 0);
    const lastSync = String(body.lastSync || "-").trim();
    const unitCode = String(body.unitCode || "DISKOMLEKAU-OPS").trim();
    const osVersion = String(body.osVersion || "Android Hardened").trim();
    const rawLog = String(body.rawLog || "").trim();

    if (!callsign || !rawLog) {
      return res.status(400).json({
        error: "Parameter 'callsign' dan 'rawLog' wajib diisi untuk analisis forensik."
      });
    }

    const ai = new GoogleGenAI({ apiKey });

    const prompt = `
Anda adalah Analis Forensik Keamanan Siber Mobile (MOSFAD) untuk Diskomlekau.
Analisislah telemetri dan log mentah perangkat taktis berikut secara objektif, tegas, dan akurat dalam Bahasa Indonesia.

DATA PERANGKAT:
- Callsign: ${callsign}
- Model: ${model || "Perangkat Taktis"}
- Unit: ${unitCode}
- OS: ${osVersion}
- Last Sync: ${lastSync}
- Background Data: ${Number.isFinite(backgroundDataMb) ? backgroundDataMb : 0} MB

LOG TELEMETRI MENTAH:
${rawLog.slice(0, 8000)}

ATURAN ANALISIS:
1. Deteksi indikasi penyadapan, RAT, backdoor, socket outbound mencurigakan, atau proses tanpa tanda tangan valid.
2. Evaluasi anomali trafik latar belakang serta potensi penyalahgunaan izin SMS/OTP atau notifikasi sensitif.
3. Pastikan filter privasi tetap mengabaikan isi pesan pribadi personel dan hanya mengaudit metadata keamanan.
4. Berikan rekomendasi tindakan konkret bagi Tim Respon Insiden (IRT) Diskomlekau.
5. Klasifikasikan riskLevel secara ketat sebagai SECURE, WATCH, atau CRITICAL sesuai bukti pada log.
    `.trim();

    const result = await ai.models.generateContent({
      model: "gemini-2.5-flash",
      contents: prompt,
      config: {
        responseMimeType: "application/json",
        responseSchema: FORENSIC_RESPONSE_SCHEMA,
        temperature: 0.2
      }
    });

    const parsed = JSON.parse(result.text || "{}");
    return res.status(200).json({
      callsign,
      model: model || "Perangkat Taktis",
      analyzedAt: new Date().toISOString(),
      aiModel: "gemini-2.5-flash",
      ...parsed
    });
  } catch (err) {
    console.error("MOSFAD /api/analyze error:", err);
    return res.status(500).json({
      error: "Gagal menjalankan analisis forensik Gemini di server.",
      detail: err?.message || "Unknown server error"
    });
  }
}
