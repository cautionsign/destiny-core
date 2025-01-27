/**
 * Created by smallufo on 2019-05-23.
 */
package destiny.core.astrology

import destiny.core.AbstractPointTest
import destiny.core.getAbbreviation
import destiny.core.toString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.test.*


class AxisTest : AbstractPointTest(Axis::class) {

  @Test
  fun testSerialize() {
    Axis.values.forEach { p ->
      val rawJson = Json.encodeToString(p)
      assertEquals("\"${p.nameKey}\"", rawJson)
      assertSame(p, Json.decodeFromString(rawJson))
    }
  }

  @Test
  fun testString() {

    assertEquals("Rising" , Axis.RISING.toString(Locale.ENGLISH))
    assertEquals("AC" , Axis.RISING.getAbbreviation(Locale.ENGLISH))
    assertEquals("東昇" , Axis.RISING.toString(Locale.TRADITIONAL_CHINESE))
    assertEquals("東昇" , Axis.RISING.toString(Locale.TAIWAN))
    assertEquals("昇" , Axis.RISING.getAbbreviation(Locale.TAIWAN))

    Axis.array.forEach { p ->
      p.toString().also {
        assertNotNull(it)
        logger.info("{} 縮寫 (default locale) -> {}", it, p.getAbbreviation(Locale.getDefault()))
        assertNotSame('!', it[0])
      }


      p.toString(Locale.ENGLISH).also {
        assertNotNull(it)
        logger.info("{} abbreviation (ENGLISH) -> {}", it, p.getAbbreviation(Locale.getDefault()))
        assertNotSame('!', it[0])
      }
    }
  }
}
