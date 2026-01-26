package io.github.kevincianfarini.grtc.state

public data class GrtcStopListScreenState(
    val stops: List<GrtcStopState>,
    val currentTime: String,
)