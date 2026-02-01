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
import io.github.kevincianfarini.cardiologist.PulseBackpressureStrategy.Companion.CancelPrevious
import io.github.kevincianfarini.cardiologist.schedulePulse
import io.github.kevincianfarini.grtc.extension.ZonedClock
import io.github.kevincianfarini.grtc.networkModel.GrtcErrorResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcPredictionResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcStopsResponse
import io.github.kevincianfarini.grtc.networkModel.Response
import io.github.kevincianfarini.grtc.networkModel.fold
import io.github.kevincianfarini.grtc.repository.GrtcStopRepository
import io.github.kevincianfarini.grtc.state.GrtcStopArrival
import io.github.kevincianfarini.grtc.state.GrtcStopListScreenState
import io.github.kevincianfarini.grtc.state.GrtcStopState
import io.github.kevincianfarini.grtc.state.LoadingState
import io.github.kevincianfarini.grtc.state.map
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
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
            stops = grtcResponses.mapToGrtcStopState(grtcStopInfo, now, clock.timeZone()),
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
    private fun produceGrtcStops(): LoadingState<GrtcStopsResponse, Response.Failure<GrtcErrorResponse>> {
        var loadingState by remember {
            mutableStateOf<LoadingState<GrtcStopsResponse, Response.Failure<GrtcErrorResponse>>>(
                value = LoadingState.Loading
            )
        }
        LaunchedEffect(Unit) {
            loadingState = LoadingState.Loading
            loadingState = repository.getBusStops().fold(
                transformSuccess = { LoadingState.Loaded(it) },
                transformFailure = { LoadingState.Failed(it) }
            )
        }
        return loadingState
    }

    @Composable
    private fun produceGrtcResponseState(
        stopNumber: String
    ): LoadingState<GrtcPredictionResponse, Response.Failure<GrtcErrorResponse>> {
        var loadingState by remember(stopNumber) {
            mutableStateOf<LoadingState<GrtcPredictionResponse, Response.Failure<GrtcErrorResponse>>>(
                value = LoadingState.Loading
            )
        }
        LaunchedEffect(Unit) {
            loadingState = LoadingState.Loading
            loadingState = repository.getBusStopSchedulePredictions(stopNumber).toLoadingState()
        }
        LaunchedEffect(clock) {
            clock.schedulePulse(clock.timeZone()) {
                atSeconds(0, 30)
            }.beat(CancelPrevious) {
                loadingState = repository.getBusStopSchedulePredictions(stopNumber).toLoadingState()

            }
        }
        return loadingState
    }
}

private fun <T : Any, E : Any> Response<T, E>.toLoadingState(): LoadingState<T, Response.Failure<E>> {
    return when (this) {
        is Response.Success -> LoadingState.Loaded(data)
        is Response.Failure -> LoadingState.Failed(this)
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

private fun Map<String, LoadingState<GrtcPredictionResponse, Response.Failure<GrtcErrorResponse>>>.mapToGrtcStopState(
    stopInfo: LoadingState<GrtcStopsResponse, Response.Failure<GrtcErrorResponse>>,
    now: Instant,
    timeZone: TimeZone
): List<GrtcStopState> = map { (stopId: String, predictionData) ->
    GrtcStopState(
        stopName = stopInfo.map(
            onSuccess = { stopResponse ->
                buildAnnotatedString {
                    append(
                        stopResponse.stops.firstNotNullOf { stop ->
                            stop.stopName.takeIf { stop.stopId == stopId }
                        }
                    )
                }
            },
            onFailure = { error -> error.mapToErrorString() }
        ),
        predictedArrivals = predictionData.map(
            onSuccess = { predictionResponse ->
                predictionResponse.predictions.map { prediction ->
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
            },
            onFailure = { error -> error.mapToErrorString() }
        )
    )
}

private fun Response.Failure<GrtcErrorResponse>.mapToErrorString(): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = Color(255, 102, 102))) {
        when (this@mapToErrorString) {
            is Response.Failure.HttpFailure -> {
                append("FAILED: HTTP ")
                append(statusCode.toString())
            }
            is Response.Failure.DeserializationError -> {
                append("FAILED: ")
                append(e.message)
            }
            is Response.Failure.NetworkError -> {
                append("FAILED: ")
                append(e.message)
            }
            is Response.Failure.UnknownError -> {
                append("FAILED: ")
                append(e.message)
            }
        }
    }
}