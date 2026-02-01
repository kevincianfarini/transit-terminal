package io.github.kevincianfarini.grtc.state

public sealed interface LoadingState<out T : Any, out E : Any> {
    public data object Loading : LoadingState<Nothing, Nothing>
    public data class Loaded<out T : Any>(val data: T) : LoadingState<T, Nothing>
    public data class Failed<out E : Any>(val error: E) : LoadingState<Nothing, E>
}

public fun <Data : Any, Error : Any, MappedData : Any, MappedError : Any> LoadingState<Data, Error>.map(
    onSuccess: (value: Data) -> MappedData,
    onFailure: (error: Error) -> MappedError,
): LoadingState<MappedData, MappedError> = when (this) {
    is LoadingState.Failed -> LoadingState.Failed(onFailure(error))
    is LoadingState.Loaded -> LoadingState.Loaded(onSuccess(data))
    LoadingState.Loading -> LoadingState.Loading
}

public fun <T : Any, E : Any, R> LoadingState<T, E>.fold(
    onLoading: () -> R,
    onSuccess: (value: T) -> R,
    onFailure: (error: E) -> R,
): R = when (this) {
    is LoadingState.Failed -> onFailure(error)
    is LoadingState.Loaded -> onSuccess(data)
    LoadingState.Loading -> onLoading()
}