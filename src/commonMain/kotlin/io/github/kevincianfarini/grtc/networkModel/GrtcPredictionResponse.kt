package io.github.kevincianfarini.grtc.networkModel

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

@Serializable
public data class GrtcPredictionResponse(
    @SerialName("prd") val predictions: List<GrtcStopPrediction>,
)

@Serializable
public data class GrtcStopPrediction(
    @SerialName("stpid") val stopNumber: String,
    @SerialName("stpnm") val stopName: String,
    @SerialName("prdtm") @Serializable(with = GrtcTimestampDeserializer::class)
    val predictedArrivalTime: Instant,
    @SerialName("des") val destination: String,
    @SerialName("rtdir") val direction: String,
    @SerialName("vid") val vehicleId: String,
)

private object GrtcTimestampDeserializer : KSerializer<Instant> {

    private val format = LocalDateTime.Format {
        year()
        monthNumber()
        day()
        char(' ')
        hour()
        char(':')
        minute()
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
