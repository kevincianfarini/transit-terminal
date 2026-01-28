package io.github.kevincianfarini.grtc.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jakewharton.mosaic.text.AnnotatedString
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.Color
import io.github.kevincianfarini.cardiologist.schedulePulse
import io.github.kevincianfarini.grtc.extension.ZonedClock
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
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
public class GrtcStopListPresenter(
    private val stops: List<Int>,
    private val clock: ZonedClock,
    private val repository: GrtcStopRepository,
) {

    @Composable
    public fun present(): GrtcStopListScreenState {
        val now = produceNowState()
        val grtcResponses = stops.map { produceGrtcResponseState(it) }
        return GrtcStopListScreenState(
            stops = grtcResponses.map { it.mapToGrtcStopState(now, clock.timeZone()) },
            currentTime = now.toLocalDateTime(clock.timeZone()).format(NOW_DATE_TIME_FORMAT),
        )
    }

    @Composable
    private fun produceNowState(): Instant {
        var now by remember { mutableStateOf(clock.now()) }
        LaunchedEffect(clock) {
            clock.schedulePulse(clock.timeZone()).beat { occurred ->
                now = occurred
            }
        }
        return now
    }

    @Composable
    private fun produceGrtcResponseState(stopNumber: Int): Response<GrtcResponse, Nothing>? {
        var response by remember(stopNumber) { mutableStateOf<Response<GrtcResponse, Nothing>?>(null) }
        LaunchedEffect(Unit) {
            response = repository.getBusStopSchedulePredictions(stopNumber, clock.now())
        }
        LaunchedEffect(clock) {
            clock.schedulePulse(clock.timeZone()) { atSeconds(0, 30) }.beat {
                response = repository.getBusStopSchedulePredictions(stopNumber, clock.now())
            }
        }
        return response
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

private fun Response<GrtcResponse, Nothing>?.mapToGrtcStopState(now: Instant, timeZone: TimeZone): GrtcStopState {
    return when (this) {
        null -> GrtcStopState.Loading
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

private val Duration.inFormattedMinutes: AnnotatedString get() {
    return buildAnnotatedString {
        when  {
            inWholeMinutes <= 0 -> withStyle(SpanStyle(color = Color(255, 102, 102))) {
                append("DUE")
            }
            inWholeMinutes <= 5 -> withStyle(SpanStyle(color = Color(255, 178, 102))) {
                append(inWholeMinutes.toString())
                append(" MIN")
            }
            inWholeMinutes <= 10 -> withStyle(SpanStyle(color = Color(255, 255, 102))) {
                append(inWholeMinutes.toString())
                append(" MIN")
            }
            else -> withStyle(SpanStyle(color = Color.White)) {
                append(inWholeMinutes.toString())
                append(" MIN")
            }
        }
    }
}