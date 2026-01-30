package io.github.kevincianfarini.grtc.networkModel

import kotlinx.serialization.Serializable

@Serializable
public data class GrtcRoutesResponse(val routes: List<GrtcRoute>)