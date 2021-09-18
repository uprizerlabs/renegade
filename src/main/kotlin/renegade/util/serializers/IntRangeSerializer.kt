package renegade.util.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class IntRangeSerializer : KSerializer<IntRange> {

    override fun serialize(encoder: Encoder, value: IntRange) {
        val surrogate = IntRangeSurrogate(value.first, value.last)
        encoder.encodeSerializableValue(IntRangeSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): IntRange {
        val surrogate = decoder.decodeSerializableValue(IntRangeSurrogate.serializer())
        return surrogate.start .. surrogate.last
    }

    override val descriptor: SerialDescriptor get() = IntRangeSurrogate.serializer().descriptor
}

@Serializable
@SerialName("IntRange")
private class IntRangeSurrogate(val start : Int, val last : Int)