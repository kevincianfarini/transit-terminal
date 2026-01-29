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
import io.github.kevincianfarini.grtc.networkModel.GrtcErrorResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcPredictionResponse
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
import kotlin.time.Duration.Companion.minutes
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
        val grtcResponses = stops.map { stopNumber ->
            val response = produceGrtcResponseState(stopNumber)
            Pair(produceGrtcStopName(response), response)
        }
        return GrtcStopListScreenState(
            stops = grtcResponses.map { (stopName, response) ->
                response.mapToGrtcStopState(stopName, now, clock.timeZone())
            },
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
    private fun produceGrtcStopName(response: Response<GrtcPredictionResponse, GrtcErrorResponse>?): String? {
        var stopName by remember { mutableStateOf(response?.toGrtcStopName()) }
        stopName = response?.toGrtcStopName() ?: stopName
        return stopName
    }

    @Composable
    private fun produceGrtcResponseState(stopNumber: Int): Response<GrtcPredictionResponse, GrtcErrorResponse>? {
        var response by remember(stopNumber) { mutableStateOf<Response<GrtcPredictionResponse, GrtcErrorResponse>?>(null) }
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

private fun Response<GrtcPredictionResponse, GrtcErrorResponse>?.mapToGrtcStopState(stopName: String?, now: Instant, timeZone: TimeZone): GrtcStopState {
    return when (this) {
        null -> GrtcStopState.Loading
        is Response.Success if stopName == null-> GrtcStopState.Loading
        is Response.Success if stopName != null -> GrtcStopState.Loaded(
            stopName = stopName,
            predictedArrivals = data.predictions.map { prediction ->
                GrtcStopArrival(
                    arrivalTime = buildAnnotatedString {
                        val durationUntilArrival = prediction.predictedArrivalTime - now
                        val localTime = prediction.predictedArrivalTime.toLocalDateTime(timeZone).time
                        val color = when {
                            !durationUntilArrival.isPositive() -> Color(255, 102, 102)
                            durationUntilArrival <= 5.minutes -> Color(255, 178, 102)
                            durationUntilArrival <= 10.minutes -> Color(255, 255, 102)
                            else -> Color.White
                        }
                        val arrivalDurationString = when {
                            !durationUntilArrival.isPositive() -> "DUE"
                            else -> "${durationUntilArrival.inWholeMinutes} MIN"
                        }
                        withStyle(SpanStyle(color)) {
                            append(localTime.format(STOP_ARRIVAL_TIME_FORMAT))
                            append(" (")
                            append(arrivalDurationString)
                            append(")")
                        }
                    },
                    vehicleId = prediction.vehicleId.takeIf { it.isNotBlank() } ?: "SCHEDULED",
                    routeInfo = buildString {
                        append(prediction.direction)
                        append(" ➜ ")
                        prediction.destination.split("\\s+".toRegex()).joinTo(buffer = this, separator = " ")
                    }
                )
            }
        )
        is Response.Failure.HttpFailure -> GrtcStopState.Error("FAILED: HTTP $statusCode")
        is Response.Failure.NetworkError -> GrtcStopState.Error("FAILED: ${e.message}")
        is Response.Failure.DeserializationError -> GrtcStopState.Error("FAILED: ${e.message}")
        else -> GrtcStopState.Error("FAILED")
    }
}

private fun Response<GrtcPredictionResponse, GrtcErrorResponse>.toGrtcStopName(): String? = when (this) {
    is Response.Success -> data.predictions.firstOrNull()?.stopName
    is Response.Failure -> null
}