package io.github.kevincianfarini.grtc.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.kevincianfarini.cardiologist.schedulePulse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
public fun Clock.PulseEffect(
    timeZone: TimeZone = TimeZone.UTC,
    atSecond: Int? = null,
    atMinute: Int? = null,
    atHour: Int? = null,
    onDayOfMonth: Int? = null,
    inMonth: Month? = null,
    onDayOfWeek: DayOfWeek? = null,
    vararg keys: Any?,
    block: suspend CoroutineScope.(LocalDateTime) -> Unit,
) {
    LaunchedEffect(this, timeZone, atSecond, atMinute, atHour, onDayOfMonth, inMonth, onDayOfWeek, *keys) {
        schedulePulse(timeZone, atSecond, atMinute, atHour, onDayOfMonth, inMonth, onDayOfWeek).beat { occurred ->
            coroutineScope { block(occurred.toLocalDateTime(timeZone)) }
        }
    }
}

public interface ZonedClock : Clock {

    public fun timeZone(): TimeZone

    public object System : ZonedClock, Clock by Clock.System {
        override fun timeZone(): TimeZone = TimeZone.currentSystemDefault()
    }
}

public fun ZonedClock.localNow(): LocalDateTime = now().toLocalDateTime(timeZone())
