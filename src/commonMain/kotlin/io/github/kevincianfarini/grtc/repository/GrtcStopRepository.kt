package io.github.kevincianfarini.grtc.repository

import io.github.kevincianfarini.grtc.networkModel.GrtcResponse

public interface GrtcStopRepository {

    public suspend fun getBusStopSchedulePredictions(): Result<GrtcResponse>
}