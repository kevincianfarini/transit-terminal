package io.github.kevincianfarini.grtc.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.Color
import io.github.kevincianfarini.cardiologist.PulseBackpressureStrategy.Companion.CancelPrevious
import io.github.kevincianfarini.cardiologist.schedulePulse
import io.github.kevincianfarini.grtc.extension.ZonedClock
import io.github.kevincianfarini.grtc.networkModel.GrtcErrorResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcPredictionResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcRouteDirectionsResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcRoutesResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcStopsResponse
import io.github.kevincianfarini.grtc.networkModel.Response
import io.github.kevincianfarini.grtc.repository.GrtcStopRepository
import io.github.kevincianfarini.grtc.state.GrtcStopArrival
import io.github.kevincianfarini.grtc.state.GrtcStopListScreenState
import io.github.kevincianfarini.grtc.state.GrtcStopState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
public class GrtcStopListPresenter(
    private val stops: List<String>,
    private val clock: ZonedClock,
    private val repository: GrtcStopRepository,
) {

    @Composable
    public fun present(): GrtcStopListScreenState {
        val now = produceNowState()
        val grtcResponses = stops.associateWith { produceGrtcResponseState(it) }
        val grtcStopInfo = produceGrtcStops()
        return GrtcStopListScreenState(
            stops = grtcResponses.map { (stop, response) ->
                response.mapToGrtcStopState(grtcStopInfo[stop], now, clock.timeZone())
            },
            currentTime = now.toLocalDateTime(clock.timeZone()).format(NOW_DATE_TIME_FORMAT),
        )
    }

    @Composable
    private fun produceNowState(): Instant {
        var now by remember { mutableStateOf(clock.now()) }
        LaunchedEffect(clock) {
            clock.schedulePulse(clock.timeZone()).beat(CancelPrevious) { occurred ->
                now = occurred
            }
        }
        return now
    }

    @Composable
    private fun produceGrtcStops(): Map<String, String> {
        val stopInfo: MutableMap<String, String> = remember { mutableStateMapOf() }
        LaunchedEffect(Unit) {
            withContext(Dispatchers.Default) {
                (repository.getRoutes() as Response.Success<GrtcRoutesResponse>).data.routes.forEach { route ->
                    launch {
                        (repository.getRouteDirections(route) as Response.Success<GrtcRouteDirectionsResponse>).data.directions.forEach { direction ->
                            launch {
                                (repository.getBusStops(route, direction) as Response.Success<GrtcStopsResponse>).data.stops.forEach { stop ->
                                    stopInfo[stop.stopId] = stop.stopName
                                }
                            }
                        }
                    }
                }
            }
        }
        return stopInfo
    }

    @Composable
    private fun produceGrtcResponseState(stopNumber: String): Response<GrtcPredictionResponse, GrtcErrorResponse>? {
        var response by remember(stopNumber) { mutableStateOf<Response<GrtcPredictionResponse, GrtcErrorResponse>?>(null) }
        LaunchedEffect(Unit) {
            response = repository.getBusStopSchedulePredictions(stopNumber)
        }
        LaunchedEffect(clock) {
            clock.schedulePulse(clock.timeZone()) {
                atSeconds(0, 30)
            }.beat(CancelPrevious) {
                response = repository.getBusStopSchedulePredictions(stopNumber)
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

private fun Response<GrtcPredictionResponse, GrtcErrorResponse>?.mapToGrtcStopState(
    stopName: String?,
    now: Instant,
    timeZone: TimeZone
): GrtcStopState {

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
                            durationUntilArrival <= 1.minutes -> Color(255, 102, 102)
                            durationUntilArrival <= 5.minutes -> Color(255, 178, 102)
                            durationUntilArrival <= 10.minutes -> Color(255, 255, 102)
                            else -> Color.White
                        }
                        val arrivalDurationString = when {
                            !durationUntilArrival.isPositive() -> "DUE"
                            durationUntilArrival.inWholeMinutes > 0 -> "${durationUntilArrival.inWholeMinutes} MIN"
                            else -> "${durationUntilArrival.inWholeSeconds} SEC"
                        }
                        withStyle(SpanStyle(color)) {
                            append(localTime.format(STOP_ARRIVAL_TIME_FORMAT))
                            append(" (")
                            append(arrivalDurationString)
                            append(")")
                        }
                    },
                    vehicleStatus = buildAnnotatedString {
                        when {
                            prediction.delayed -> withStyle(SpanStyle(color = Color(255, 102, 102))) {
                                append("DELAYED")
                            }
                            prediction.vehicleId.isBlank() -> append("SCHEDULED")
                            else -> append("EN ROUTE")
                        }
                    },
                    routeInfo = buildAnnotatedString {
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