package io.github.kevincianfarini.grtc.repository

import io.github.kevincianfarini.grtc.networkModel.GrtcResponse
import io.github.kevincianfarini.grtc.networkModel.Response
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HeadersBuilder
import io.ktor.http.isSuccess
import io.ktor.utils.io.core.toByteArray
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.ByteString
import kotlin.time.Instant

public interface GrtcStopRepository {

    public suspend fun getBusStopSchedulePredictions(stopNumber: Int, now: Instant): Response<GrtcResponse, Nothing>
}

public class KtorGrtcStopRepository : GrtcStopRepository {

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

    override suspend fun getBusStopSchedulePredictions(stopNumber: Int, now: Instant): Response<GrtcResponse, Nothing> {
        val url = "http://new.grtcbustracker.com/bustime/api/v3/getpredictions?requestType=getpredictions&locale=en&stpid=$stopNumber&rtpidatafeed=bustime&top=20&key=Qskvu4Z5JDwGEVswqdAVkiA5B&format=json&xtime=${now.toEpochMilliseconds()}"
        val request = request {
            url(url)
            this.headers { appendHeaders(url, now) }
        }
        val response = try {
            client.get(request)
        } catch (e: IOException) {
            return Response.Failure.NetworkError(e)
        }
        val body = try {
            json.decodeFromString(GrtcResponse.serializer(), response.bodyAsText())
        } catch (e: SerializationException) {
            return Response.Failure.DeserializationError(e)
        }
        return when {
            response.status.isSuccess() -> Response.Success(body)
            else -> Response.Failure.HttpFailure(response.status.value, errorData = null)
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