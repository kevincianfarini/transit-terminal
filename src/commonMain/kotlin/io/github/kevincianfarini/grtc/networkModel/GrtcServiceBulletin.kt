package io.github.kevincianfarini.grtc.networkModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
public data class GrtcServiceBulletin(
    @SerialName("dtl") val detail: String,
    @SerialName("mod") @Serializable(with = GrtcTimestampDeserializer::class)
    val posted: Instant,
)