const STORAGE_KEY = "mosfad_diskomlekau_state_v2";

const DEFAULT_DEVICES = [
  {
    id: "dev-ghost-01",
    callsign: "GHOST-01",
    model: "Rugged Tab S9",
    lastSync: "20/15:29 ARD",
    backgroundDataMb: 42,
    riskLevel: "SECURE",
    unitCode: "SAT-SIBER-01",
    osVersion: "Android 15 Hardened",
    isIsolated: false,
    defaultLog: "SELinux=Enforcing; VPN=Diskomlekau-IPSec UP; bg_upload=42MB; sms_otp_perm=NONE; cert_chain=VALID"
  },
  {
    id: "dev-viper-03",
    callsign: "VIPER-03",
    model: "XCover7 Tactical",
    lastSync: "20/16:29 ARD",
    backgroundDataMb: 894,
    riskLevel: "CRITICAL",
    unitCode: "KOOPSUD-03",
    osVersion: "Android 14 Rugged",
    isIsolated: false,
    defaultLog: "proc=com.sys.tactical.bridge uid=10294 outbound_socket=185.220.101.4:8443; bg_upload=894MB; perm_req=READ_SMS,RECEIVE_SMS(OTP); sig=UNVERIFIED"
  },
  {
    id: "dev-shadow-09",
    callsign: "SHADOW-09",
    model: "Sonim XP10",
    lastSync: "20/15:29 ARD",
    backgroundDataMb: 318,
    riskLevel: "WATCH",
    unitCode: "DISK-OPS-09",
    osVersion: "Android 14 Mil",
    isIsolated: false,
    defaultLog: "proc=svc.telemetri.relay udp_burst=318MB port=51822; cert_pinning=MISMATCH; sms_otp=NONE"
  },
  {
    id: "dev-elang-04",
    callsign: "ELANG-04",
    model: "Pixel 9 Pro Mil",
    lastSync: "20/15:41 ARD",
    backgroundDataMb: 245,
    riskLevel: "WATCH",
    unitCode: "LANUD-HLM-04",
    osVersion: "Android 15 Graphene",
    isIsolated: false,
    defaultLog: "proc=audiodaemon_ext bg_data=245MB; notif_listener=OTP_INTERCEPT_ATTEMPT off_hours=02:14"
  },
  {
    id: "dev-rajawali-02",
    callsign: "RAJAWALI-02",
    model: "CAT S75 Sat",
    lastSync: "20/15:18 ARD",
    backgroundDataMb: 64,
    riskLevel: "SECURE",
    unitCode: "SATKOM-02",
    osVersion: "Android 14 Sat-Link",
    isIsolated: false,
    defaultLog: "ntn_sat_link=ENCRYPTED_AES256; bg_data=64MB; integrity=VERIFIED"
  },
  {
    id: "dev-garuda-07",
    callsign: "GARUDA-07",
    model: "Toughbook FZ-N1",
    lastSync: "20/14:55 ARD",
    backgroundDataMb: 51,
    riskLevel: "SECURE",
    unitCode: "DISKOM-HQ-07",
    osVersion: "Android 14 Rugged",
    isIsolated: false,
    defaultLog: "bootloader=LOCKED; selinux=ENFORCING; bg_data=51MB; zero_touch=ACTIVE"
  },
  {
    id: "dev-cakra-05",
    callsign: "CAKRA-05",
    model: "Armor 24 Tactical",
    lastSync: "20/15:10 ARD",
    backgroundDataMb: 88,
    riskLevel: "SECURE",
    unitCode: "RADAR-05",
    osVersion: "Android 14 Hardened",
    isIsolated: false,
    defaultLog: "vpn_diskomlekau=STABLE; bg_data=88MB; anomalies=0"
  }
];

function loadState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch (_) {}
  return {
    offlineCache: !navigator.onLine,
    devices: DEFAULT_DEVICES,
    alerts: [],
    reportsByDevice: {},
    selectedDeviceId: DEFAULT_DEVICES[1].id,
    settings: { enrollment: true, allowMgmt: true, autoJoin: true }
  };
}

let state = loadState();
let deferredInstallPrompt = null;

function saveState() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch (_) {}
}

function clockNow() {
  return new Date().toTimeString().slice(0, 8);
}

function getSelectedDevice() {
  return state.devices.find((d) => d.id === state.selectedDeviceId) || state.devices[0];
}

function renderKpis() {
  const total = state.devices.length;
  const watch = state.devices.filter((d) => d.riskLevel === "WATCH").length;
  const critical = state.devices.filter((d) => d.riskLevel === "CRITICAL" && !d.isIsolated).length;

  document.getElementById("kpiTotal").textContent = String(total);
  document.getElementById("kpiWatch").textContent = String(watch);
  document.getElementById("kpiCritical").textContent = String(critical);
  document.getElementById("kpiStatus").textContent = critical > 1 ? "SIAGA" : "SECURE";
  document.getElementById("kpiStatusSub").textContent = state.offlineCache ? "OFFLINE CACHE" : "OPERATIONAL";
  document.getElementById("kpiCriticalSub").textContent = critical > 0 ? "IMMEDIATE ACTION REQUIRED" : "CONTAINED";
}

function renderLinkButton() {
  const btn = document.getElementById("tacticalLinkBtn");
  const label = document.getElementById("tacticalLinkLabel");
  btn.className = `link-pill ${state.offlineCache ? "offline" : "active"}`;
  label.textContent = state.offlineCache ? "TACTICAL LINK: OFFLINE CACHE" : "TACTICAL LINK: ACTIVE";
}

function renderDevices() {
  const tbody = document.getElementById("deviceTableBody");
  document.getElementById("deviceCountBadge").textContent = `${state.devices.length} UNIT`;
  tbody.innerHTML = "";

  state.devices.forEach((dev) => {
    const tr = document.createElement("tr");
    if (dev.id === state.selectedDeviceId) tr.classList.add("selected");
    tr.innerHTML = `
      <td><strong>${dev.callsign}</strong></td>
      <td>${dev.model}</td>
      <td>${dev.lastSync}</td>
      <td>${dev.backgroundDataMb}</td>
      <td><span class="risk-badge risk-${dev.riskLevel}">${dev.riskLevel}</span></td>
    `;
    tr.addEventListener("click", () => {
      state.selectedDeviceId = dev.id;
      saveState();
      renderDevices();
      renderSelectedReport();
    });
    tbody.appendChild(tr);
  });
}

function renderSelectedReport() {
  const dev = getSelectedDevice();
  if (!dev) return;

  document.getElementById("reportCallsign").textContent = `${dev.callsign} • ${dev.model}`;
  document.getElementById("reportMeta").textContent = `UNIT: ${dev.unitCode} | OS: ${dev.osVersion}`;
  document.getElementById("rawLogInput").value = dev.lastAnalyzedLog || dev.defaultLog || "";

  const isolateBtn = document.getElementById("isolateBtn");
  if (dev.isIsolated) {
    isolateBtn.textContent = "PULIHKAN KONEKSI PERANGKAT";
    isolateBtn.className = "btn-tactical green";
  } else {
    isolateBtn.textContent = "ISOLASI PERANGKAT / KIRIM IRT";
    isolateBtn.className = "btn-tactical red";
  }

  const cached = state.reportsByDevice[dev.id];
  if (cached) {
    document.getElementById("reportThreatScore").textContent = `SKOR ANCAMAN: ${cached.threatScore}/100`;
    document.getElementById("repWiretap").textContent = cached.wiretapBackdoorStatus;
    document.getElementById("repTraffic").textContent = cached.trafficOtpAnomalyStatus;
    document.getElementById("repPrivacy").textContent = cached.privacyFilterStatus;
    document.getElementById("repIrt").textContent = cached.irtRecommendation;
  } else {
    document.getElementById("reportThreatScore").textContent = "SKOR: BELUM DIANALISIS";
    document.getElementById("repWiretap").textContent = "Klik ANALISIS LOG VIA /api/analyze untuk analisis forensik Gemini real-time.";
    document.getElementById("repTraffic").textContent = "-";
    document.getElementById("repPrivacy").textContent = "-";
    document.getElementById("repIrt").textContent = "-";
  }
}

function renderAlerts() {
  const container = document.getElementById("alertFeedList");
  document.getElementById("alertCountBadge").textContent = `${state.alerts.length} LOG`;
  container.innerHTML = "";

  if (state.alerts.length === 0) {
    container.innerHTML = `<p class="muted">Belum ada alert tercatat. Jalankan analisis <code>/api/analyze</code> atau tindakan IRT.</p>`;
    return;
  }

  state.alerts.forEach((item) => {
    const div = document.createElement("div");
    div.className = `alert-card ${item.severity}`;
    div.innerHTML = `
      <div class="alert-time">${item.timestamp}</div>
      <div class="alert-title">${item.title}</div>
      <div class="alert-tag">${item.callsign} // ${item.severity}</div>
      <div class="alert-detail">${item.detail}</div>
    `;
    div.addEventListener("click", () => {
      const target = state.devices.find((d) => d.callsign === item.callsign);
      if (target) {
        state.selectedDeviceId = target.id;
        saveState();
        renderDevices();
        renderSelectedReport();
      }
    });
    container.appendChild(div);
  });
}

function showStatus(msg, isError = false) {
  const banner = document.getElementById("forensicStatusMsg");
  banner.textContent = msg;
  banner.className = `status-banner ${isError ? "error" : ""}`;
}

async function runBackendAnalysis() {
  const dev = getSelectedDevice();
  if (!dev) return;
  const rawLog = document.getElementById("rawLogInput").value.trim();
  if (!rawLog) {
    showStatus("Masukkan log telemetri perangkat sebelum menganalisis.", true);
    return;
  }
  if (state.offlineCache) {
    showStatus("Mode Offline Cache aktif. Aktifkan Tactical Link untuk memanggil /api/analyze.", true);
    return;
  }

  const btn = document.getElementById("analyzeBackendBtn");
  btn.disabled = true;
  showStatus(`Mengirim telemetri ${dev.callsign} ke /api/analyze (gemini-2.5-flash)...`);

  try {
    const res = await fetch("/api/analyze", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        callsign: dev.callsign,
        model: dev.model,
        backgroundDataMb: dev.backgroundDataMb,
        lastSync: dev.lastSync,
        unitCode: dev.unitCode,
        osVersion: dev.osVersion,
        rawLog
      })
    });
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.error || `HTTP ${res.status}`);
    }

    dev.lastAnalyzedLog = rawLog;
    if (!dev.isIsolated && data.riskLevel) {
      dev.riskLevel = data.riskLevel;
    }
    state.reportsByDevice[dev.id] = data;

    if (data.riskLevel === "CRITICAL" || data.riskLevel === "WATCH") {
      state.alerts.unshift({
        id: `alt-${Date.now()}`,
        timestamp: clockNow(),
        title: data.alertTitle || "FORENSIC ALERT",
        callsign: dev.callsign,
        severity: data.riskLevel,
        detail: data.alertDetail || data.irtRecommendation
      });
    }

    saveState();
    renderKpis();
    renderDevices();
    renderSelectedReport();
    renderAlerts();
    showStatus(`Analisis Gemini (${data.aiModel}) untuk ${dev.callsign} selesai.`);
  } catch (err) {
    showStatus(`Gagal memanggil /api/analyze: ${err.message}`, true);
  } finally {
    btn.disabled = false;
  }
}

function downloadMdmProfile(format) {
  const isJson = format === "json";
  const content = isJson
    ? JSON.stringify(
        {
          profileName: "MOSFAD-DISKOMLEKAU-MDM",
          zeroTouch: state.settings,
          generatedAt: new Date().toISOString()
        },
        null,
        2
      )
    : `<?xml version="1.0" encoding="UTF-8"?><plist version="1.0"><dict><key>PayloadDisplayName</key><string>MOSFAD Diskomlekau MDM</string><key>ZeroTouchEnrollment</key><${state.settings.enrollment}/></dict></plist>`;

  const blob = new Blob([content], { type: isJson ? "application/json" : "application/x-apple-aspen-config" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `mosfad-diskomlekau-mdm.${format}`;
  a.click();
  URL.revokeObjectURL(url);
}

function drawTacticalQr() {
  const canvas = document.getElementById("qrCanvas");
  const ctx = canvas.getContext("2d");
  const matrix = [
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
  ];
  const cell = canvas.width / matrix.length;
  ctx.fillStyle = "#f3f6f8";
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  ctx.fillStyle = "#0d1418";
  matrix.forEach((row, r) => {
    [...row].forEach((bit, c) => {
      if (bit === "1") ctx.fillRect(c * cell, r * cell, Math.ceil(cell), Math.ceil(cell));
    });
  });
}

document.getElementById("tacticalLinkBtn").addEventListener("click", () => {
  state.offlineCache = !state.offlineCache;
  saveState();
  renderLinkButton();
  renderKpis();
});

document.getElementById("analyzeBackendBtn").addEventListener("click", runBackendAnalysis);

document.getElementById("isolateBtn").addEventListener("click", () => {
  const dev = getSelectedDevice();
  if (!dev) return;
  dev.isIsolated = !dev.isIsolated;
  dev.riskLevel = dev.isIsolated ? "ISOLATED" : "SECURE";
  state.alerts.unshift({
    id: `alt-${Date.now()}`,
    timestamp: clockNow(),
    title: dev.isIsolated ? "IRT ISOLATION SENT" : "DEVICE RESTORED",
    callsign: dev.callsign,
    severity: "OPERATIONAL",
    detail: dev.isIsolated
      ? "Tim IRT Diskomlekau dikirim dan perangkat diisolasi."
      : "Karantina perangkat dicabut setelah verifikasi."
  });
  saveState();
  renderKpis();
  renderDevices();
  renderSelectedReport();
  renderAlerts();
});

document.getElementById("downloadJsonBtn").addEventListener("click", () => downloadMdmProfile("json"));
document.getElementById("downloadMobileConfigBtn").addEventListener("click", () => downloadMdmProfile("mobileconfig"));

["toggleEnrollment", "toggleMgmt", "toggleAutoJoin"].forEach((id) => {
  const el = document.getElementById(id);
  el.addEventListener("change", () => {
    state.settings = {
      enrollment: document.getElementById("toggleEnrollment").checked,
      allowMgmt: document.getElementById("toggleMgmt").checked,
      autoJoin: document.getElementById("toggleAutoJoin").checked
    };
    saveState();
  });
});

const enrollDialog = document.getElementById("enrollDialog");
document.getElementById("openEnrollBtn").addEventListener("click", () => enrollDialog.showModal());
document.getElementById("cancelEnrollBtn").addEventListener("click", () => enrollDialog.close());
document.getElementById("enrollForm").addEventListener("submit", (e) => {
  e.preventDefault();
  const callsign = document.getElementById("newCallsign").value.trim().toUpperCase();
  const model = document.getElementById("newModel").value.trim();
  const unitCode = document.getElementById("newUnit").value.trim() || "DISKOMLEKAU-OPS";
  if (!callsign || !model) return;

  const newDev = {
    id: `dev-${Date.now()}`,
    callsign,
    model,
    lastSync: "BARU SAJA",
    backgroundDataMb: 0,
    riskLevel: "SECURE",
    unitCode,
    osVersion: "Android 15 Hardened",
    isIsolated: false,
    defaultLog: `enrollment=ZERO_TOUCH unit=${unitCode} selinux=ENFORCING`
  };
  state.devices.unshift(newDev);
  state.selectedDeviceId = newDev.id;
  state.alerts.unshift({
    id: `alt-${Date.now()}`,
    timestamp: clockNow(),
    title: "CLIENT ENROLLED",
    callsign,
    severity: "OPERATIONAL",
    detail: `Perangkat ${model} berhasil didaftarkan via Zero-Touch.`
  });
  saveState();
  renderKpis();
  renderDevices();
  renderSelectedReport();
  renderAlerts();
  enrollDialog.close();
});

window.addEventListener("beforeinstallprompt", (e) => {
  e.preventDefault();
  deferredInstallPrompt = e;
  document.getElementById("installPwaBtn").classList.remove("hidden");
});

document.getElementById("installPwaBtn").addEventListener("click", async () => {
  if (!deferredInstallPrompt) return;
  deferredInstallPrompt.prompt();
  await deferredInstallPrompt.userChoice;
  deferredInstallPrompt = null;
  document.getElementById("installPwaBtn").classList.add("hidden");
});

document.getElementById("toggleEnrollment").checked = state.settings.enrollment;
document.getElementById("toggleMgmt").checked = state.settings.allowMgmt;
document.getElementById("toggleAutoJoin").checked = state.settings.autoJoin;

drawTacticalQr();
renderLinkButton();
renderKpis();
renderDevices();
renderSelectedReport();
renderAlerts();
