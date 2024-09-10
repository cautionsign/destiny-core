/**
 * Created by smallufo on 2021-10-15.
 */
package destiny.core.chinese.ziwei

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import destiny.core.DayNight
import destiny.core.Gender
import destiny.core.IIntAge
import destiny.core.astrology.*
import destiny.core.calendar.*
import destiny.core.calendar.TimeTools.toGmtJulDay
import destiny.core.calendar.chinese.ChineseDate
import destiny.core.calendar.chinese.IChineseDateFeature
import destiny.core.calendar.chinese.IFinalMonthNumber
import destiny.core.calendar.chinese.MonthAlgo
import destiny.core.calendar.eightwords.*
import destiny.core.chinese.*
import destiny.core.toString
import destiny.tools.AbstractCachedPersonFeature
import destiny.tools.CacheGrain
import destiny.tools.PersonFeature
import destiny.tools.getTitle
import jakarta.inject.Inject
import jakarta.inject.Named
import destiny.tools.KotlinLogging
import java.text.MessageFormat
import java.time.LocalTime
import java.time.chrono.ChronoLocalDate
import java.time.chrono.ChronoLocalDateTime
import java.util.*
import javax.cache.Cache


/** 某時刻對應到的 大運、流年 兩資訊 */
data class Flow(val section: StemBranch?, val year: StemBranch) : java.io.Serializable


interface IZiweiFeature : PersonFeature<IZiweiConfig, IPlate> {

  /** 取命宮、身宮地支  */
  fun getMainBodyHouse(lmt: ChronoLocalDateTime<*>, loc: ILocation, config: IZiweiConfig): Triple<Branch, Branch, Int?>

  /**
   * @param flowType  在[本命、大限、流年]... (之一)
   * @param flowStem  天干為
   * @return 傳回四化 (若有的話)
   */
  fun getTrans4Map(flowType: FlowType, flowStem: Stem, config: IZiweiConfig): Map<Pair<ZStar, FlowType>, T4Value>

  /**
   * 本命盤
   *
   * @param mainAndBody 預先計算過的命宮、身宮地支
   * @param preFinalMonthNumForMainStars 預先計算好的「最終月數」 for 14顆主星 ([StarMain]])
   * @param cycle              陰曆循環 : TODO : // 很多時候是沒有 cycle 的，可能得改為 [Int?] , 但又會影響許多後續計算 (例如 vageMap)
   * @param lunarYear          陰曆的年干支
   * @param solarYear          「節氣」的年干支
   * @param monthBranch        「節氣」的月支
   * @param lunarDays          陰曆日期
   * @param optionalVageMap    預先計算好的虛歲時刻(GMT from / to)
   * @param optionalRageMap    預先計算好的實歲時刻(GMT from / to)
   */
  fun getBirthPlate(
    mainAndBody: Pair<Branch, Branch>?,
    preFinalMonthNumForMainStars: Int?,
    cycle: Int,
    lunarYear: StemBranch,
    solarYear: StemBranch,
    lunarMonth: Int,
    leapMonth: Boolean,
    monthBranch: Branch,
    solarTerms: SolarTerms,
    lunarDays: Int,
    hour: Branch,
    dayNight: DayNight,
    gender: Gender,
    optionalVageMap: Map<Int, Pair<GmtJulDay, GmtJulDay>>? = null,
    optionalRageMap: Map<Int, Pair<GmtJulDay, GmtJulDay>>? = null,
    appendingNotes: List<String> = emptyList(),
    config: IZiweiConfig
  ): IPlate

  fun getModernBirthPlate(
    name: String?,
    lmt: ChronoLocalDateTime<*>?,
    loc: ILocation,
    place: String?,
    mainAndBody: Pair<Branch, Branch>?,
    preFinalMonthNumForMainStars: Int?,
    cycle: Int,
    lunarYear: StemBranch,
    solarYear: StemBranch,
    lunarMonth: Int,
    leapMonth: Boolean,
    monthBranch: Branch,
    solarTerms: SolarTerms,
    lunarDays: Int,
    hour: Branch,
    dayNight: DayNight,
    gender: Gender,
    optionalVageMap: Map<Int, Pair<GmtJulDay, GmtJulDay>>? = null,
    optionalRageMap: Map<Int, Pair<GmtJulDay, GmtJulDay>>? = null,
    appendingNotes: List<String> = emptyList(),
    config: IZiweiConfig
  ): IPlate

  /** 計算 大限盤  */
  fun getFlowSection(plate: IPlate, section: StemBranch, config: IZiweiConfig): IPlateSection

  /** 計算 流年盤 */
  fun getFlowYear(plate: IPlate, section: StemBranch, flowYear: StemBranch, config: IZiweiConfig): IPlateYear

  /** 計算 流月盤 */
  fun getFlowMonth(plate: IPlate, section: StemBranch, flowYear: StemBranch, flowMonth: StemBranch, config: IZiweiConfig): IPlateMonth

  /** 計算 流日盤 */
  fun getFlowDay(plate: IPlate, section: StemBranch, flowYear: StemBranch, flowMonth: StemBranch, flowDay: StemBranch, flowDayNum: Int, config: IZiweiConfig): IPlateDay

  /** 計算 流時盤 */
  fun getFlowHour(plate: IPlate, section: StemBranch, flowYear: StemBranch, flowMonth: StemBranch, flowDay: StemBranch, flowDayNum: Int, flowHour: StemBranch, config: IZiweiConfig): IPlateHour

  /** 反推大限、流年兩資訊 */
  fun reverseFlows(plate: IPlate, viewGmt: GmtJulDay, config: IZiweiConfig): Flow?
  fun reverseFlows(plate: IPlate, lmt: ChronoLocalDateTime<*>, config: IZiweiConfig): Flow? {
    return plate.location?.let { loc ->
      val viewGmt = lmt.toGmtJulDay(loc)
      reverseFlows(plate , viewGmt, config)
    }
  }

  /** 計算大限盤 */
  fun getFlowSection(plate: IPlate, lmt: ChronoLocalDateTime<*>, config: IZiweiConfig): IPlateSection? {
    return reverseFlows(plate, lmt, config)?.section?.let {
      getFlowSection(plate, it, config)
    }
  }

  /**
   * 列出到當下所經過的大限盤 (unfinished inclusive)
   */
  fun getFlowSections(plate: IPlate, lmt: ChronoLocalDateTime<*>, config: IZiweiConfig): List<IPlate> {
    return reverseFlows(plate, lmt, config)?.section?.let { section ->

      val sections = plate.flowSectionAgeMap.keys.asSequence().takeWhile { it != section }.toList().toMutableList().apply {
        add(section)
      }

      sections.map { getFlowSection(plate, it, config) }.toList()
    } ?: emptyList()
  }

  /** 計算大限、流年盤 */
  fun getFlowYear(plate: IPlate, lmt: ChronoLocalDateTime<*>, config: IZiweiConfig): IPlate? {
    return reverseFlows(plate, lmt, config)?.let { flow ->
      flow.section?.let { section ->
        getFlowYear(plate, section, flow.year, config)
      }
    }
  }

  /**
   * @param cycle     cycle
   * @param flowYear  流年
   * @param flowMonth 流月
   * @param leap      是否閏月
   * @return 該流月的日子 (陰曆＋陽曆＋干支）
   */
  fun getDaysOfMonth(cycle: Int, flowYear: StemBranch, flowMonth: Int, leap: Boolean): List<Triple<ChineseDate, ChronoLocalDate, StemBranch>>

  /** 列出此大限中，包含哪十個流年 (陰曆 cycle + 地支干支) , 並且「歲數」各別是幾歲 */
  fun getYearsOfFlowSection(plate: IPlate, section: Branch, config: IZiweiConfig): List<Triple<Int, StemBranch, Int>>
}

@Named
class ZiweiFeature(
  private val julDayResolver: JulDayResolver,
  @Named("houseCuspImpl")
  private val risingSignImpl: IRisingSign,
  private val starPositionImpl: IStarPosition<*>,
  private val chineseDateFeature: IChineseDateFeature,
  private val yearFeature: YearFeature,
  private val yearMonthFeature: YearMonthFeature,
  private val dayHourFeature: DayHourFeature,
  private val hourBranchFeature: IHourBranchFeature,
  private val houseSeqImplMap: Map<HouseSeq, IHouseSeq>,
  private val prevMonthDaysImpl: IPrevMonthDays,
  private val purpleStarBranchImplMap : Map<PurpleStarBranch, IPurpleStarBranch>,
  private val tianyiImplMap: Map<Tianyi, ITianyi>,
  private val transFourImplMap: Map<TransFour, ITransFour>,
  private val strengthImplMap: Map<Strength, IStrength>,
  private val flowSectionImplMap: Map<BigRange, IFlowSection>,
  private val flowYearImplMap: Map<FlowYear, IFlowYear>,
  private val flowMonthImplMap: Map<FlowMonth, IFlowMonth>,
  private val flowDayImplMap: Map<FlowDay, IFlowDay>,
  private val flowHourImplMap: Map<FlowHour, IFlowHour>,
  private val dayNightFeature: DayNightFeature,

  @Named("intVageZiweiImpl")
  private val intVageImpl: IIntAge,

  @Named("intRageZiweiImpl")
  private val intRageImpl: IIntAge,

  private val purpleRelocationMutator : IHouseMutator
) : AbstractCachedPersonFeature<IZiweiConfig, IPlate>(), IZiweiFeature {

  @Inject
  @Transient
  private lateinit var ziweiCache: Cache<LmtCacheKey<*>, IPlate>

  override val lmtPersonCache: Cache<LmtCacheKey<IZiweiConfig>, IPlate>
    get() = ziweiCache as Cache<LmtCacheKey<IZiweiConfig>, IPlate>

  override val defaultConfig: IZiweiConfig = ZiweiConfig()

  override var lmtCacheGrain: CacheGrain? = CacheGrain.MINUTE

  override fun calculate(gmtJulDay: GmtJulDay, loc: ILocation, gender: Gender, name: String?, place: String?, config: IZiweiConfig): IPlate {
    val lmt = TimeTools.getLmtFromGmt(gmtJulDay, loc, julDayResolver)
    return getPersonModel(lmt, loc, gender, name, place, config)
  }

  /**
   * @return 命宮、身宮 、以及「最後要給主星所使用的月數 (若為占星算法，此值為空) 」
   *  */
  override fun getMainBodyHouse(lmt: ChronoLocalDateTime<*>, loc: ILocation, config: IZiweiConfig): Triple<Branch, Branch, Int?> {
    return when (config.mainBodyHouse) {
      MainBodyHouse.Trad  -> {
        val cDate: ChineseDate = chineseDateFeature.getModel(lmt, loc, config.ewConfig.dayHourConfig)

        val monthBranch = yearMonthFeature.getModel(lmt, loc, config.ewConfig.yearMonthConfig).branch

        val lunarMonth = cDate.month
        val days = cDate.day

        val hour = dayHourFeature.getModel(lmt, loc, config.ewConfig.dayHourConfig).second.branch


        // 最終要計算的「月份」數字 , for 主星
        val finalMonthNumForMainStars = IFinalMonthNumber.getFinalMonthNumber(lunarMonth, cDate.leapMonth, monthBranch, days, config.mainStarsAlgo)

        val mainHouse = Ziwei.getMainHouseBranch(finalMonthNumForMainStars, hour)
        val bodyHouse = Ziwei.getBodyHouseBranch(finalMonthNumForMainStars, hour)
        logger.trace { "命 = $mainHouse , 身 = $bodyHouse" }

        Triple(mainHouse, bodyHouse, finalMonthNumForMainStars)
      }
      MainBodyHouse.Astro -> {
        /** 利用上升星座，計算命宮
         *  利用月亮星座，計算身宮 */
        val mainHouse = risingSignImpl.getRisingSign(lmt, loc, HouseSystem.PLACIDUS, Coordinate.ECLIPTIC).branch
        val moonPos = starPositionImpl.getPosition(Planet.MOON, lmt, loc, Centric.GEO, Coordinate.ECLIPTIC)

        val zodiacSign = moonPos.lngDeg.sign

        val bodyHouse = zodiacSign.branch

        Triple(mainHouse, bodyHouse, null)
      }
    }
  }

  private fun List<Pair<String, Array<Any>>>.build(locale: Locale): List<String> {
    return this.map { (first: String, second: Array<Any>) ->
      val pattern = ResourceBundle.getBundle(ZiweiFeature::class.java.name, locale).getString(first)
      MessageFormat.format(pattern, *second).also {
        logger.trace("note : {}", it)
      }
    }
  }

  /**
   * 本命盤
   *
   * @param mainAndBody 預先計算過的命宮、身宮地支
   * @param preFinalMonthNumForMainStars 預先計算好的「最終月數」 for 14顆主星 ([StarMain]])
   * @param cycle              陰曆循環 : TODO : // 很多時候是沒有 cycle 的，可能得改為 [Int?] , 但又會影響許多後續計算 (例如 vageMap)
   * @param lunarYear          陰曆的年干支
   * @param solarYear          「節氣」的年干支
   * @param monthBranch        「節氣」的月支
   * @param lunarDays          陰曆日期
   * @param optionalVageMap    預先計算好的虛歲時刻(GMT from / to)
   */
  override fun getBirthPlate(
    mainAndBody: Pair<Branch, Branch>?,
    preFinalMonthNumForMainStars: Int?,
    cycle: Int,
    lunarYear: StemBranch,
    solarYear: StemBranch,
    lunarMonth: Int,
    leapMonth: Boolean,
    monthBranch: Branch,
    solarTerms: SolarTerms,
    lunarDays: Int,
    hour: Branch,
    dayNight: DayNight,
    gender: Gender,
    optionalVageMap: Map<Int, Pair<GmtJulDay, GmtJulDay>>?,
    optionalRageMap: Map<Int, Pair<GmtJulDay, GmtJulDay>>?,
    appendingNotes: List<String>,
    config: IZiweiConfig
  ): IPlate {

    // 排盤之中所產生的註解 , Pair<KEY , parameters>
    val notesBuilders = mutableListOf<Pair<String, Array<Any>>>()

    /**
     * 14主星的「月數」 [IZiweiConfig.mainStarsAlgo]
     * 與「月系星」的月數 [IZiweiConfig.monthStarsAlgo]
     * 可以分開設定 , 故，這裡產生兩個 finalMonthNum
     */

    /**
     * 最終要計算的「月份」數字 , for 主星 .
     * 若已經有了值（通常來自 傳統計算命宮、身宮的演算法 [MainBodyHouseTradImpl] ) , 代表也一定預先計算了命宮、身宮 [mainAndBody]
     * 若沒有預先計算的值 , 代表其命宮、身宮演算法來自占星 [MainBodyHouseAstroImpl] ,
     *    紫微星的算法，有依據「五行局數」. 而「五行局數」來自命宮. 若命宮採用上升星座，就與「月數」「節氣」無關。
     * */
    val finalMonthNumForMainStars: Int =
      preFinalMonthNumForMainStars ?: IFinalMonthNumber.getFinalMonthNumber(lunarMonth, leapMonth, monthBranch, lunarDays, config.mainStarsAlgo)

    // 最終要計算的「月份」數字 , for 月系星
    val finalMonthNumForMonthStars =
      IFinalMonthNumber.getFinalMonthNumber(lunarMonth, leapMonth, monthBranch, lunarDays, config.monthStarsAlgo)

    logger.trace("finalMonthNumForMainStars = {}", finalMonthNumForMainStars)


    if (config.mainBodyHouse == MainBodyHouse.Trad  // 如果主星是以傳統方式計算
      && lunarMonth != finalMonthNumForMainStars    // 而且最終月份數不一樣
    ) {
      logger.warn(
        "命身宮設定為 : {} , 造成原本月份為 {}{}月，以月數 {} 取代之", config.mainStarsAlgo, if (leapMonth) "閏" else "", lunarMonth,
        finalMonthNumForMainStars
      )
      when (config.mainStarsAlgo) {
        MonthAlgo.MONTH_FIXED_THIS   -> {
          // do nothing
        }
        MonthAlgo.MONTH_SOLAR_TERMS  -> {
          // 命身宮用節氣計算，故用 {0}月={1} 而非 {2}{3}月
          notesBuilders.add(
            Pair(
              "mainStarsAlgo_month_solar_terms",
              arrayOf(
                monthBranch, finalMonthNumForMainStars, if (leapMonth) "閏" else "",
                lunarMonth
              )
            )
          )
        }
        MonthAlgo.MONTH_LEAP_NEXT    -> {
          // 命身宮於閏{0}月視為下月={1}
          notesBuilders.add(Pair("mainStarsAlgo_month_leap_next", arrayOf(lunarMonth, finalMonthNumForMainStars)))
        }
        MonthAlgo.MONTH_LEAP_SPLIT15 -> {
          // 命身宮於閏月月中切割,故用{0}月
          notesBuilders.add(Pair("mainStarsAlgo_month_leap_split15", arrayOf(finalMonthNumForMainStars)))
        }
        else                         -> {}
      }
    }

    if (lunarMonth != finalMonthNumForMonthStars) {
      logger.warn(
        "月系星的設定為 : {} , 造成原本月份為 {}{}月，以月數 {} 取代之", config.monthStarsAlgo, if (leapMonth) "閏" else "", lunarMonth,
        finalMonthNumForMonthStars
      )

      when (config.monthStarsAlgo) {
        MonthAlgo.MONTH_FIXED_THIS   -> {
          // do nothing
        }
        MonthAlgo.MONTH_SOLAR_TERMS  -> {
          // 月系星以節氣計算，故月用 {0}={1} 而非 {2}
          notesBuilders.add(
            Pair(
              "monthStarsAlgo_solar_month",
              arrayOf(
                monthBranch, finalMonthNumForMonthStars, if (leapMonth) "閏" else "",
                lunarMonth
              )
            )
          )
        }
        MonthAlgo.MONTH_LEAP_NEXT    -> {
          // 月系星於閏{0}月視為下月{1}
          notesBuilders.add(
            Pair("monthStarsAlgo_month_leap_next", arrayOf(lunarMonth, finalMonthNumForMonthStars))
          )
        }
        MonthAlgo.MONTH_LEAP_SPLIT15 -> {
          // 月系星於閏月月中切割,故用{0}月
          notesBuilders.add(Pair("monthStarsAlgo_month_leap_split15", arrayOf(finalMonthNumForMonthStars)))
        }
      }
    }

    // 寅 的天干
    val stemOf寅 = Ziwei.getStemOf寅(lunarYear.stem)

    // 命宮所參考的「年干」，同時依據「年系星」的類型來決定
    val year: StemBranch = if (config.yearType == YearType.YEAR_LUNAR) lunarYear else solarYear

    // 命宮地支
    val mainBranch = mainAndBody?.first ?: Ziwei.getMainHouseBranch(finalMonthNumForMainStars, hour)
    val mainHouse = Ziwei.getStemBranchOf(mainBranch, stemOf寅)
    logger.trace("命宮在 : {}", mainHouse)

    // 身宮地支
    val bodyBranch = mainAndBody?.second ?: Ziwei.getBodyHouseBranch(finalMonthNumForMainStars, hour)
    val bodyHouse = Ziwei.getStemBranchOf(bodyBranch, stemOf寅)
    logger.trace("身宮在 : {}", bodyHouse)

    // 取命主 : 命宮所在地支安星
    val mainStar = Ziwei.getMainStar(mainHouse.branch)

    // 取身主 : 以出生年之地支安星
    val bodyStar = Ziwei.getBodyStar(lunarYear.branch)

    // 12宮 順序 以及 名稱
    val houseSeqImpl = houseSeqImplMap[config.houseSeq]!!

    /**
     * 由命宮干支的納音 (NaYin) 來決定 五行+局數(Int) , 例如 水 2 局
     * */
    val (五行, 五行局) = Ziwei.getMainDesc(mainHouse)

    // 干支 -> 宮位 的 mapping
    val stemBranchHouseMap: Map<StemBranch, House> = // 要計算的宮位，比命宮，超前幾步
      houseSeqImpl.houses.associateBy { house ->
        // 要計算的宮位，比命宮，超前幾步
        val steps = houseSeqImpl.getAheadOf(house, House.命宮)
        Ziwei.getStemBranchOf(mainHouse.branch.prev(steps), stemOf寅)
      }

    // 地支 <-> 宮位 的 雙向 mapping
    val branchHouseBiMap: BiMap<Branch, House> = HashBiMap.create()
    stemBranchHouseMap.forEach { (sb, house) -> branchHouseBiMap[sb.branch] = house }

    // 為了某些流派閏月的考量 , 須在此求出「上個月」有幾天 , 才能求出紫微星
    val prevMonthDays = if (leapMonth) prevMonthDaysImpl.getPrevMonthDays(cycle, lunarYear, lunarMonth, true) else 0

    // 什麼星，在什麼地支
    val starBranchMap: Map<ZStar, Branch> = config.stars.map { star ->
      val branch: Branch? = HouseFunctions.map[star]?.getBranch(
        HouseCalContext(
          lunarYear,
          solarYear,
          monthBranch,
          finalMonthNumForMonthStars,
          solarTerms,
          lunarDays,
          hour,
          五行局,
          gender,
          leapMonth,
          prevMonthDays,
          config,
          mainBranch,
          purpleStarBranchImplMap,
          tianyiImplMap
        )
      )
      star to branch
    }
      .filter { (_, branch) -> branch != null }
      .associate { (star, branch) -> star to branch!! }

    logger.debug("stars = {}", config.stars)
    logger.debug("starBranchMap = {}", starBranchMap)

    // 本命四化 : 四化要依據 陰曆初一 還是 節氣立春 劃分

    val trans4Map: Map<Pair<ZStar, FlowType>, T4Value> =
      if (config.yearType == YearType.YEAR_LUNAR) {
        getTrans4Map(FlowType.MAIN, lunarYear.stem, config)
      } else {
        // 立春分年
        getTrans4Map(FlowType.MAIN, solarYear.stem, config).also {
          if (lunarYear !== solarYear) {
            // 如果年 與 陰曆年不同
            // solar_year=年系星立春為界，故年用 {0} 而非 {1}
            notesBuilders.add(Pair("solar_year", arrayOf(solarYear, lunarYear)))
          }
        }
      }
    logger.debug { "transFour = ${config.transFour} , title = ${config.transFour.getTitle(Locale.TAIWAN)}" }
    logger.debug { "trans4Map = $trans4Map" }

    val transFourMap: Map<ZStar, SortedMap<FlowType, T4Value>> = trans4Map.map { (k: Pair<ZStar, FlowType>,v: T4Value) ->
      k.first to sortedMapOf(k.second to v)
    }.toMap()


    // 宮干四化 : 此宮位，因為什麼星，各飛入哪個宮位(地支)
    // 參考 : http://www.fate123.com.tw/fate-teaching/fate-lesson-5.2.asp
    val transFourImpl = transFourImplMap[config.transFour]!!
    val flyMap: Map<StemBranch, Set<Triple<T4Value, ZStar, Branch>>> = stemBranchHouseMap.keys.associateWith { sb: StemBranch ->

      T4Value.entries
        .map { value ->
          val flyStar: ZStar = transFourImpl.getStarOf(sb.stem, value)
          value to flyStar
        }
        .filter { (_, flyStar) ->
          starBranchMap[flyStar] != null
        }
        .map { (value, flyStar) ->
          Triple(value, flyStar, starBranchMap.getValue(flyStar))
        }
        .toSet()
    }

    // 星體強弱表
    val strengthImpl = strengthImplMap[config.strength]!!
    val starStrengthMap: Map<ZStar, Int> = config.stars.map { star ->
      val strength: Int? = strengthImpl.getStrengthOf(star, starBranchMap.getValue(star))
      star to strength
    }
      .filter { it.second != null }
      .associate { it.first to it.second!! }

    val chineseDate = ChineseDate(cycle, lunarYear, lunarMonth, leapMonth, lunarDays)

    // 計算每個地支的 大限 起訖 「歲數」時刻 (不考慮實歲或虛歲)
    val flowSectionImpl = flowSectionImplMap[config.bigRange]!!
    val flowSectionAgeMap = flowSectionImpl.getSortedFlowSectionAgeMap(branchHouseBiMap, 五行局, lunarYear, gender, houseSeqImpl)

    // 小限 mapping
    val branchSmallRangesMap: Map<Branch, List<Int>> = Branch.entries
      .associateWith { branch ->
        ISmallRange.getRanges(branch, lunarYear.branch, gender)
      }

    /**
     * 歲數 map , 2018-06-03 改 optional . 因為不想在 core 內 , depend on ChineseDateCalendricaImpl
     * 不然就得開發另一套非常簡易的 [IIntAge] 在此使用
     */
    val vageMap: Map<Int, Pair<GmtJulDay, GmtJulDay>>? = optionalVageMap
    val rageMap: Map<Int, Pair<GmtJulDay, GmtJulDay>>? = optionalRageMap

    // 中介 map , 記錄 '[辰] : 天相,紫微' 這樣的 mapping , 此 map 的 key 不一定包含全部地支，因為可能有空宮
    val branchStarsMap: Map<Branch, List<ZStar>> =
      starBranchMap.entries.groupBy { it.value }.mapValues { it.value.map { entry -> entry.key } }

    // 哪個地支 裡面 有哪些星體 (可能會有空宮 , 若星體很少的話)
    val branchStarMap: Map<Branch, List<ZStar>?> =
      Branch.entries.associateWith {
        // 可能為 null (空宮)
          branch ->
        branchStarsMap[branch]
      }.toSortedMap()


    /**
     * (branchFlowHouseMap) 儲存類似這樣資料結構 , 12組
     * (子) :
     *  本命 -> 疾厄
     *  大運 -> XX宮
     * (丑) :
     *  本命 -> 財帛
     */
    val branchFlowHouseMap: Map<Branch, Map<FlowType, House>> = stemBranchHouseMap.entries.associate { e ->
      val m = mapOf(FlowType.MAIN to stemBranchHouseMap.getValue(e.key))
      e.key.branch to m
    }.toSortedMap()

    val houseDataSet = stemBranchHouseMap.entries.map { e ->
      val sb = e.key
      val house = e.value
      val stars = branchStarMap[sb.branch]?.toSet() ?: emptySet()

      val fromTo = flowSectionAgeMap.getValue(sb) // 必定不為空
      val smallRanges = branchSmallRangesMap.getValue(sb.branch)
      HouseData(
        house, sb, stars.toMutableSet(), branchFlowHouseMap.getValue(sb.branch), flyMap.getValue(sb), fromTo.first,
        fromTo.second, smallRanges
      )
    }.toSet()

    // 本命盤，不具備流運等資料
    val flowBranchMap = emptyMap<FlowType, StemBranch>()

    val summaries: List<String> = run {

      val starMap = houseDataSet
        .flatMap { hd -> hd.stars.map { star -> star to hd } }
        .toMap()

      val locale = Locale.TAIWAN

      val line1 = buildString {
        append("命宮在")
        append(mainHouse.branch).append(",")

        append(StarMain.紫微.toString(Locale.getDefault())).append("在")
        starMap[StarMain.紫微]?.also { ziweiHouse: HouseData ->
          append(ziweiHouse.stemBranch.branch)
          append("(").append(ziweiHouse.house).append("宮)")
        }
      }

      val line2 = buildString {
        val naYin = NaYin.getDesc(mainHouse, locale)
        append(naYin + " " + 五行.toString() + 五行局 + "局")
      }

      val line3 = buildString {
        append("命主：")
        append(mainStar.toString(locale))
        append("，")
        append("身主：")
        append(bodyStar.toString(locale))
      }


      listOf(line1, line2, line3)
    }


    val notes: List<String> = notesBuilders.build(config.locale).let {
      if (appendingNotes.isEmpty()) {
        it
      } else {
        it.toMutableList().apply {
          addAll(appendingNotes)
        }
      }
    }


    return Plate(
      null, chineseDate, null, year, finalMonthNumForMonthStars, hour, null, null, dayNight, gender, bodyHouse, mainStar,
      bodyStar, 五行, 五行局, houseDataSet, transFourMap //, branchFlowHouseMap
      , flowBranchMap, starStrengthMap, notes,
      vageMap, rageMap, summaries
    )
  }

  override fun getModernBirthPlate(
    name: String?,
    lmt: ChronoLocalDateTime<*>?,
    loc: ILocation,
    place: String?,
    mainAndBody: Pair<Branch, Branch>?,
    preFinalMonthNumForMainStars: Int?,
    cycle: Int,
    lunarYear: StemBranch,
    solarYear: StemBranch,
    lunarMonth: Int,
    leapMonth: Boolean,
    monthBranch: Branch,
    solarTerms: SolarTerms,
    lunarDays: Int,
    hour: Branch,
    dayNight: DayNight,
    gender: Gender,
    optionalVageMap: Map<Int, Pair<GmtJulDay, GmtJulDay>>?,
    optionalRageMap: Map<Int, Pair<GmtJulDay, GmtJulDay>>?,
    appendingNotes: List<String>,
    config: IZiweiConfig
  ): IPlate {

    val plate: IPlate = getBirthPlate(
      mainAndBody,
      preFinalMonthNumForMainStars,
      cycle,
      lunarYear,
      solarYear,
      lunarMonth,
      leapMonth,
      monthBranch,
      solarTerms,
      lunarDays,
      hour,
      dayNight,
      gender,
      optionalVageMap,
      optionalRageMap,
      appendingNotes,
      config
    )

    return Plate(
      name, plate.chineseDate, lmt, plate.year, plate.finalMonthNumForMonthStars, plate.hour, loc, place, plate.dayNight, plate.gender,
      plate.bodyHouse, plate.mainStar, plate.bodyStar, plate.fiveElement, plate.state, plate.houseDataSet, plate.transFours, plate.flowBranchMap,
      plate.starStrengthMap, plate.notes, plate.vageMap, plate.rageMap, plate.summaries
    )

  }

  /**
   * @param flowType  在[本命、大限、流年]... (之一)
   * @param flowStem  天干為
   * @return 傳回四化 (若有的話)
   */
  override fun getTrans4Map(flowType: FlowType, flowStem: Stem, config: IZiweiConfig): Map<Pair<ZStar, FlowType>, T4Value> {
    return config.stars.map { star ->
      val key = star to flowType
      val value: T4Value? = transFourImplMap[config.transFour]!!.getValueOf(star, flowStem)
      key to value
    }
      .filter { it.second != null }
      .associate { it.first to it.second!! }
  }

  /** 計算 大限盤  */
  override fun getFlowSection(plate: IPlate, section: StemBranch, config: IZiweiConfig): IPlateSection {
    // 在此大限中，每個地支，對應到哪個宮位

    val newBranchHouseMap: Map<Branch, House> = Branch.entries.associateWith { branch ->
      val steps = branch.getAheadOf(section.branch)

      houseSeqImplMap[config.houseSeq]!!.prev(House.命宮, steps)
    }

    // 大限四化
    val trans4Map: Map<Pair<ZStar, FlowType>, T4Value> = getTrans4Map(FlowType.SECTION, section.stem, config)


    val newHouseDataSet: Set<HouseData> = (purpleRelocationMutator.mutate(plate, config)?: plate.houseDataSet)
      .append(FlowType.SECTION, newBranchHouseMap)

    return PlateWithSection(plate , section, newBranchHouseMap, newHouseDataSet, plate.transFours.append(trans4Map))
  }

  /** 計算 流年盤  */
  override fun getFlowYear(plate: IPlate, section: StemBranch, flowYear: StemBranch, config: IZiweiConfig): IPlateYear {
    // 流年命宮
    val yearlyMain = flowYearImplMap[config.flowYear]!!.getFlowYear(flowYear.branch, plate.finalMonthNumForMonthStars, plate.hour)

    val branchHouseMap = Branch.entries.associateWith { branch ->
      val steps = branch.getAheadOf(yearlyMain)
      houseSeqImplMap[config.houseSeq]!!.prev(House.命宮, steps)
    }

    // 流年四化
    val trans4Map = getTrans4Map(FlowType.YEAR, flowYear.stem, config)

    val plateSection = getFlowSection(plate , section, config)


    // 本命盤中，是否已經存在任何 將前12星 , 若有，代表設定要計算
    val showGeneralFront: Boolean = plateSection.houseDataSet
      .flatMap { houseData -> houseData.stars }
      .any { it is StarGeneralFront }

    // 本命盤中，是否已經存在任何 歲前12星。 若有，代表設定要計算
    val showYearFront = plateSection.houseDataSet
      .flatMap { houseData -> houseData.stars }
      .any { it is StarYearFront }



    val newHouseDataSet: Set<HouseData> = plateSection.houseDataSet.map { houseData ->
      if (showGeneralFront) {
        // 先過濾掉 將前12星
        val newStars = houseData.stars.filterNot { it is StarGeneralFront }.toMutableSet().apply {
          // 再加入 流年的 將前12星

          val addingStar = StarGeneralFront.values.first { star ->
            StarGeneralFront.starFuncMap.getValue(star).invoke(flowYear.branch) == houseData.stemBranch.branch
          }
          add(addingStar)
        }

        houseData.copy(stars = newStars)
      } else {
        houseData
      }
    }.map { houseData ->
      if (showYearFront) {
        // 先過濾掉 歲前12星
        val newStars = houseData.stars.filterNot { it is StarYearFront }.toMutableSet().apply {
          // 再加入 流年的 歲前12星
          val addingStar = StarYearFront.values.first { star ->
            StarYearFront.starFuncMap.getValue(star).invoke(flowYear.branch) == houseData.stemBranch.branch
          }
          add(addingStar)
        }

        houseData.copy(stars = newStars)
      } else {
        houseData
      }
    }.toSet().append(FlowType.YEAR, branchHouseMap)

    return PlateWithYear(plateSection , flowYear, branchHouseMap, newHouseDataSet, plateSection.transFours.append(trans4Map))
  }

  /** 計算 流月盤  */
  override fun getFlowMonth(plate: IPlate, section: StemBranch, flowYear: StemBranch, flowMonth: StemBranch, config: IZiweiConfig): IPlateMonth {

    // 流月命宮
    val monthlyMain = flowMonthImplMap[config.flowMonth]!!.getFlowMonth(flowYear.branch, flowMonth.branch, plate.finalMonthNumForMonthStars, plate.hour)

    val branchHouseMap = Branch.entries.associateWith { branch ->
      val steps = branch.getAheadOf(monthlyMain)
      houseSeqImplMap[config.houseSeq]!!.prev(House.命宮, steps)
    }

    // 流月四化
    val trans4Map = getTrans4Map(FlowType.MONTH, flowMonth.stem, config)

    val plateYear = getFlowYear(plate , section, flowYear, config)

    val newHouseDataSet: Set<HouseData> = plateYear.houseDataSet.append(FlowType.MONTH, branchHouseMap)

    return PlateWithMonth(plateYear , section, branchHouseMap, newHouseDataSet, plateYear.transFours.append(trans4Map))
  }

  /** 計算 流日盤  */
  override fun getFlowDay(plate: IPlate, section: StemBranch, flowYear: StemBranch, flowMonth: StemBranch, flowDay: StemBranch, flowDayNum: Int, config: IZiweiConfig): IPlateDay {
    // 流月命宮
    val monthlyMain = flowMonthImplMap[config.flowMonth]!!.getFlowMonth(flowYear.branch, flowMonth.branch, plate.finalMonthNumForMonthStars, plate.hour)

    // 流日命宮
    val dailyMain = flowDayImplMap[config.flowDay]!!.getFlowDay(flowDay.branch, flowDayNum, monthlyMain)
    val branchHouseMap = Branch.entries.associateWith { branch ->
      val steps = branch.getAheadOf(dailyMain)
      houseSeqImplMap[config.houseSeq]!!.prev(House.命宮, steps)
    }

    // 流日四化
    val trans4Map = getTrans4Map(FlowType.DAY, flowDay.stem, config)

    val plateMonth = getFlowMonth(plate , section, flowYear, flowMonth, config)

    val newHouseDataSet = plateMonth.houseDataSet.append(FlowType.DAY, branchHouseMap)

    return PlateWithDay(plateMonth , flowDay, branchHouseMap, newHouseDataSet, plateMonth.transFours.append(trans4Map))
  }

  /** 計算 流時盤 */
  override fun getFlowHour(plate: IPlate, section: StemBranch, flowYear: StemBranch, flowMonth: StemBranch, flowDay: StemBranch, flowDayNum: Int, flowHour: StemBranch, config: IZiweiConfig): IPlateHour {
    // 流月命宮
    val monthlyMain = flowMonthImplMap[config.flowMonth]!!.getFlowMonth(flowYear.branch, flowMonth.branch, plate.finalMonthNumForMonthStars, plate.hour)
    // 流日命宮
    val dailyMain = flowDayImplMap[config.flowDay]!!.getFlowDay(flowDay.branch, flowDayNum, monthlyMain)
    // 流時命宮
    val hourlyMain = flowHourImplMap[config.flowHour]!!.getFlowHour(flowHour.branch, dailyMain)

    val branchHouseMap = Branch.entries.associateWith { branch ->
      val steps = branch.getAheadOf(hourlyMain)
      houseSeqImplMap[config.houseSeq]!!.prev(House.命宮, steps)
    }

    // 流時四化
    val trans4Map: Map<Pair<ZStar, FlowType>, T4Value> = getTrans4Map(FlowType.HOUR, flowHour.stem, config)

    val plateDay = getFlowDay(plate , section, flowYear, flowMonth, flowDay, flowDayNum, config)

    val newHouseDataSet = plateDay.houseDataSet.append(FlowType.HOUR, branchHouseMap)

    return PlateWithHour(plateDay , flowHour, branchHouseMap, newHouseDataSet, plateDay.transFours.append(trans4Map))
  }


  private fun Set<HouseData>.append(flowType: FlowType, branchHouseMap: Map<Branch, House>): Set<HouseData> {
    return this.map { houseData ->
      val newFlowHouseMap: Map<FlowType, House> = houseData.flowHouseMap.toMutableMap().apply {
        put(flowType, branchHouseMap[houseData.stemBranch.branch]!!)
      }.toMap()
      houseData.copy(house = newFlowHouseMap[flowType]!!,
                     flowHouseMap = newFlowHouseMap)
    }.toSet()
  }


  private fun Map<ZStar, Map<FlowType, T4Value>>.append(trans4Map: Map<Pair<ZStar, FlowType>, T4Value>): Map<ZStar, Map<FlowType, T4Value>> {
    return this.toMutableMap().apply {
      trans4Map.forEach { (starFlowType: Pair<ZStar, FlowType>, value: T4Value) ->
        val (star, flowType) = starFlowType

        val starMap: Map<FlowType, T4Value>? = this[star]
        if (starMap != null) {
          this[star] = starMap.toMutableMap().apply {
            put(flowType, value)
          }
        } else {
          put(star, mapOf(flowType to value))
        }
      }
    }.toMap()
  }


  /** 反推大限、流年兩資訊 */
  override fun reverseFlows(plate: IPlate, viewGmt: GmtJulDay, config: IZiweiConfig): Flow? {
    return plate.location?.let { loc ->
      plate.localDateTime?.let { lmt ->
        lmt.toGmtJulDay(loc).takeIf { birthGmtJulDay ->
          viewGmt > birthGmtJulDay
        }?.let {
          val ageMap = plate.getAgeMap(config.sectionAgeType)!!
          ageMap.entries.firstOrNull { (_, pair) ->
            val (fromGmt, toGmt) = pair
            viewGmt in fromGmt..toGmt
          }?.key?.let { targetAge -> // target歲數
            val section: StemBranch? = plate.findSection(targetAge)
            val flowYear: StemBranch = when (config.sectionAgeType) {
              AgeType.VIRTUAL -> plate.year.next(targetAge - 1)
              AgeType.REAL    -> plate.year.next(targetAge)
            }
            Flow(section, flowYear)
          }
        }
      }
    }
  }


  /**
   * @param cycle     cycle
   * @param flowYear  流年
   * @param flowMonth 流月
   * @param leap      是否閏月
   * @return 該流月的日子 (陰曆＋陽曆＋干支）
   */
  override fun getDaysOfMonth(cycle: Int, flowYear: StemBranch, flowMonth: Int, leap: Boolean): List<Triple<ChineseDate, ChronoLocalDate, StemBranch>> {
    val days = chineseDateFeature.getDaysOf(cycle, flowYear, flowMonth, leap)

    return (1..days).map {i ->
      val yinDate = ChineseDate(cycle, flowYear, flowMonth, leap, i)

      val yangDate = chineseDateFeature.getYangDate(yinDate)
      val lmtJulDay = (TimeTools.getGmtJulDay(yangDate.atTime(LocalTime.MIDNIGHT)).value + 0.5).toInt()
      val index = (lmtJulDay - 11) % 60
      val sb = StemBranch[index]
      Triple(yinDate, yangDate, sb)
    }.toList()

  }

  /** 列出此大限中，包含哪十個流年 (陰曆 cycle + 地支干支) , 並且「歲數」各別是幾歲  */
  override fun getYearsOfFlowSection(plate: IPlate, section: Branch, config: IZiweiConfig): List<Triple<Int, StemBranch, Int>> {

    val birthYear = plate.chineseDate.year
    val birthCycle = plate.chineseDate.cycleOrZero

    val flowSectionImpl = flowSectionImplMap[config.bigRange]!!

    // 原本 house 取得方式 : plate.branchHouseMap.getValue(section) , 只會取得 命宮
    val (fromAge, toAge) = flowSectionImpl.getAgeRange(plate.getHouseDataOf(section).getHouse(FlowType.MAIN),
                                                       plate.state, birthYear.stem, plate.gender, houseSeqImplMap[config.houseSeq]!!)

    return when(config.sectionAgeType) {
      AgeType.VIRTUAL -> {
        (fromAge..toAge).map { vAge ->
          val sb = birthYear.next(vAge - 1) // 虛歲 (vAge) 轉換為年 , 要減一 .
          val cycle: Int = if (sb.index >= birthYear.index) {
            birthCycle + (vAge - 1) / 60
          } else {
            birthCycle + (vAge - 1) / 60 + 1
          }
          Triple(cycle, sb, vAge)
        }.toList()
      }
      AgeType.REAL -> {
        (fromAge..toAge).map { rAge ->
          val sb = birthYear.next(rAge) // 實歲 (rAge) 轉換為年 , 不需要減一 .
          val cycle: Int = if (sb.index >= birthYear.index) {
            birthCycle + (rAge) / 60
          } else {
            birthCycle + (rAge) / 60 + 1
          }
          Triple(cycle, sb, rAge)
        }.toList()
      }
    }
  }

  override fun calculate(lmt: ChronoLocalDateTime<*>, loc: ILocation, gender: Gender, name: String?, place: String?, config: IZiweiConfig): IPlate {

    // 排盤之中所產生的註解 , Pair<KEY , parameters>
    val notesBuilders = mutableListOf<Pair<String, Array<Any>>>()

    // 命宮地支, 身宮地支
    val (mainBranch, bodyBranch, finalMonthNumForMainStars) = getMainBodyHouse(lmt, loc, config)

    if (config.mainBodyHouse == MainBodyHouse.Astro) {
      logger.warn("命宮、身宮 採用上升、月亮星座")
      notesBuilders.add(Pair("main_body_astro", arrayOf()))
    }

    logger.debug("命宮地支 = {} , 身宮地支 = {}", mainBranch, bodyBranch)

    val (dst , duration) = TimeTools.getDstAndOffset(lmt, loc)
    val minuteOffset = duration.inWholeMinutes

    val cDate = chineseDateFeature.getModel(lmt, loc)
    val cycle = cDate.cycleOrZero
    val yinYear = cDate.year

    val monthBranch = yearMonthFeature.getModel(lmt, loc, config.ewConfig.yearMonthConfig).branch

    val solarYear = yearFeature.getModel(lmt, loc, config.ewConfig.yearMonthConfig.yearConfig)

    val lunarMonth = cDate.month
    val solarTerms = yearMonthFeature.solarTermsImpl.getSolarTerms(lmt, loc)

    val lunarDays = cDate.day

    val hour = dayHourFeature.getModel(lmt, loc, config.ewConfig.dayHourConfig).second.branch

    val hourImpl = config.ewConfig.dayHourConfig.hourBranchConfig.hourImpl

    if (dst) {
      // 日光節約時間 特別註記
      notesBuilders.add(Pair("dst", arrayOf()))
      logger.trace("[DST]:校正日光節約時間...")
      logger.trace("lmt = {} , location = {} . location.hasMinuteOffset = {}", lmt, loc, loc.hasMinuteOffset)
      logger.trace("loc tz = {} , minuteOffset = {}", loc.zoneId.id, loc.finalMinuteOffset)
      logger.trace("有日光節約時間 ,  GMT 時差 : {}", minuteOffset)
      logger.trace("時辰 = {} . hourImpl = {}", hour, config.ewConfig.dayHourConfig.hourBranchConfig.hourImpl)
    }

    if (hourImpl == HourImpl.TST) {
      // 如果是真太陽時

      val hour2: Branch = hourBranchFeature.getModel(lmt, loc, HourBranchConfig(hourImpl = HourImpl.LMT))

      if (hour != hour2) {
        // 如果真太陽時與LMT時間不一致，出現提醒
        notesBuilders.add(Pair("true_solar_time", arrayOf(hour, hour2)))
      }
    }

    val dayNight = dayNightFeature.getModel(lmt, loc, config.dayNightConfig)

    // 虛歲時刻 , gmt Julian Day
    val vageMap = intVageImpl.getRangesMap(gender, lmt.toGmtJulDay(loc), loc, 1, 130)
    // 實歲時刻 , gmt Julian Day
    val rageMap = intRageImpl.getRangesMap(gender, lmt.toGmtJulDay(loc), loc, 0, 130)

    val appendingNotes = notesBuilders.build(config.locale)

    return getModernBirthPlate(
      name, lmt, loc, place,
      Pair(mainBranch, bodyBranch),
      finalMonthNumForMainStars,
      cycle,
      yinYear,
      solarYear,
      lunarMonth,
      cDate.leapMonth,
      monthBranch,
      solarTerms,
      lunarDays,
      hour,
      dayNight,
      gender,
      vageMap,
      rageMap,
      appendingNotes,
      config
    )

  }

  companion object {
    private val logger = KotlinLogging.logger { }
    const val CACHE_ZIWEI_FEATURE = "ziweiFeatureCache"
  }
}
