package io.github.kevincianfarini.grtc.screen

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import io.github.kevincianfarini.grtc.state.GrtcStopListState

@Composable
public fun GrtcTransitListScreen(state: GrtcStopListState) {
    Column {
        Text(state.stops.joinToString())
        Text(state.count.toString())
    }
}