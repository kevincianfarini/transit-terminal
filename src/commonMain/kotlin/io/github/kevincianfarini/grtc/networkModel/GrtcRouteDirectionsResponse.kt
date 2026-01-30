package io.github.kevincianfarini.grtc.networkModel

import kotlinx.serialization.Serializable

@Serializable
public data class GrtcRouteDirectionsResponse(val directions: List<GrtcRouteDirection>)