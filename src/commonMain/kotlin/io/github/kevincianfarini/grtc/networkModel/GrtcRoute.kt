package io.github.kevincianfarini.grtc.networkModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GrtcRoute(@SerialName("rt") val route: String)