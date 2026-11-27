package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ForensicAnalysisUiState
import com.example.model.TacticalLinkMode
import com.example.ui.MosfadViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches MOSFAD Diskomlekau`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MOSFAD Diskomlekau", appName)
    }

    @Test
    fun `initial state has zero simulated alerts and supports real backend validation`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MosfadViewModel(app)

        val initial = viewModel.uiState.value
        assertEquals(7, initial.totalDevicesCount)
        assertEquals(0, initial.alerts.size)
        assertEquals(TacticalLinkMode.ACTIVE, initial.tacticalLinkMode)

        // Toggle Tactical Link to Offline Cache
        viewModel.toggleTacticalLink()
        assertEquals(TacticalLinkMode.OFFLINE_CACHE, viewModel.uiState.value.tacticalLinkMode)

        // Attempting backend analysis while offline yields explicit error state
        viewModel.analyzeSelectedDeviceWithBackend("proc=test")
        assertTrue(viewModel.uiState.value.forensicState is ForensicAnalysisUiState.Error)

        // Enroll a new client adds device and operational enrollment log
        viewModel.enrollNewClient("GARUDA-99", "Rugged X1", "DISKOMLEKAU-OPS")
        assertEquals(8, viewModel.uiState.value.totalDevicesCount)
        assertEquals("GARUDA-99", viewModel.uiState.value.selectedDevice?.callsign)
        assertEquals(1, viewModel.uiState.value.alerts.size)

        // Select critical device VIPER-03 and isolate it via IRT
        viewModel.selectDevice("dev-viper-03")
        viewModel.toggleIsolateSelectedDevice()
        assertEquals(0, viewModel.uiState.value.criticalThreatsCount)
        assertTrue(viewModel.uiState.value.selectedDevice?.isIsolated == true)
    }
}
