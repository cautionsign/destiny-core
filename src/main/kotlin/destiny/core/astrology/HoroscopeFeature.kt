/**
 * Created by smallufo on 2021-08-15.
 */
package destiny.core.astrology

import destiny.core.astrology.classical.IVoidCourseFeature
import destiny.core.astrology.classical.VoidCourseConfig
import destiny.core.astrology.classical.rules.Misc
import destiny.core.astrology.prediction.*
import destiny.core.calendar.GmtJulDay
import destiny.core.calendar.ILocation
import destiny.core.calendar.JulDayResolver1582CutoverImpl
import destiny.tools.AbstractCachedFeature
import destiny.tools.Feature
import jakarta.inject.Named
import mu.KotlinLogging
import javax.cache.Cache


interface IHoroscopeFeature : Feature<IHoroscopeConfig, IHoroscopeModel> {

  /**
   * secondary progression calculation
   */
  fun getSecondaryProgression(
    model: IHoroscopeModel, progressionTime: GmtJulDay, aspects: Set<Aspect>,
    aspectsCalculator: IAspectsCalculator, config: IHoroscopeConfig, converse: Boolean = false
  ): IProgressionModel {
    val progression = ProgressionSecondary(converse)

    return getProgression(progression, model, progressionTime, aspects, aspectsCalculator, config)
  }

  /**
   * Tertiary Progression calculation
   */
  fun getTertiaryProgression(
    model: IHoroscopeModel, progressionTime: GmtJulDay, aspects: Set<Aspect>,
    aspectsCalculator: IAspectsCalculator, config: IHoroscopeConfig, converse: Boolean = false
  ): IProgressionModel {
    val progression = ProgressionTertiary(converse)

    return getProgression(progression, model, progressionTime, aspects, aspectsCalculator, config)
  }

  /**
   * Minor Progression calculation
   */
  fun getMinorProgression(
    model: IHoroscopeModel, progressionTime: GmtJulDay, aspects: Set<Aspect>,
    aspectsCalculator: IAspectsCalculator, config: IHoroscopeConfig, converse: Boolean = false
  ): IProgressionModel {
    val progression = ProgressionMinor(converse)

    return getProgression(progression, model, progressionTime, aspects, aspectsCalculator, config)
  }

  fun transit(
    model: IHoroscopeModel, transitTime: GmtJulDay, aspects: Set<Aspect>,
    aspectsCalculator: IAspectsCalculator, config: IHoroscopeConfig, converse: Boolean = false
  ): IProgressionModel {
    val progression = Transit(converse)
    return getProgression(progression, model, transitTime, aspects, aspectsCalculator, config)
  }

  fun getProgression(
    progression: AbstractProgression, model: IHoroscopeModel, progressionTime: GmtJulDay, aspects: Set<Aspect>,
    aspectsCalculator: IAspectsCalculator, config: IHoroscopeConfig
  ): IProgressionModel {

    // inner : natal chart
    val posMapInner = model.positionMap

    return progression.getConvergentTime(model.gmtJulDay, progressionTime).let { convergentTime ->

      logger.trace { "convergentTime = $convergentTime" }

      logger.info { "convergentGmt = ${JulDayResolver1582CutoverImpl().getLocalDateTime(convergentTime)}" }

      val convergentModel = getModel(convergentTime, model.location, config)

      // outer : progression chart
      val posMapOuter = convergentModel.positionMap

      // 2.4 hours later
      val later = progressionTime.plus(0.1)
      progression.getConvergentTime(model.gmtJulDay, later).let { laterConvergentTime ->
        val laterModel = getModel(laterConvergentTime, model.location, config)
        val posMapLater = laterModel.positionMap


        val progressedAspects = config.points.asSequence().flatMap { p1 -> config.points.asSequence().map { p2 -> p1 to p2 } }
          .mapNotNull { (p1, p2) ->
            aspectsCalculator.getAspectPatterns(p1, p2, posMapOuter, posMapInner, { posMapLater[p1] }, { posMapInner[p2] }, aspects)?.let { p: IPointAspectPattern ->
              ProgressedAspect(p1, p2, p.aspect, p.orb, p.type!!, p.score)
            }
          }.toSet()

        ProgressionModel(progression.type, model.gmtJulDay, progressionTime, convergentTime, progressedAspects)
      }
    }
  }


  companion object {
    private val logger = KotlinLogging.logger { }
  }
}

@Named
class HoroscopeFeature(
  private val pointPosFuncMap: Map<AstroPoint, IPosition<*>>,
  private val houseCuspFeature: IHouseCuspFeature,
  private val voidCourseFeature: IVoidCourseFeature,
  private val planetHourFeature: Feature<PlanetaryHourConfig, PlanetaryHour?>,
  @Transient
  private val horoscopeFeatureCache: Cache<GmtCacheKey<*>, IHoroscopeModel>
) : AbstractCachedFeature<IHoroscopeConfig, IHoroscopeModel>(), IHoroscopeFeature {
  override val key: String = "horoscope"

  override val defaultConfig: HoroscopeConfig = HoroscopeConfig()

  @Suppress("UNCHECKED_CAST")
  override val gmtCache: Cache<GmtCacheKey<IHoroscopeConfig>, IHoroscopeModel>
    get() = horoscopeFeatureCache as Cache<GmtCacheKey<IHoroscopeConfig>, IHoroscopeModel>

  override fun calculate(gmtJulDay: GmtJulDay, loc: ILocation, config: IHoroscopeConfig): IHoroscopeModel {

    val positionMap: Map<AstroPoint, IPosWithAzimuth> = config.points.map { point ->
      point to pointPosFuncMap[point]?.getPosition(gmtJulDay, loc, config.centric, config.coordinate, config.temperature, config.pressure)
    }.filter { (_, v) -> v != null }.associate { (point, pos) -> point to pos!! as IPosWithAzimuth }


    // [1] 到 [12] 宮首黃道度數
    val cuspDegreeMap: Map<Int, ZodiacDegree> = houseCuspFeature.getModel(gmtJulDay, loc, HouseConfig(config.houseSystem, config.coordinate))

    // 行星空亡表
    val vocMap: Map<Planet, Misc.VoidCourseSpan> = voidCourseFeature.getVocMap(gmtJulDay, loc, config.points, VoidCourseConfig(vocImpl = config.vocImpl))

    // 行星時 Planetary Hour
    val planetaryHour =
      planetHourFeature.getModel(gmtJulDay, loc, PlanetaryHourConfig(PlanetaryHourType.ASTRO, TransConfig(temperature = config.temperature, pressure = config.pressure)))

    return HoroscopeModel(gmtJulDay, loc, config, positionMap, cuspDegreeMap, vocMap, planetaryHour)
  }

  companion object {
    const val CACHE_HOROSCOPE_FEATURE = "horoscopeFeatureCache"
  }
}
