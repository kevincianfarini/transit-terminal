package io.github.kevincianfarini.grtc

import androidx.compose.runtime.LaunchedEffect
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.jakewharton.mosaic.StaticEffect
import com.jakewharton.mosaic.runMosaicMain
import com.jakewharton.mosaic.ui.Text
import kotlinx.coroutines.awaitCancellation

public fun main(args: Array<String>) {
    GrtcTransitTerminal().main(args)
}

private class GrtcTransitTerminal : CliktCommand() {

    val stops: List<Int> by option("-s", "--stop").int().multiple()

    override fun run() {
        if (stops.isEmpty()) {
            throw CliktError("You must provide at least one stop number.")
        }
        runMosaicMain {
            StaticEffect {
                Text(stops.joinToString())
            }
            LaunchedEffect(Unit) {
                awaitCancellation()
            }
        }
    }
}