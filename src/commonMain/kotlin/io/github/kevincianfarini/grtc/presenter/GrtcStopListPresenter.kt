package io.github.kevincianfarini.grtc.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.kevincianfarini.grtc.extension.PulseEffect
import io.github.kevincianfarini.grtc.extension.ZonedClock
import io.github.kevincianfarini.grtc.extension.nowLocal
import io.github.kevincianfarini.grtc.networkModel.GrtcResponse
import io.github.kevincianfarini.grtc.networkModel.Response
import io.github.kevincianfarini.grtc.repository.GrtcStopRepository
import io.github.kevincianfarini.grtc.state.GrtcStopArrival
import io.github.kevincianfarini.grtc.state.GrtcStopListScreenState
import io.github.kevincianfarini.grtc.state.GrtcStopState
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
public class GrtcStopListPresenter(
    private val stops: List<Int>,
    private val clock: ZonedClock,
    private val repository: GrtcStopRepository,
) {

    @Composable
    public fun present(): GrtcStopListScreenState {
        var localNow by remember { mutableStateOf(clock.nowLocal()) }
        clock.PulseEffect { localOccurrance -> localNow = localOccurrance }
        return GrtcStopListScreenState(
            stops = stops.map { presentTransitStop(it) },
            currentTime = localNow.format(DATE_TIME_FORMAT)
        )
    }

    @Composable
    private fun presentTransitStop(stopNumber: Int): GrtcStopState {
        var transitStop: GrtcStopState by remember(stopNumber) { mutableStateOf(GrtcStopState.Loading) }
        LaunchedEffect(Unit) {
            transitStop = GrtcStopState.Loading
            transitStop = repository.getBusStopSchedulePredictions(stopNumber).mapToGrtcStopState()
        }
        clock.PulseEffect(atSecond = 0) {
            transitStop = GrtcStopState.Loading
            transitStop = repository.getBusStopSchedulePredictions(stopNumber).mapToGrtcStopState()
        }
        return transitStop
    }
}

private val TIME_FORMAT = LocalTime.Format {
    hour()
    char(':')
    minute()
    char(':')
    second()
}

private val DATE_TIME_FORMAT = LocalDateTime.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
    char(' ')
    time(TIME_FORMAT)
}

private fun Response<GrtcResponse, Nothing>.mapToGrtcStopState(): GrtcStopState {
    return when (this) {
        is Response.Success -> GrtcStopState.Loaded(
            stopNumber = data.predictions.first().stopNumber,
            stopName = data.predictions.first().stopName,
            predictedArrivals = data.predictions.map { prediction ->
                GrtcStopArrival(
                    arrivalTime = prediction.predictedArrivalTime.time.format(TIME_FORMAT),
                    durationUntilArrival = "5 MIN",
                )
            }
        )
        is Response.Failure.HttpFailure -> GrtcStopState.Error("FAILED: HTTP $statusCode")
        else -> GrtcStopState.Error("FAILED")
    }
}