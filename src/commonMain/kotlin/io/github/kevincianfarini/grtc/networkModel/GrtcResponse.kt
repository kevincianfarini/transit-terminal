package io.github.kevincianfarini.grtc.networkModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GrtcResponse<T : Any>(
    @SerialName("bustime-response") val response: T
)