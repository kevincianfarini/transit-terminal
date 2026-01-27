package io.github.kevincianfarini.grtc.networkModel

public sealed interface Response<out T, out E> {
    public data class Success<out T : Any>(public val data: T) : Response<T, Nothing>
    public sealed interface Failure<out E> : Response<Nothing, E> {
        public data class NetworkError(val e: Throwable) : Failure<Nothing>
        public data class HttpFailure<out E>(val statusCode: Int, val errorData: E?) : Failure<E>
        public data class DeserializationError(val e: Throwable) : Failure<Nothing>
        public data class UnknownError(val e: Throwable) : Failure<Nothing>
    }
}