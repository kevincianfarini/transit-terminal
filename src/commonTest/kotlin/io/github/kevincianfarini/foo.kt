package io.github.kevincianfarini

import io.github.kevincianfarini.grtc.repository.KtorGrtcStopRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Clock

class SomeTest {

    @Test
    fun test() = runTest {
        val repository = KtorGrtcStopRepository(Clock.System)
        repository.getServeBulletins()
    }
}