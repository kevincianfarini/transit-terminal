package io.github.kevincianfarini.grtc.networkModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
public data class GrtcStopPrediction(
    @SerialName("prdtm") @Serializable(with = GrtcTimestampDeserializer::class)
    val predictedArrivalTime: Instant,
    @SerialName("des") val destination: String,
    @SerialName("rtdir") val direction: String,
    @SerialName("vid") val vehicleId: String,
    @SerialName("dly") val delayed: Boolean,
)
