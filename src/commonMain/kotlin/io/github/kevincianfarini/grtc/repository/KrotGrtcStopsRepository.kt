package io.github.kevincianfarini.grtc.repository

import io.github.kevincianfarini.grtc.extension.mapConcurrently
import io.github.kevincianfarini.grtc.networkModel.GrtcErrorResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcPredictionResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcRoute
import io.github.kevincianfarini.grtc.networkModel.GrtcRouteDirection
import io.github.kevincianfarini.grtc.networkModel.GrtcRouteDirectionsResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcRoutesResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcStopsResponse
import io.github.kevincianfarini.grtc.networkModel.Response
import io.github.kevincianfarini.grtc.networkModel.flatMapSuccess
import io.github.kevincianfarini.grtc.networkModel.reduceResponses
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HeadersBuilder
import io.ktor.http.encodeURLPathPart
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.IOException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.ByteString
import kotlin.time.Clock
import kotlin.time.Instant

public class KtorGrtcStopRepository(private val clock: Clock) : GrtcStopRepository {

    private val client = HttpClient(CIO)
    private val json = Json { ignoreUnknownKeys = true }
    private val xDateFormatter = LocalDateTime.Format {
        dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
        chars(", ")
        day()
        char(' ')
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        year()
        char(' ')
        hour()
        char(':')
        minute()
        char(':')
        second()
        chars(" GMT")
    }
    private val gmt = TimeZone.of("GMT")
    private val hmacKey = ByteString.of(*"ZSqCAFdU7bwxHJUHKYfQUxKin06hMxCK".toByteArray())

    override suspend fun getBusStops(): Response<GrtcStopsResponse, GrtcErrorResponse> = withContext(Dispatchers.Default) {
        getRoutes().flatMapSuccess { routeResponse ->
            routeResponse.routes.mapConcurrently { route ->
                foo(route)
            }.reduceResponses { acc, next -> GrtcStopsResponse(acc.stops + next.stops) }
        }
    }


    private suspend fun foo(route: GrtcRoute): Response<GrtcStopsResponse, GrtcErrorResponse> {
        return getRouteDirections(route).flatMapSuccess { directionsResponse ->
            directionsResponse.directions.mapConcurrently { direction ->
                getBusStops(route, direction)
            }.reduceResponses { acc, next ->
                GrtcStopsResponse(acc.stops + next.stops)
            }
        }
    }

    override suspend fun getBusStopSchedulePredictions(stopNumber: String): Response<GrtcPredictionResponse, GrtcErrorResponse> {
        val now = clock.now()
        val url = "http://new.grtcbustracker.com/bustime/api/v3/getpredictions?requestType=getpredictions&locale=en&stpid=$stopNumber&rtpidatafeed=bustime&top=20&key=Qskvu4Z5JDwGEVswqdAVkiA5B&format=json&xtime=${now.toEpochMilliseconds()}"
        val response = makeRequest(
            url,
            now,
            bodySerializer = GrtcPredictionResponse.serializer(),
            errorSerializer = GrtcErrorResponse.serializer()
        )
        return when (response) {
            is Response.Success -> response
            is Response.Failure.HttpFailure -> {
                val firstErrorMessage = response.errorData?.error?.firstOrNull()?.message
                if ("No arrival times" == firstErrorMessage || "No service scheduled" == firstErrorMessage) {
                    Response.Success(GrtcPredictionResponse(emptyList()))
                } else {
                    response
                }
            }

            else -> response
        }
    }

    private suspend fun getRoutes(): Response<GrtcRoutesResponse, GrtcErrorResponse> {
        val now = clock.now()
        val url = "http://new.grtcbustracker.com/bustime/api/v3/getroutes?requestType=getroutes&locale=en&key=Qskvu4Z5JDwGEVswqdAVkiA5B&format=json&xtime=${now.toEpochMilliseconds()}"
        return makeRequest(
            url = url,
            now = now,
            bodySerializer = GrtcRoutesResponse.serializer(),
            errorSerializer = GrtcErrorResponse.serializer()
        )
    }

    private suspend fun getRouteDirections(
        route: GrtcRoute
    ): Response<GrtcRouteDirectionsResponse, GrtcErrorResponse> {
        val now = clock.now()
        val url = "http://new.grtcbustracker.com/bustime/api/v3/getdirections?requestType=getdirections&locale=en&rt=${route.route}&rtpidatafeed=bustime&key=Qskvu4Z5JDwGEVswqdAVkiA5B&format=json&xtime=${now.toEpochMilliseconds()}"
        return makeRequest(
            url = url,
            now = now,
            bodySerializer = GrtcRouteDirectionsResponse.serializer(),
            errorSerializer = GrtcErrorResponse.serializer()
        )
    }

    private suspend fun getBusStops(
        route: GrtcRoute,
        direction: GrtcRouteDirection
    ): Response<GrtcStopsResponse, GrtcErrorResponse> {
        val now = clock.now()
        val url = "http://new.grtcbustracker.com/bustime/api/v3/getstops?requestType=getstops&locale=en&rt=${route.route}&dir=${direction.id.encodeURLPathPart()}&rtpidatafeed=bustime&key=Qskvu4Z5JDwGEVswqdAVkiA5B&format=json&xtime=${now.toEpochMilliseconds()}"
        return makeRequest(
            url = url,
            now = now,
            bodySerializer = GrtcStopsResponse.serializer(),
            errorSerializer = GrtcErrorResponse.serializer()
        )
    }

    private suspend fun <T : Any, E : Any> makeRequest(
        url: String,
        now: Instant,
        bodySerializer: KSerializer<T>,
        errorSerializer: KSerializer<E>,
    ): Response<T, E> {
        val response = try {
            client.get {
                url(url)
                this.headers { appendHeaders(url, now) }
            }
        } catch (e: IOException) {
            return Response.Failure.NetworkError(e)
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            return Response.Failure.UnknownError(e)
        }
        return try {
            Response.Success(
                data = json.decodeFromString(
                    deserializer = GrtcResponse.serializer(bodySerializer),
                    string = response.bodyAsText(),
                ).response
            )
        } catch (_: SerializationException) {
            try {
                Response.Failure.HttpFailure(
                    statusCode = response.status.value,
                    errorData = json.decodeFromString(
                        deserializer = GrtcResponse.serializer(errorSerializer),
                        string = response.bodyAsText()
                    ).response
                )
            } catch (e: SerializationException) {
                Response.Failure.DeserializationError(e)
            }
        }
    }

    private fun HeadersBuilder.appendHeaders(urlString: String, now: Instant) {
        val xDate = now.toLocalDateTime(gmt).format(xDateFormatter)
        append("X-Date", xDate)
        val content = buildString {
            append(urlString.substring(urlString.indexOf("/api/v3")))
            append(xDate)
        }
        val hashData = ByteString.of(*content.toByteArray()).hmacSha256(hmacKey)
        append("X-Request-ID", hashData.hex())
    }
}
