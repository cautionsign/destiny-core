/**
 * Created by smallufo on 2017-12-27.
 */
package destiny.core.astrology

import destiny.core.IPoints
import destiny.core.Point
import destiny.core.toString
import destiny.tools.serializers.PlanetSerializer
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.reflect.KClass

@Serializable(with = PlanetSerializer::class)
sealed class Planet(nameKey: String,
                    abbrKey: String,
                    unicode: Char) : Star(nameKey, abbrKey, Star::class.qualifiedName!! , unicode) , Comparable<Planet> {

  object SUN     : Planet("Planet.SUN"    , "Planet.SUN_ABBR" , '☉')
  object MOON    : Planet("Planet.MOON", "Planet.MOON_ABBR", '☽')
  object MERCURY : Planet("Planet.MERCURY", "Planet.MERCURY_ABBR", '☿')
  object VENUS   : Planet("Planet.VENUS", "Planet.VENUS_ABBR", '♀')
  object MARS    : Planet("Planet.MARS", "Planet.MARS_ABBR", '♂')
  object JUPITER : Planet("Planet.JUPITER", "Planet.JUPITER_ABBR", '♃')
  object SATURN  : Planet("Planet.SATURN", "Planet.SATURN_ABBR", '♄')
  object URANUS  : Planet("Planet.URANUS", "Planet.URANUS_ABBR", '♅')
  object NEPTUNE : Planet("Planet.NEPTUNE", "Planet.NEPTUNE_ABBR", '♆')
  object PLUTO   : Planet("Planet.PLUTO", "Planet.PLUTO_ABBR", '♇')

  override fun compareTo(other: Planet): Int {
    if (this === other)
      return 0
    return values.indexOf(this) - values.indexOf(other)
  }

  companion object : IPoints<Planet> {

    override val type: KClass<out Point> = Planet::class

    val classicalArray by lazy { arrayOf(SUN, MOON, MERCURY, VENUS, MARS, JUPITER, SATURN) }
    val classicalList by lazy { listOf(*classicalArray) }
    val classicalSet by lazy { setOf(*classicalArray) }

    override val values by lazy { arrayOf(*classicalArray, URANUS, NEPTUNE, PLUTO) }
    val list by lazy { listOf(*values) }
    val planetSet by lazy { setOf(*values) }

    private val weekPlanets by lazy {
      arrayOf(SUN, MOON, MARS, MERCURY, JUPITER, VENUS, SATURN)
    }

    fun Planet.aheadOf(planet: Planet): Int {
      require(weekPlanets.contains(this))
      require(weekPlanets.contains(planet))
      return (this.weekIndex() - planet.weekIndex()).let {
        if (it < 0)
          it + 7
        else
          it
      }
    }

    private fun Planet.weekIndex() = weekPlanets.indexOf(this)

    override fun fromString(value: String, locale: Locale): Planet? {
      return values.firstOrNull {
        it.toString(locale).equals(value, ignoreCase = true)
      }
    }
  }
}
