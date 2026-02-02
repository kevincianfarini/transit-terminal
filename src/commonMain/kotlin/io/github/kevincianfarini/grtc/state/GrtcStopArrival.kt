package io.github.kevincianfarini.grtc.state

import com.jakewharton.mosaic.text.AnnotatedString

public data class GrtcStopArrival(
    val arrivalTime: AnnotatedString,
    val vehicleStatus: AnnotatedString,
    val routeInfo: AnnotatedString,
)
