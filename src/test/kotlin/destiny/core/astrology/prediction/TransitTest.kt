/**
 * Created by smallufo on 2022-08-06.
 */
package destiny.core.astrology.prediction

import destiny.core.calendar.JulDayResolver1582CutoverImpl
import destiny.core.calendar.TimeTools.toGmtJulDay
import destiny.core.calendar.absDuration
import destiny.core.calendar.locationOf
import mu.KotlinLogging
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TransitTest {
  val loc = locationOf(Locale.TAIWAN)

  val julDayResolver = JulDayResolver1582CutoverImpl()

  val logger = KotlinLogging.logger { }

  @Test
  fun testDirectTransit_convergent() {
    val natal = LocalDateTime.of(1970, 1, 1, 0, 0).toGmtJulDay(loc)
    val transit = Transit(true)
    val nowTime = LocalDateTime.of(2022, 8, 6, 0, 0).toGmtJulDay(loc)
    transit.getConvergentTime(natal, nowTime).also { result ->
      assertTrue { result > natal }
      assertEquals(nowTime, result)
    }
  }

  @Test
  fun testConverseTransit_convergent() {
    val natal = LocalDateTime.of(1970, 1, 1, 0, 0).toGmtJulDay(loc)
    val transit = Transit(false)
    val nowTime = LocalDateTime.of(2022, 8, 6, 0, 0).toGmtJulDay(loc)
    transit.getConvergentTime(natal, nowTime).also { result ->
      assertTrue { result < natal }
      val dur1 = natal.absDuration(nowTime)
      val dur2 = result.absDuration(natal)
      assertEquals(dur1, dur2)
    }
  }

  @Test
  fun testDirectTransit_divergent() {
    val natal = LocalDateTime.of(1970, 1, 1, 0, 0).toGmtJulDay(loc)
    val transit = Transit(true)
    val nowTime = LocalDateTime.of(2022, 8, 6, 0, 0).toGmtJulDay(loc)
    transit.getDivergentTime(natal, nowTime).also { result ->
      assertTrue { result > natal }
      assertEquals(nowTime, result)
    }
  }

  @Test
  fun testConverseTransit_divergent() {
    val natal = LocalDateTime.of(1970, 1, 1, 0, 0).toGmtJulDay(loc)
    val transit = Transit(false)
    val nowTime = LocalDateTime.of(2022, 8, 6, 0, 0).toGmtJulDay(loc)
    transit.getDivergentTime(natal, nowTime).also { result ->

      assertTrue { result < natal }
      val dur1 = natal.absDuration(nowTime)
      val dur2 = result.absDuration(natal)
      assertEquals(dur1, dur2)
    }
  }
}
