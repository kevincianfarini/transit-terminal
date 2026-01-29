package io.github.kevincianfarini.grtc.state

import com.jakewharton.mosaic.text.AnnotatedString

public sealed interface GrtcStopState {
    public data object Loading : GrtcStopState
    public data class Loaded(
        val stopName: String,
        val predictedArrivals: List<GrtcStopArrival>,
    ) : GrtcStopState
    public data class Error(val message: String) : GrtcStopState
}

public data class GrtcStopArrival(
    val arrivalTime: AnnotatedString,
    val vehicleId: String,
    val routeInfo: String,
)