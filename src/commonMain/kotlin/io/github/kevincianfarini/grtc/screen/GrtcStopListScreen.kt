package io.github.kevincianfarini.grtc.screen

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.fillMaxSize
import com.jakewharton.mosaic.layout.fillMaxWidth
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.requiredWidth
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.AnnotatedString
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
import io.github.kevincianfarini.grtc.state.LoadingState
import io.github.kevincianfarini.grtc.state.fold

@Composable
public fun GrtcTransitListScreen(state: GrtcStopListScreenState) {
    BorderedTitledBox(
        modifier = Modifier.fillMaxSize(),
        title = state.currentTime,
        titleColor = Color.White,
        borderColor = Color.White
    ) {
        val maxVehicleStatusWidth = state.stops.maxOf { stop ->
            stop.predictedArrivals.fold(
                onLoading = { 0 },
                onSuccess = { arrivals ->
                    arrivals.maxOfOrNull { it.vehicleStatus.length } ?: 0
                },
                onFailure = { 0 },
            )
        }
        val maxArrivalTimeStatusWidth = state.stops.maxOf { stop ->
            stop.predictedArrivals.fold(
                onLoading = { 0 },
                onSuccess = { arrivals ->
                    arrivals.maxOfOrNull { it.arrivalTime.length } ?: 0
                },
                onFailure = { 0 },
            )
        }
        Column {
            state.stops.forEach { stop -> TransitStop(stop, maxVehicleStatusWidth, maxArrivalTimeStatusWidth) }
        }
    }
}

@Composable
private fun TransitStop(stop: GrtcStopState, vehicleStatusWidth: Int, arrivalTimeWidth: Int) {
    val (title, color) = when (stop.stopName) {
        is LoadingState.Loading -> Pair("Loading ${produceAnimatedLoadingString()}", Color.White)
        is LoadingState.Loaded if stop.predictedArrivals is LoadingState.Loading -> Pair(
            "${stop.stopName.data} ${produceAnimatedLoadingString()}", Color.White
        )
        is LoadingState.Loaded -> Pair(stop.stopName.data.toString(), Color.White)
        is LoadingState.Failed -> Pair(stop.stopName.error.toString(), Color.Red)
    }
    BorderedTitledBox(title = title, titleColor = color, borderColor = color, modifier = Modifier.fillMaxWidth()) {
        when (stop.predictedArrivals) {
            is LoadingState.Failed -> Text(stop.predictedArrivals.error)
            is LoadingState.Loaded -> TransitStopArrivals(stop.predictedArrivals.data, vehicleStatusWidth, arrivalTimeWidth)
            LoadingState.Loading -> Unit
        }
    }
}

@Composable
private fun TransitStopArrivals(
    arrivals: List<GrtcStopArrival>,
    vehicleStatusWidth: Int,
    arrivalTimeWidth: Int,
) = when (arrivals.isEmpty()) {
    true -> Text("NO SCHEDULED ARRIVALS", color = Color(255, 102, 102))
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