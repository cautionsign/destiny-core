/**
 * @author smallufo
 * Created on 2007/6/12 at 上午 6:09:35
 */
package destiny.core.astrology


import destiny.core.AbstractPointTest
import destiny.core.astrology.Planet.Companion.aheadOf
import destiny.core.getAbbreviation
import destiny.core.toString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.test.*

class PlanetTest : AbstractPointTest(Planet::class) {

  @Test
  fun testSerialize() {
    Planet.values.forEach { p ->
      val rawJson = Json.encodeToString(p)
      assertEquals("\"${p.nameKey}\"", rawJson)
      assertSame(p, Json.decodeFromString(rawJson))
    }
  }


  /** 將 太陽 up-case 再 down-cast , 比對 equality 以及 same  */
  @Test
  fun testPlanetEqual() {
    val sun1 = Planet.SUN
    val sun2 = Planet.SUN

    val points = setOf<AstroPoint>(sun2)

    val pointsIt = points.iterator()
    while (pointsIt.hasNext()) {
      val p = pointsIt.next()

      if (p is Planet) {
        assertSame(p, sun1)
        assertSame(p, sun1)
      } else
        throw RuntimeException("Error , it should be Planet ")
    }
  }

  @Test
  fun testToStringLocale() {
    assertEquals("太陽", Planet.SUN.toString(Locale.TAIWAN))
    assertEquals("太阳", Planet.SUN.toString(Locale.SIMPLIFIED_CHINESE))
    assertEquals("Sun", Planet.SUN.toString(Locale.ENGLISH))

    assertEquals("日", Planet.SUN.getAbbreviation(Locale.TAIWAN))

    val locale = Locale.ENGLISH
    assertEquals("Sun", Planet.SUN.toString(locale))
    assertEquals("Su", Planet.SUN.getAbbreviation(locale))
  }

  @Test
  fun testCompare() {
    assertTrue(Planet.SUN < Planet.MOON)
    assertTrue(Planet.MOON < Planet.MERCURY)
    assertTrue(Planet.MERCURY < Planet.VENUS)
    assertTrue(Planet.VENUS < Planet.MARS)
    assertTrue(Planet.MARS < Planet.JUPITER)
    assertTrue(Planet.JUPITER < Planet.SATURN)
    assertTrue(Planet.SATURN < Planet.URANUS)
    assertTrue(Planet.URANUS < Planet.NEPTUNE)
    assertTrue(Planet.NEPTUNE < Planet.PLUTO)
  }


  @Test
  fun testAheadOf() {
    assertEquals(0, Planet.SUN.aheadOf(Planet.SUN))
    assertEquals(1, Planet.MOON.aheadOf(Planet.SUN))
    assertEquals(2, Planet.MARS.aheadOf(Planet.SUN))
    assertEquals(3, Planet.MERCURY.aheadOf(Planet.SUN))
    assertEquals(4, Planet.JUPITER.aheadOf(Planet.SUN))
    assertEquals(5, Planet.VENUS.aheadOf(Planet.SUN))
    assertEquals(6, Planet.SATURN.aheadOf(Planet.SUN))

    assertEquals(0, Planet.SATURN.aheadOf(Planet.SATURN))
    assertEquals(1, Planet.SUN.aheadOf(Planet.SATURN))
    assertEquals(2, Planet.MOON.aheadOf(Planet.SATURN))
    assertEquals(3, Planet.MARS.aheadOf(Planet.SATURN))
    assertEquals(4, Planet.MERCURY.aheadOf(Planet.SATURN))
    assertEquals(5, Planet.JUPITER.aheadOf(Planet.SATURN))
    assertEquals(6, Planet.VENUS.aheadOf(Planet.SATURN))

    try {
      assertEquals(6, Planet.PLUTO.aheadOf(Planet.SATURN))
      fail()
    } catch (ignored: IllegalArgumentException) {
      assertTrue(true)
    }

    try {
      assertEquals(6, Planet.SATURN.aheadOf(Planet.PLUTO))
      fail()
    } catch (ignored: IllegalArgumentException) {
      assertTrue(true)
    }

  }

}
