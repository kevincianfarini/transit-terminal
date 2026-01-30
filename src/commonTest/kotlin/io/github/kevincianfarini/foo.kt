package io.github.kevincianfarini

import io.github.kevincianfarini.grtc.networkModel.GrtcRoute
import io.github.kevincianfarini.grtc.networkModel.GrtcRouteDirection
import io.github.kevincianfarini.grtc.networkModel.GrtcRouteDirectionsResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcRoutesResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcStopsResponse
import io.github.kevincianfarini.grtc.networkModel.Response
import io.github.kevincianfarini.grtc.repository.KtorGrtcStopRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Clock

class SomeTest {

    @Test
    fun test() = runTest {
        val repository = KtorGrtcStopRepository()
        val now = Clock.System.now()
        repository.getBusStops(now, GrtcRoute("BRT"), GrtcRouteDirection("West Bound"))
    }
}