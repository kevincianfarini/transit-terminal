package io.github.kevincianfarini

import io.github.kevincianfarini.grtc.repository.KtorGrtcStopRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class SomeTest {

    @Test
    fun test() = runTest {
        KtorGrtcStopRepository().getBusStopSchedulePredictions(
            stopNumber = 3523,
            now = Clock.System.now()
        )
    }
}