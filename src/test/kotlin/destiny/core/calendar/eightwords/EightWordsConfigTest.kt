/**
 * Created by smallufo on 2021-08-11.
 */
package destiny.core.calendar.eightwords

import com.jayway.jsonpath.JsonPath
import destiny.core.AbstractConfigTest
import destiny.core.astrology.TransConfig
import destiny.core.astrology.TransConfigBuilder.Companion.trans
import destiny.core.calendar.eightwords.DayConfigBuilder.Companion.dayConfig
import destiny.core.calendar.eightwords.DayHourConfigBuilder.Companion.dayHour
import destiny.core.calendar.eightwords.EightWordsConfigBuilder.Companion.ewConfig
import destiny.core.calendar.eightwords.HourBranchConfigBuilder.Companion.hourBranchConfig
import destiny.core.calendar.eightwords.MonthConfigBuilder.Companion.monthConfig
import destiny.core.calendar.eightwords.YearConfigBuilder.Companion.yearConfig
import destiny.core.calendar.eightwords.YearMonthConfigBuilder.Companion.yearMonthConfig
import kotlinx.serialization.KSerializer
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EightWordsConfigTest : AbstractConfigTest<EightWordsConfig>() {

  override val serializer: KSerializer<EightWordsConfig> = EightWordsConfig.serializer()

  override val configByConstructor: EightWordsConfig = EightWordsConfig(
    yearMonthConfig = YearMonthConfig(
      YearConfig(270.0),
      MonthConfig(
        southernHemisphereOpposition = true,
        hemisphereBy = HemisphereBy.DECLINATION,
        monthImpl = MonthImpl.SunSign
      )
    ),
    dayHourConfig = DayHourConfig(
      DayConfig(changeDayAfterZi = false, midnight = MidnightImpl.CLOCK0),
      HourBranchConfig(
        hourImpl = HourImpl.LMT,
        transConfig = TransConfig(
          discCenter = true,
          refraction = false,
          temperature = 23.0,
          pressure = 1000.0
        )
      )
    )
  )

  override val configByFunction: EightWordsConfig
    get() {

      val yearConfig = yearConfig {
        changeYearDegree = 270.0
      }
      val monthConfig = monthConfig {
        southernHemisphereOpposition = true
        hemisphereBy = HemisphereBy.DECLINATION
        monthImpl = MonthImpl.SunSign
      }

      val yearMonthConfig = with(yearConfig) {
        with(monthConfig) {
          yearMonthConfig {
          }
        }
      }


      val transConfig = trans {
        discCenter = true
        refraction = false
        temperature = 23.0
        pressure = 1000.0
      }

      val hourBranchConfig = with(transConfig) {
        hourBranchConfig {
          hourImpl = HourImpl.LMT
        }
      }

      val dayConfig = dayConfig {
        changeDayAfterZi = false
        midnight = MidnightImpl.CLOCK0
      }


      val dayHourConfig = with(dayConfig) {
        with(hourBranchConfig) {
          dayHour {
          }
        }
      }

      return with(yearMonthConfig) {
        with(dayHourConfig) {
          ewConfig {
          }
        }
      }
    }

  override val assertion = { raw: String ->
    val docCtx = JsonPath.parse(raw)
    assertEquals(270.0 , docCtx.read("$.yearMonthConfig.yearConfig.changeYearDegree", Double::class.java))
    assertEquals(true , docCtx.read("$.yearMonthConfig.monthConfig.southernHemisphereOpposition", Boolean::class.java))
    assertEquals("DECLINATION", docCtx.read("$.yearMonthConfig.monthConfig.hemisphereBy", String::class.java))
    assertEquals("SunSign", docCtx.read("$.yearMonthConfig.monthConfig.monthImpl", String::class.java))
    assertFalse(docCtx.read("$.dayHourConfig.dayConfig.changeDayAfterZi", Boolean::class.java))
    assertEquals("CLOCK0", docCtx.read("$.dayHourConfig.dayConfig.midnight", String::class.java))
    assertEquals("LMT", docCtx.read("$.dayHourConfig.hourBranchConfig.hourImpl", String::class.java))

    assertTrue(docCtx.read("$.dayHourConfig.hourBranchConfig.transConfig.discCenter", Boolean::class.java))
    assertFalse(docCtx.read("$.dayHourConfig.hourBranchConfig.transConfig.refraction", Boolean::class.java))
    assertEquals(23.0, docCtx.read("$.dayHourConfig.hourBranchConfig.transConfig.temperature", Double::class.java))
    assertEquals(1000.0, docCtx.read("$.dayHourConfig.hourBranchConfig.transConfig.pressure", Double::class.java))
  }
}
