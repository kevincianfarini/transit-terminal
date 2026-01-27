package io.github.kevincianfarini.grtc.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jakewharton.mosaic.layout.fillMaxSize
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Spacer
import com.jakewharton.mosaic.ui.Text
import io.github.kevincianfarini.grtc.components.BorderedTitledBox
import io.github.kevincianfarini.grtc.state.GrtcStopListScreenState
import io.github.kevincianfarini.grtc.state.GrtcStopState
import kotlinx.coroutines.delay

@Composable
public fun GrtcTransitListScreen(state: GrtcStopListScreenState) {
    BorderedTitledBox(
        modifier = Modifier.fillMaxSize(),
        title = state.currentTime,
        titleColor = Color.White,
        borderColor = Color.White
    ) {
        Column {
            state.stops.forEach { stop -> TransitStop(stop) }
        }
    }
}

@Composable
private fun TransitStop(stop: GrtcStopState) {
    val (title, color) = when (stop) {
        is GrtcStopState.Loaded -> Pair(stop.stopName, Color.White)
        is GrtcStopState.Loading -> Pair(getLoadingText(), Color.White)
        is GrtcStopState.Error -> Pair(stop.message, Color.Red)
    }
    BorderedTitledBox(title = title, titleColor = color, borderColor = color) {
        if (stop is GrtcStopState.Loaded) {
            Column {
                stop.predictedArrivals.forEach { arrival ->
                    Row {
                        Text(arrival.arrivalTime)
                        Spacer(Modifier.weight(1f))
                        Text(arrival.durationUntilArrival)
                    }
                }
            }
        }
    }
}

@Composable
private fun getLoadingText(): String {
    var progress by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(75)
            progress = (progress + 1) % 6
        }
    }
    return when (progress) {
        0 -> "Loading ◜"
        1 -> "Loading ◠"
        2 -> "Loading ◝"
        3 -> "Loading ◞"
        4 -> "Loading ◡"
        else -> "Loading ◟"
    }
}
