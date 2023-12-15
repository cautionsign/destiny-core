/**
 * Created by smallufo on 2021-11-01.
 */
package destiny.core.astrology.classical

import destiny.core.astrology.HoroscopeClassicalConfig
import destiny.core.astrology.HoroscopeConfig
import destiny.core.astrology.HoroscopeFeature
import destiny.core.astrology.Planet
import destiny.core.astrology.classical.rules.ClassicalPatternContext
import destiny.core.astrology.classical.rules.IPlanetPattern
import destiny.core.astrology.classical.rules.IPlanetPatternFactory
import destiny.core.calendar.GmtJulDay
import destiny.core.calendar.ILocation
import destiny.tools.AbstractCachedFeature
import jakarta.inject.Named
import javax.cache.Cache


@Named
class ClassicalFeature(private val horoscopeFeature: HoroscopeFeature,
                       classicalPatternContext: ClassicalPatternContext,
                       @Transient
                       private val classicalRulesCache: Cache<GmtCacheKey<*>, Map<*,*>>) : AbstractCachedFeature<HoroscopeClassicalConfig, Map<Planet, List<IPlanetPattern>>>() {

  override val defaultConfig: HoroscopeClassicalConfig = HoroscopeClassicalConfig(
    horoConfig = HoroscopeConfig() ,
    factories = classicalPatternContext.let {
      it.essentialDignities.plus(it.accidentalDignities).plus(it.debilities)
    }
  )

  @Suppress("UNCHECKED_CAST")
  override val gmtCache: Cache<GmtCacheKey<HoroscopeClassicalConfig>, Map<Planet, List<IPlanetPattern>>>
    get() = classicalRulesCache as Cache<GmtCacheKey<HoroscopeClassicalConfig>, Map<Planet, List<IPlanetPattern>>>

  override fun calculate(gmtJulDay: GmtJulDay, loc: ILocation, config: HoroscopeClassicalConfig): Map<Planet, List<IPlanetPattern>> {

    val h = horoscopeFeature.getModel(gmtJulDay, loc, config.horoConfig)

    val factories: List<IPlanetPatternFactory> = config.factories


    return Planet.classicalList.associateWith { planet ->
      val list = factories.flatMap { factory ->
        factory.getPatterns(planet, h)
      }

      list
    }
  }

  companion object {
    const val CACHE_CLASSICAL_RULES = "classicalRulesCache"
  }
}
