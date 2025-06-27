/**
 * Created by smallufo on 2017-12-22.
 */
package destiny.core.astrology.classical

import destiny.core.DayNight
import destiny.core.astrology.Element.*
import destiny.core.astrology.Planet
import destiny.core.astrology.Planet.*
import destiny.core.astrology.ZodiacSign
import java.io.Serializable

/**
 * Dorotheus Triplicity 實作 , Dorotheus of Sidon
 * 參考表格 https://imgur.com/zHWZQ8L
 * 來自 https://altairastrology.wordpress.com/2008/04/18/a-closer-look-at-triplicity/
 *
 *
 * https://tonylouis.wordpress.com/2017/03/16/the-logic-behind-triplicity-rulers/
 *
 * Dorotheus uses the domicile rulers of the fixed signs
 * as well as the exaltations of the planets in his system of triplicity rulers.
 * The sect of the planets is also of paramount importance.
 *
 * it appears that either Dorotheus or his Arabic translators considered
 * the idea of Mercury as a triplicity ruler of the earth signs.
 * 土象 的 ruler 也可考慮水星！
 *
 * <pre>
 *     | 白天        | 夜晚 | 共管
 * ------------------------------
 * 火象 | 太陽        | 木星 | 土星
 * 土象 | 金星 (or 水)| 月亮 | 火星
 * 風象 | 土星        | 水星 | 木星
 * 水象 | 金星        | 火星 | 月亮
 * </pre>
 * 此表格與維基百科一致
 *
 *
 */
object TriplicityDorotheusImpl : ITriplicity, Serializable {
  private fun readResolve(): Any = TriplicityDorotheusImpl

  /** 哪顆星在此星座得到三分相 (+3) */
  override fun ZodiacSign.getTriplicityPoint(dayNight: DayNight): Planet {
    return when (dayNight) {
      DayNight.DAY -> when(this.element) {
        FIRE -> SUN
        EARTH -> VENUS
        AIR -> SATURN
        WATER -> VENUS
      }
      DayNight.NIGHT -> when(this.element) {
        FIRE -> JUPITER
        EARTH -> MOON
        AIR -> MERCURY
        WATER -> MARS
      }
    }
  }

  override fun ZodiacSign.getPartner(): Planet {
    return when (this.element) {
      FIRE -> SATURN
      EARTH -> MARS
      AIR -> JUPITER
      WATER -> MOON
    }
  }
}
