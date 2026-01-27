package io.github.kevincianfarini.grtc.networkModel

import kotlinx.datetime.LocalDateTime

public data class GrtcResponse(val predictions: List<GrtcStopPrediction>)

public data class GrtcStopPrediction(
    val stopNumber: Int,
    val stopName: String,
    val predictedArrivalTime: LocalDateTime,
)