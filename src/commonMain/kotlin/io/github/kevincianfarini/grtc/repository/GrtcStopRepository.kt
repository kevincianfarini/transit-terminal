package io.github.kevincianfarini.grtc.repository

import io.github.kevincianfarini.grtc.networkModel.GrtcResponse
import io.github.kevincianfarini.grtc.networkModel.GrtcStopPrediction
import io.github.kevincianfarini.grtc.networkModel.Response
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

public interface GrtcStopRepository {

    public suspend fun getBusStopSchedulePredictions(stopNumber: Int): Response<GrtcResponse, Nothing>
}

public class KtorGrtcStopRepository : GrtcStopRepository {

    override suspend fun getBusStopSchedulePredictions(stopNumber: Int): Response<GrtcResponse, Nothing> {
        delay(3.seconds)
        return when (Random.nextInt(4)) {
            0, 1 -> Response.Success(getRandomGrtcResponse())
            2 -> Response.Failure.HttpFailure(statusCode = 500, errorData = null)
            else -> Response.Failure.NetworkError(IllegalStateException("Oops!"))
        }
    }

    private fun getRandomGrtcResponse(): GrtcResponse {
        val stop = Random.choose(
            "ROCKETTS LANDING STATION",
            "EAST RIVERFRONT WESTBOUND STATION",
            "SHOCKOE BOTTOM WESTBOUND STATION"
        )
        val stopNumber = Random.nextInt(1000, 9999)
        return GrtcResponse(
            predictions = listOf(
                GrtcStopPrediction(
                    stopNumber = stopNumber,
                    predictedArrivalTime = LocalDateTime(2026, 1, 26, 19, 40),
                    stopName = stop,
                ),
                GrtcStopPrediction(
                    stopNumber = stopNumber,
                    predictedArrivalTime = LocalDateTime(2026, 1, 26, 19, 45),
                    stopName = stop,
                ),
                GrtcStopPrediction(
                    stopNumber = stopNumber,
                    predictedArrivalTime = LocalDateTime(2026, 1, 26, 19, 50),
                    stopName = stop,
                ),
            ),
        )
    }
}

private fun <T> Random.choose(vararg choices: T): T {
    return choices[nextInt(choices.size)]
}