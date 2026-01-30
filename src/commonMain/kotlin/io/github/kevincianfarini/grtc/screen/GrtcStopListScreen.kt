package io.github.kevincianfarini.grtc.screen

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.LocalTerminalState
import com.jakewharton.mosaic.layout.fillMaxSize
import com.jakewharton.mosaic.layout.fillMaxWidth
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.requiredWidth
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.AnnotatedString
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.RowScope
import com.jakewharton.mosaic.ui.Text
import io.github.kevincianfarini.grtc.components.BorderedTitledBox
import io.github.kevincianfarini.grtc.components.produceAnimatedLoadingString
import io.github.kevincianfarini.grtc.state.GrtcStopArrival
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
        val maxVehicleStatusWidth = state.stops.maxOf { stop ->
            stop.attributeWidth { loaded ->
                loaded.predictedArrivals.maxOfOrNull { it.vehicleStatus.length } ?: 0
            }
        }
        val maxArrivalTimeStatusWidth = state.stops.maxOf { stop ->
            stop.attributeWidth { loaded ->
                loaded.predictedArrivals.maxOfOrNull { it.arrivalTime.length } ?: 0
            }
        }
        Column {
            state.stops.forEach { stop -> TransitStop(stop, maxVehicleStatusWidth, maxArrivalTimeStatusWidth) }
        }
    }
}

@Composable
private fun TransitStop(stop: GrtcStopState, vehicleStatusWidth: Int, arrivalTimeWidth: Int) {
    val (title, color) = when (stop) {
        is GrtcStopState.Loaded -> Pair(stop.stopName, Color.White)
        is GrtcStopState.Loading -> Pair("Loading ${produceAnimatedLoadingString()}", Color.White)
        is GrtcStopState.Error -> Pair("FAILED", Color.Red)
    }
    BorderedTitledBox(title = title, titleColor = color, borderColor = color, modifier = Modifier.fillMaxWidth()) {
        when (stop) {
            is GrtcStopState.Loaded -> TransitStopArrivals(stop.predictedArrivals, vehicleStatusWidth, arrivalTimeWidth)
            is GrtcStopState.Error -> Text(
                value = stop.message,
                modifier = Modifier.align(Alignment.Center).width(LocalTerminalState.current.size.columns / 2)
            )
            else -> Unit
        }
    }
}

@Composable
private fun TransitStopArrivals(
    arrivals: List<GrtcStopArrival>,
    vehicleStatusWidth: Int,
    arrivalTimeWidth: Int,
) = when (arrivals.isEmpty()) {
    true -> Text("No arrivals scheduled.", color = Color.Red)
    false -> Column(modifier = Modifier.fillMaxWidth()) {
        arrivals.forEach { arrival ->
            Row(modifier = Modifier.fillMaxWidth()) {
                VehicleStatusText(arrival.vehicleStatus, vehicleStatusWidth)
                RouteInfoText(arrival.routeInfo)
                ArrivalTimeText(arrival.arrivalTime, arrivalTimeWidth)
            }
        }
    }
}

@Composable
private fun VehicleStatusText(
    status: AnnotatedString,
    width: Int,
) = Box(modifier = Modifier.requiredWidth(width)) {
    // Set this as a static width to help align the overall layout.
    Text(value = status)
}

@Composable
private fun RowScope.RouteInfoText(routeInfo: AnnotatedString) = Box(modifier = Modifier.weight(1f)) {
    Text(routeInfo, modifier = Modifier.padding(horizontal = 5))
}

@Composable
private fun ArrivalTimeText(
    arrivalTime: AnnotatedString,
    width: Int,
) = Box(modifier = Modifier.requiredWidth(width)) {
    // Set this as a static width to help align the overall layout.
    Text(value = arrivalTime)
}

private fun GrtcStopState.attributeWidth(selector: (GrtcStopState.Loaded) -> Int) = when (this) {
    is GrtcStopState.Error -> 0
    is GrtcStopState.Loaded -> selector(this)
    GrtcStopState.Loading -> 0
}