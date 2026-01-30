package io.github.kevincianfarini.grtc

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.jakewharton.mosaic.LocalTerminalState
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.runMosaicMain
import com.jakewharton.mosaic.ui.Box
import io.github.kevincianfarini.grtc.extension.ZonedClock
import io.github.kevincianfarini.grtc.presenter.GrtcStopListPresenter
import io.github.kevincianfarini.grtc.repository.KtorGrtcStopRepository
import io.github.kevincianfarini.grtc.screen.GrtcTransitListScreen

public fun main(args: Array<String>) {
    GrtcTransitTerminal().main(args)
}

private class GrtcTransitTerminal : CliktCommand() {

    val stops: List<Int> by option("-s", "--stop").int().multiple()

    override fun run() {
        if (stops.isEmpty()) {
            throw CliktError("You must provide at least one stop number.")
        }
        val presenter = GrtcStopListPresenter(
            stops = stops.map { it.toString() },
            clock = ZonedClock.System,
            repository = KtorGrtcStopRepository(ZonedClock.System)
        )
        presentGrtcTransitApplication(presenter)
    }
}

private fun presentGrtcTransitApplication(presenter: GrtcStopListPresenter) = runMosaicMain {
    val terminal = LocalTerminalState.current
    Box(modifier = Modifier.width(terminal.size.columns).height(terminal.size.rows - 1)) {
        GrtcTransitListScreen(presenter.present())
    }
}