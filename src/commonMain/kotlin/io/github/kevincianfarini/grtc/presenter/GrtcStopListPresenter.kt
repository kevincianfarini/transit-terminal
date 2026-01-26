package io.github.kevincianfarini.grtc.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.kevincianfarini.grtc.extension.PulseEffect
import io.github.kevincianfarini.grtc.extension.ZonedClock
import io.github.kevincianfarini.grtc.extension.nowLocal
import io.github.kevincianfarini.grtc.state.GrtcStopListScreenState
import io.github.kevincianfarini.grtc.state.GrtcStopState
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
public class GrtcStopListPresenter(
    private val stops: List<Int>,
    private val clock: ZonedClock,
) {

    @Composable
    public fun present(): GrtcStopListScreenState {
        var localNow by remember { mutableStateOf(clock.nowLocal()) }
        clock.PulseEffect { localOccurrance -> localNow = localOccurrance }
        return GrtcStopListScreenState(
            stops = stops.map { presentTransitStop(it) },
            currentTime = localNow.format(TIME_FORMAT)
        )
    }

    @Composable
    private fun presentTransitStop(stopNumber: Int): GrtcStopState {
        val transitStop by remember(stopNumber) { mutableStateOf(GrtcStopState.Loading) }
        return transitStop
    }
}

private val TIME_FORMAT = LocalDateTime.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
    char(' ')
    hour()
    char(':')
    minute()
    char(':')
    second()
}