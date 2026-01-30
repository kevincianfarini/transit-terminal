package io.github.kevincianfarini.grtc.repository

import io.github.kevincianfarini.grtc.networkModel.GrtcErrorResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcPredictionResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcRoute
import io.github.kevincianfarini.grtc.networkModel.GrtcRouteDirection
import io.github.kevincianfarini.grtc.networkModel.GrtcRouteDirectionsResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcRoutesResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcStopsResponse
import io.github.kevincianfarini.grtc.networkModel.Response
import kotlin.time.Instant

public interface GrtcStopRepository {

    public suspend fun getRoutes(now: Instant): Response<GrtcRoutesResponse, GrtcErrorResponse>

    public suspend fun getRouteDirections(
        now: Instant,
        route: GrtcRoute
    ): Response<GrtcRouteDirectionsResponse, GrtcErrorResponse>

    public suspend fun getBusStops(
        now: Instant,
        route: GrtcRoute,
        direction: GrtcRouteDirection
    ): Response<GrtcStopsResponse, GrtcErrorResponse>

    public suspend fun getBusStopSchedulePredictions(
        stopNumber: String,
        now: Instant
    ): Response<GrtcPredictionResponse, GrtcErrorResponse>
}