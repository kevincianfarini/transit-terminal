package io.github.kevincianfarini.grtc.screen

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.fillMaxSize
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import io.github.kevincianfarini.grtc.components.BorderedTitledBox
import io.github.kevincianfarini.grtc.state.GrtcStopListScreenState
import io.github.kevincianfarini.grtc.state.GrtcStopState

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
    BorderedTitledBox(title = stop.toString(), titleColor = Color.White, borderColor = Color.White) {

    }
}
