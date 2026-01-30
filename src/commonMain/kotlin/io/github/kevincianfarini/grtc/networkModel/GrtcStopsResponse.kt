package io.github.kevincianfarini.grtc.networkModel

import kotlinx.serialization.Serializable

@Serializable
public data class GrtcStopsResponse(val stops: List<GrtcStop>)