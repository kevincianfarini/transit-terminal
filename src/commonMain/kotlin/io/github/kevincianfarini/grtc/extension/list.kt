package io.github.kevincianfarini.grtc.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


public suspend fun <A, B> Iterable<A>.mapConcurrently(
    transform: suspend CoroutineScope.(A) -> B
): List<B> = coroutineScope {
    map {
        async { transform(this, it) }
    }.awaitAll()
}

public suspend fun <A, B> Iterable<A>.flatMapConcurrently(
    transform: suspend CoroutineScope.(A) -> List<B>
): List<B> = coroutineScope {
    flatMap {
        async { transform(this, it) }.await()
    }
}