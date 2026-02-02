package io.github.kevincianfarini.grtc.networkModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GrtcServiceBulletinResponse(
    @SerialName("sb") val serviceBulletins: List<GrtcServiceBulletin>
)