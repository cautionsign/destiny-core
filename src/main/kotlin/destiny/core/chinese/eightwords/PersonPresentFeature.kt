/**
 * Created by smallufo on 2021-08-28.
 */
package destiny.core.chinese.eightwords

import destiny.core.Gender
import destiny.core.calendar.GmtJulDay
import destiny.core.calendar.ILocation
import destiny.core.calendar.JulDayResolver
import destiny.core.calendar.chinese.ChineseDateFeature
import destiny.core.calendar.eightwords.IEightWordsPersonConfig
import destiny.core.calendar.eightwords.IPersonPresentConfig
import destiny.core.calendar.eightwords.YearFeature
import destiny.core.chinese.IStemBranch
import destiny.core.chinese.StemBranch
import destiny.tools.AbstractCachedPersonFeature
import destiny.tools.Builder
import destiny.tools.DestinyMarker
import jakarta.inject.Named
import kotlinx.serialization.Serializable
import javax.cache.Cache

@Serializable
data class PersonPresentConfig(val personContextConfig: EightWordsPersonConfig = EightWordsPersonConfig(),
                               override var viewGmt: GmtJulDay = GmtJulDay.nowCeilingToNoon()) : IPersonPresentConfig,
                                                                                                 IEightWordsPersonConfig by personContextConfig

context(IEightWordsPersonConfig)
@DestinyMarker
class PersonPresentConfigBuilder : Builder<PersonPresentConfig> {

  /**
   * 內定讓 viewGmt 取整數 GmtJulDay+1 (明日) , 因此，接連兩次 ewPersonPresent{} , 應該會出現相同的 config 物件
   * 增加 cache 效率
   */
  var viewGmt: GmtJulDay = GmtJulDay.nowCeilingToNoon()

  override fun build(): PersonPresentConfig {
    return PersonPresentConfig(ewPersonConfig, viewGmt)
  }

  companion object {
    context(IEightWordsPersonConfig)
    fun ewPersonPresent(block: PersonPresentConfigBuilder.() -> Unit = {}) : PersonPresentConfig {
      return PersonPresentConfigBuilder().apply(block).build()
    }
  }
}


@Named
class PersonPresentFeature(private val personContextFeature: PersonContextFeature,
                           private val personLargeFeature: IFortuneLargeFeature,
                           private val yearFeature: YearFeature,
                           private val chineseDateFeature: ChineseDateFeature,
                           private val julDayResolver: JulDayResolver,
                           @Transient
                           private val ewPersonPresentFeatureCache: Cache<GmtCacheKey<*>, IPersonPresentModel>) : AbstractCachedPersonFeature<IPersonPresentConfig , IPersonPresentModel>() {

  override val key: String = "personPresent"

  override val defaultConfig: IPersonPresentConfig = PersonPresentConfig()

  @Suppress("UNCHECKED_CAST")
  override val gmtPersonCache: Cache<GmtCacheKey<IPersonPresentConfig>, IPersonPresentModel>
    get() = ewPersonPresentFeatureCache as Cache<GmtCacheKey<IPersonPresentConfig>, IPersonPresentModel>

  override fun calculate(gmtJulDay: GmtJulDay, loc: ILocation, gender: Gender, name: String?, place: String?, config: IPersonPresentConfig): IPersonPresentModel {

    val viewGmtTime = julDayResolver.getLocalDateTime(config.viewGmt)

    val viewChineseDate = chineseDateFeature.getModel(config.viewGmt, loc)

    val pcm: IPersonContextModel = personContextFeature.getPersonModel(gmtJulDay, loc, gender, name, place, config.ewPersonConfig)
    // 目前所處的大運
    val selectedFortuneLarge: IStemBranch? = personLargeFeature.getStemBranch(gmtJulDay, loc, gender, config.viewGmt, config.fortuneLargeConfig)

    // 選定的十年流年
    val selectedFortuneLargeYears: List<StemBranch> = pcm.fortuneDataLarges.firstOrNull { it.stemBranch == selectedFortuneLarge }?.let { fortuneData ->
      generateSequence(yearFeature.getModel(fortuneData.startFortuneGmtJulDay, loc)) {
        it.next(1)
      }.take(10).toList()
    }?: emptyList()

    // 當年流年
    val presentYear: StemBranch = yearFeature.getModel(viewGmtTime, loc)

    return PersonPresentModel(pcm, viewGmtTime, viewChineseDate, selectedFortuneLarge, selectedFortuneLargeYears, presentYear)
  }

  companion object {
    const val CACHE_EIGHTWORDS_PERSON_PRESENT_FEATURE = "ewPersonPresentFeatureCache"
  }
}
