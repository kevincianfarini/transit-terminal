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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

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
            currentTime = localNow.format(NOW_DATE_TIME_FORMAT)
        )
    }

    @Composable
    private fun presentTransitStop(stopNumber: Int): GrtcStopState {
        var transitStop: GrtcStopState by remember(stopNumber) { mutableStateOf(GrtcStopState.Loading) }
        LaunchedEffect(Unit) {
            transitStop = GrtcStopState.Loading
            transitStop = repository.getBusStopSchedulePredictions(stopNumber, clock.now())
                .mapToGrtcStopState(clock.now(), clock.timeZone())
        }
        clock.PulseEffect(atSecond = 0) {
            transitStop = repository.getBusStopSchedulePredictions(stopNumber, clock.now())
                .mapToGrtcStopState(clock.now(), clock.timeZone())
        }
        return transitStop
    }
}

private val STOP_ARRIVAL_TIME_FORMAT = LocalTime.Format {
    hour()
    char(':')
    minute()
}

private val NOW_DATE_TIME_FORMAT = LocalDateTime.Format {
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

private fun Response<GrtcResponse, Nothing>.mapToGrtcStopState(now: Instant, timeZone: TimeZone): GrtcStopState {
    return when (this) {
        is Response.Success -> GrtcStopState.Loaded(
            stopNumber = data.predictions.first().stopNumber,
            stopName = data.predictions.first().stopName,
            predictedArrivals = data.predictions.map { prediction ->
                GrtcStopArrival(
                    arrivalTime = prediction.predictedArrivalTime
                        .toLocalDateTime(timeZone)
                        .time
                        .format(STOP_ARRIVAL_TIME_FORMAT),
                    durationUntilArrival = (prediction.predictedArrivalTime - now).inFormattedMinutes,
                )
            }
        )
        is Response.Failure.HttpFailure -> GrtcStopState.Error("FAILED: HTTP $statusCode")
        is Response.Failure.NetworkError -> GrtcStopState.Error("FAILED: ${e.message}")
        is Response.Failure.DeserializationError -> GrtcStopState.Error("FAILED: ${e.message}")
        else -> GrtcStopState.Error("FAILED")
    }
}

private val Duration.inFormattedMinutes: String get() {
    return when  {
        inWholeMinutes > 0 -> "$inWholeMinutes MIN"
        else -> "DUE"
    }
}