package io.github.kevincianfarini.grtc.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
public fun produceAnimatedLoadingString(): String {
    var progress by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(90)
            progress = (progress + 1) % 6
        }
    }
    return when (progress) {
        0 -> "◜"
        1 -> "◠"
        2 -> "◝"
        3 -> "◞"
        4 -> "◡"
        else -> "◟"
    }
}