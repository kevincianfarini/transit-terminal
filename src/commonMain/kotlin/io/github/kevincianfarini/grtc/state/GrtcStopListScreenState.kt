package io.github.kevincianfarini.grtc.state

public data class GrtcStopListScreenState(
    val currentTime: String,
    val stops: List<GrtcStopState>,
    val alerts: GrtcServiceAlertsState,
)