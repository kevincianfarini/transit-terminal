package io.github.kevincianfarini.grtc.state

public sealed interface GrtcStopState {
    public data object Loading : GrtcStopState
    public data class Loaded(val stopNumber: Int) : GrtcStopState
    public data class NetworkError(val statusCode: Int, val message: String) : GrtcStopState
}