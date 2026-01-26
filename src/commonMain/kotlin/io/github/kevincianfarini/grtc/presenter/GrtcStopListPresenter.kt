package io.github.kevincianfarini.grtc.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.kevincianfarini.grtc.state.GrtcStopListState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

public class GrtcStopListPresenter(private val stops: List<Int>) {

    @Composable
    public fun present(): GrtcStopListState {
        var count by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(1.seconds)
                count++
            }
        }
        return GrtcStopListState(stops, count)
    }
}