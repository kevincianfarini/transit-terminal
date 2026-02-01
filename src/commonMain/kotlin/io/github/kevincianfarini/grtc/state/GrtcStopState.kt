package io.github.kevincianfarini.grtc.state

import com.jakewharton.mosaic.text.AnnotatedString

public data class GrtcStopState(
    val stopName: LoadingState<AnnotatedString, AnnotatedString>,
    val predictedArrivals: LoadingState<List<GrtcStopArrival>, AnnotatedString>,
)

public data class GrtcStopArrival(
    val arrivalTime: AnnotatedString,
    val vehicleStatus: AnnotatedString,
    val routeInfo: AnnotatedString,
)