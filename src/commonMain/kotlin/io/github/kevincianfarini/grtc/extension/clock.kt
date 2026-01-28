package io.github.kevincianfarini.grtc.extension

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

public interface ZonedClock : Clock {

    public fun timeZone(): TimeZone

    public object System : ZonedClock, Clock by Clock.System {
        override fun timeZone(): TimeZone = TimeZone.currentSystemDefault()
    }
}

public fun ZonedClock.nowLocal(): LocalDateTime = now().toLocalDateTime(timeZone())
