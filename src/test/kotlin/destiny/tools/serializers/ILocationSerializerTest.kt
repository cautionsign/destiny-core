/**
 * Created by smallufo on 2023-10-23.
 */
package destiny.tools.serializers

import destiny.core.calendar.ILocation
import destiny.tools.serializers.Assertions.assertLocEquals
import kotlinx.serialization.json.Json
import destiny.tools.KotlinLogging
import kotlin.test.Test

class ILocationSerializerTest {
  private val logger = KotlinLogging.logger { }
  private val json = Json { encodeDefaults = false }

  @Test
  fun testSerializeDeserialize() {
    val location = object : ILocation {
      override val lat: Double = 42.0
      override val lng: Double = -123.45
      override val tzid: String = "America/New_York"
      override val minuteOffset: Int = -300
      override val altitudeMeter: Double = 123.0
    }

    val serialized = json.encodeToString(ILocationSerializer, location)
    logger.info { serialized }
    val deserialized = json.decodeFromString(ILocationSerializer, serialized)

    assertLocEquals(location, deserialized)
  }

  @Test
  fun testSerializeDeserializeWithoutOptionalFields() {
    val location = object : ILocation {
      override val lat: Double = 33.0
      override val lng: Double = -122.0
      override val tzid: String? = null
      override val minuteOffset: Int? = null
      override val altitudeMeter: Double? = null
    }

    val serialized = json.encodeToString(ILocationSerializer, location)
    logger.info { serialized }
    val deserialized = json.decodeFromString(ILocationSerializer, serialized)

    assertLocEquals(location, deserialized)
  }
}
