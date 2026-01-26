package io.github.kevincianfarini.grtc.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.kevincianfarini.grtc.extension.PulseEffect
import io.github.kevincianfarini.grtc.extension.ZonedClock
import io.github.kevincianfarini.grtc.extension.localNow
import io.github.kevincianfarini.grtc.state.GrtcStopListState
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
public class GrtcStopListPresenter(
    private val stops: List<Int>,
    private val clock: ZonedClock,
) {

    @Composable
    public fun present(): GrtcStopListState {
        var currentSecond by remember { mutableIntStateOf(clock.localNow().second) }
        clock.PulseEffect(clock.timeZone()) { localOccurrance ->
            currentSecond = localOccurrance.second
        }
        return GrtcStopListState(stops, currentSecond)
    }
}