package io.github.kevincianfarini.grtc.state

public sealed interface LoadingState<out T : Any, out E : Any> {
    public data object Loading : LoadingState<Nothing, Nothing>
    public data class Loaded<out T : Any>(val data: T) : LoadingState<T, Nothing>
    public data class Refreshing<out T : Any>(val data: T) : LoadingState<T, Nothing>
    public data class Failed<out E : Any>(val error: E) : LoadingState<Nothing, E>
}