package maya.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import maya.core.model.MayaTelemetryState

class MayaEdgeService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        startEdgeForeground()
        _telemetryState.value = _telemetryState.value.copy(
            isServiceActive = true,
            statusMessage = "Edge Telemetry Online"
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        _telemetryState.value = _telemetryState.value.copy(
            isServiceActive = false,
            statusMessage = "Offline"
        )
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startEdgeForeground() {
        val channelId = "maya_edge_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Maya Edge Telemetry",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("MayaGuideMe")
                .setContentText("Edge telemetry active")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("MayaGuideMe")
                .setContentText("Edge telemetry active")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build()
        }

        startForeground(1001, notification)
    }

    companion object {
        private val _telemetryState = MutableStateFlow(MayaTelemetryState())
        val telemetryState: StateFlow<MayaTelemetryState> = _telemetryState.asStateFlow()
    }
}
