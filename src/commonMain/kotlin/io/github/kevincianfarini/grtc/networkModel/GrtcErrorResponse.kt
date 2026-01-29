package io.github.kevincianfarini.grtc.networkModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GrtcErrorResponse(
    val error: List<BusTimeResponseError>
)

@Serializable
public data class BusTimeResponseError(
    @SerialName("msg") val message: String
)
