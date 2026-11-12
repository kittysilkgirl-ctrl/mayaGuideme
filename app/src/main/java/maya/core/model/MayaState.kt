package maya.core.model

data class GpsCoordinate(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampMs: Long
)

data class MayaTelemetryState(
    val isServiceActive: Boolean = false,
    val lastCoordinate: GpsCoordinate? = null,
    val landmarkMatchId: String? = null,
    val statusMessage: String = "Idle"
)
