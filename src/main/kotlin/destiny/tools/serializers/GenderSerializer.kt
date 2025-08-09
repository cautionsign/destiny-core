/**
 * Created by smallufo on 2025-01-25.
 */
package destiny.tools.serializers

import destiny.core.Gender
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


object GenderSerializer : KSerializer<Gender> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Gender", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Gender) {
    val result = when (value) {
      Gender.男 -> "M"
      Gender.女 -> "F"
    }
    encoder.encodeString(result)
  }

  override fun deserialize(decoder: Decoder): Gender {
    return decoder.decodeString().let { raw ->
      when (raw.uppercase()) {
        "M", "男" -> Gender.男
        "F", "女" -> Gender.女
        else      -> throw IllegalArgumentException("Invalid gender value : $raw")
      }
    }
  }
}
