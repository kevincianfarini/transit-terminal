package io.github.kevincianfarini.grtc.networkModel

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.format.optional
import kotlinx.datetime.toInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

public object GrtcTimestampDeserializer : KSerializer<Instant> {

    private val format = LocalDateTime.Format {
        year()
        monthNumber()
        day()
        char(' ')
        hour()
        char(':')
        minute()
        optional {
            char(':')
            second()
        }
    }

    private val timezone = TimeZone.of("America/New_York")

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("timestamp", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        TODO("Not yet implemented")
    }

    override fun deserialize(decoder: Decoder): Instant {
        return format.parse(decoder.decodeString()).toInstant(timezone)
    }
}
