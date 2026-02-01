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

public inline fun <T : Any, E : Any, R : Any> Response<T, E>.fold(
    transformSuccess: (T) -> R,
    transformFailure: (Response.Failure<E>) -> R,
): R = when (this) {
    is Response.Success -> transformSuccess(data)
    is Response.Failure -> transformFailure(this)
}

public inline fun <T : Any, R : Any, E : Any> Response<T, E>.flatMapSuccess(
    transform: (T) -> Response<R, E>
): Response<R, E> {
    return when (this) {
        is Response.Success -> transform(data)
        is Response.Failure -> this
    }
}

public fun <T : Any, E : Any> List<Response<T, E>>.reduceResponses(
    accumulator: (acc: T, next: T) -> T
): Response<T, E> = reduce { acc, response ->
    when (acc) {
        is Response.Success if response is Response.Success -> Response.Success(accumulator(acc.data, response.data))
        is Response.Failure -> return@reduce acc
        else -> return@reduce response
    }
}