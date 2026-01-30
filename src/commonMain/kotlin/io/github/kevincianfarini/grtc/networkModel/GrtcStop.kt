package io.github.kevincianfarini.grtc.networkModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GrtcStop(
    @SerialName("stpid") val stopId: String,
    @SerialName("stpnm") val stopName: String
)