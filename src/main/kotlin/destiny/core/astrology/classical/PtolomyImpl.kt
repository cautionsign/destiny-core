/**
 * Created by smallufo on 2017-12-21.
 */
package destiny.core.astrology.classical

import destiny.core.DayNight
import destiny.core.DayNight.DAY
import destiny.core.DayNight.NIGHT
import destiny.core.astrology.*
import destiny.core.astrology.Element.*
import destiny.core.astrology.Planet.*
import destiny.core.astrology.ZodiacDegree.Companion.toZodiacDegree
import destiny.core.astrology.ZodiacSign.*
import destiny.core.astrology.ZodiacSign.Companion.of
import destiny.tools.CircleTools.normalize
import java.io.Serializable


/**
 * Ruler 托勒密 Ptolemy 實作
 */
object RulerPtolemyImpl : AbstractPtolemy(), IRuler {
  private fun readResolve(): Any = RulerPtolemyImpl

  override fun ZodiacSign.getRulerPoint(dayNight: DayNight?): Planet? {
    return if (dayNight != null)
      rulerDayNightMap[(this to dayNight)]
    else
      rulerMap[this]
  }

  override fun Planet.getRulingSign(dayNight: DayNight): ZodiacSign? {
    return rulingDayNightMap[(this to dayNight)]
  }

  /** 不分日夜，取得此行星為哪兩個星座的 ruler (日月除外 , 各只有一個星座） */
  override fun Planet.getRulingSigns(): Set<ZodiacSign> {
    return rulingMap.getValue(this)
  }
}


/**
 * 陷 , -5
 * Detriment 托勒密 Ptolemy 實作
 * (Ruler 對沖)
 */
object DetrimentPtolemyImpl : AbstractPtolemy(), IDetriment {
  private fun readResolve(): Any = DetrimentPtolemyImpl

  override fun ZodiacSign.getDetrimentPoint(): Planet {
    return rulerMap.getValue(this.oppositeSign)
  }

  /** 此行星在哪些星座 陷 (-5), 至少一個，最多兩個 */
  override fun Planet.getDetrimentSigns(): Set<ZodiacSign> {
    return detrimentMap.getValue(this) // 托勒密表格，必定有值
  }

  override fun ZodiacSign.getDetrimentPoint(dayNight: DayNight?): Planet? {
    return rulerDayNightMap[this.oppositeSign to dayNight]
  }
}

/** Exaltation , +4 */
object ExaltationPtolemyImpl : AbstractPtolemy(), IExaltation {
  private fun readResolve(): Any = ExaltationPtolemyImpl

  /** 哪顆星體在此星座 擢升 (EXALT , +4) , 必定為 1 or 0 顆星 */
  override fun ZodiacSign.getExaltPoint(): AstroPoint? {
    return findPoint(this, exaltDegreeMap)
  }

  /** 此星體在哪個星座 擢升 (EXALT , +4) , 前者逆函數 , 必定有值 */
  override fun AstroPoint.getExaltSign(): ZodiacSign? {
    return exaltSignMap[this]
  }

  /** 取得在此星座得到「Exaltation , 廟 +4」的星體及度數 */
  override fun ZodiacSign.getExaltPointDegree(): PointDegree? {
    return findPoint(this, exaltDegreeMap)?.let { point -> PointDegree(point, exaltDegreeMap.getValue(point)) }
  }
}

/** Fall , -4 */
object FallPtolemyImpl : AbstractPtolemy(), IFall, Serializable {
  private fun readResolve(): Any = FallPtolemyImpl

  /** 哪顆星體在此星座 落 (FALL , -4) , 必定為 1 or 0 顆星 */
  override fun ZodiacSign.getFallPoint(): AstroPoint? {
    return findPoint(this, fallDegreeMap)
  }

  /** 此星體在哪個星座 落 (FALL , -4) , 前者逆函數 */
  override fun AstroPoint.getFallingSign(): ZodiacSign? {
    return fallSignMap[this]
  }

  /** 取得在此星座得到 落 (FALL , -4) 的星體及度數 */
  override fun ZodiacSign.getPointDegree(): PointDegree? {
    return findPoint(this, fallDegreeMap)?.let { point -> PointDegree(point, fallDegreeMap.getValue(point)) }
  }
}

/**
 * Triplicity , +3
 * 托勒密 三分相 :
 * source https://imgur.com/CoTa2bZ
 * https://tonylouis.wordpress.com/2012/04/14/still-thinking-about-triplicity-rulers/
 *
 * <pre>
 *     | 白天 | 夜晚 | 共管
 * -----------------------
 * 火象 | 太陽 | 木星 | 火星
 * 土象 | 金星 | 月亮 | 土星
 * 風象 | 土星 | 水星 | 木星
 * 水象 | 金星 | 月亮 | 火星
 * </pre>
 * William Lily 指稱 , 托勒密後來把水象星座 的日夜 ruler 都改為火星 , 但此實作仍拆分為 金星、月亮
 * 另一版本實作 [TriplicityWilliamImpl] 則作此調整
 * */
object TriplicityPtolomyImpl : ITriplicity, Serializable {
  private fun readResolve(): Any = TriplicityPtolomyImpl

  /** 哪顆星在此星座得到三分相 (+3) */
  override fun ZodiacSign.getTriplicityPoint(dayNight: DayNight): AstroPoint {
    return when (dayNight) {
      DAY -> when (this.element) {
        FIRE -> SUN
        EARTH -> VENUS
        AIR -> SATURN
        WATER -> VENUS
      }
      NIGHT -> when (this.element) {
        FIRE -> JUPITER
        EARTH -> MOON
        AIR -> MERCURY
        WATER -> MOON
      }
    }
  }

  /**
   * 共管 , Partner
   * Ptolomy 只有水象星座，由火星共管
   * */
  override fun ZodiacSign.getPartner(): AstroPoint {
    return when (this.element) {
      FIRE -> MARS
      EARTH -> SATURN
      AIR -> JUPITER
      WATER -> MARS
    }
  }

}


object TermPtolomyImpl : ITerm, Serializable {
  private fun readResolve(): Any = TermPtolomyImpl

  override fun getPoint(degree: ZodiacDegree): AstroPoint {
    val signIndex = degree.value.toInt() / 30

    return (0..4)
      .map { termPointDegrees[signIndex * 5 + it] }
      .filter { degree.value < it.degree }
      .map { it.point }
      .firstOrNull() ?: throw IllegalStateException("Cannot find Essential Terms at degree $degree , signIndex = $signIndex")
  }

  override fun ZodiacSign.getTermPoint(degree: Double): AstroPoint {
    return getPoint((this.degree + degree).toZodiacDegree())
  }

  /**
   * Terms , +2
   * Ptolemy's Table , 以五分法
   * */
  private val termPointDegrees = listOf(
    //戌
    PointDegree(JUPITER, 6.0),
    PointDegree(VENUS, 14.0),
    PointDegree(MERCURY, 21.0),
    PointDegree(MARS, 26.0),
    PointDegree(SATURN, 30.0),
    //酉
    PointDegree(VENUS, 38.0),
    PointDegree(MERCURY, 45.0),
    PointDegree(JUPITER, 52.0),
    PointDegree(SATURN, 56.0),
    PointDegree(MARS, 60.0),
    //申
    PointDegree(MERCURY, 67.0),
    PointDegree(JUPITER, 74.0),
    PointDegree(VENUS, 81.0),
    PointDegree(SATURN, 85.0),
    PointDegree(MARS, 90.0),
    //未
    PointDegree(MARS, 96.0),
    PointDegree(JUPITER, 103.0),
    PointDegree(MERCURY, 110.0),
    PointDegree(VENUS, 117.0),
    PointDegree(SATURN, 120.0),
    //午
    PointDegree(SATURN, 126.0),
    PointDegree(MERCURY, 133.0),
    PointDegree(VENUS, 139.0),
    PointDegree(JUPITER, 145.0),
    PointDegree(MARS, 150.0),
    //巳
    PointDegree(MERCURY, 157.0),
    PointDegree(VENUS, 163.0),
    PointDegree(JUPITER, 168.0),
    PointDegree(SATURN, 174.0),
    PointDegree(MARS, 180.0),
    //辰
    PointDegree(SATURN, 186.0),
    PointDegree(VENUS, 191.0),
    PointDegree(JUPITER, 199.0),
    PointDegree(MERCURY, 204.0),
    PointDegree(MARS, 210.0),
    //卯
    PointDegree(MARS, 216.0),
    PointDegree(JUPITER, 224.0),
    PointDegree(VENUS, 231.0),
    PointDegree(MERCURY, 237.0),
    PointDegree(SATURN, 240.0),
    //寅
    PointDegree(JUPITER, 248.0),
    PointDegree(VENUS, 254.0),
    PointDegree(MERCURY, 259.0),
    PointDegree(SATURN, 265.0),
    PointDegree(MARS, 270.0),
    //丑
    PointDegree(VENUS, 276.0),
    PointDegree(MERCURY, 282.0),
    PointDegree(JUPITER, 289.0),
    PointDegree(MARS, 295.0),
    PointDegree(SATURN, 300.0),
    //子
    PointDegree(SATURN, 306.0),
    PointDegree(MERCURY, 312.0),
    PointDegree(VENUS, 320.0),
    PointDegree(JUPITER, 325.0),
    PointDegree(MARS, 330.0),
    //亥
    PointDegree(VENUS, 338.0),
    PointDegree(JUPITER, 344.0),
    PointDegree(MERCURY, 350.0),
    PointDegree(MARS, 356.0),
    PointDegree(SATURN, 359.999999999999) //如果改成 360 , 會被 normalize 成 0
  )
}

/**
 * Face , +1
 * Essential Face 內定實作  , 參考 Ptolemy's Table , 以三分法 .
 *
 * Al-Biruni 利用 Chaldean order 排列，從戌宮零度開始， 火 -> 日 -> 金 -> 水 -> 月 -> 土 -> 木 ，依序下去，每星佔 10度
 *
 * 另一種做法，是 Ptolemy 的定義：
 * 根據此網站說明： http://www.gotohoroscope.com/dictionary/astrological-F.html
 * A planet is said to be in its own face when located in a house that is distant from the Moon or the Sun
 * by the same number of houses as the sign it rules is distant from the sign ruled by the Moon or Sun respectively.
 *
 * 也就是說 :
 *
 * 水星若與日月呈現 30度角，則得 Face
 * 金星若與日月呈現 60度角，則得 Face
 * 火星若與日月呈現 90度角，則得 Face
 * 木星若與日月呈現120度角，則得 Face
 * 土星若與日月呈現150度角，則得 Face
 *
 * */



object FacePtolomyImpl : IFace, Serializable {
  private fun readResolve(): Any = FacePtolomyImpl

  /** 取得黃道帶上的某點，其 Face 是哪顆星 , 0<=degree<360  */
  override fun getPoint(degree: ZodiacDegree): Planet {
    val index = (degree.value / 10).toInt()
    return faceStarList[index]
  }

  /** 取得某星座某度，其 Face 是哪顆星 , 0<=degree<30  */
  override fun ZodiacSign.getFacePoint(degree: Double): Planet {
    return getPoint((this.degree + degree).toZodiacDegree())
  }

  /** 因為間距固定 10度 , 所以 list 不用儲存度數  */
  private val faceStarList = listOf(
    //戌
    MARS, SUN, VENUS
    //酉
    , MERCURY, MOON, SATURN
    //申
    , JUPITER, MARS, SUN
    //未
    , VENUS, MERCURY, MOON
    //午
    , SATURN, JUPITER, MARS
    //巳
    , SUN, VENUS, MERCURY
    //辰
    , MOON, SATURN, JUPITER
    //卯
    , MARS, SUN, VENUS
    //寅
    , MERCURY, MOON, SATURN
    //丑
    , JUPITER, MARS, SUN
    //子
    , VENUS, MERCURY, MOON
    //亥
    , SATURN, JUPITER, MARS
  )
}




abstract class AbstractPtolemy : Serializable {

  fun findPoint(sign: ZodiacSign, map: Map<AstroPoint, Double>): AstroPoint? {
    return map.entries
      .filter { (_, value) -> sign === of(value) }
      .map { entry -> entry.key }
      .firstOrNull()
  }

  companion object {

    /** 托勒密 RULER / DETRIMENT 共用表格 */

    internal val rulerMap = mapOf(
      ARIES to MARS,
      TAURUS to VENUS,
      GEMINI to MERCURY,
      CANCER to MOON,
      LEO to SUN,
      VIRGO to MERCURY,
      LIBRA to VENUS,
      SCORPIO to MARS,
      SAGITTARIUS to JUPITER,
      CAPRICORN to SATURN,
      AQUARIUS to SATURN,
      PISCES to JUPITER
    )

    /** 存放星體在黃道帶上幾度得到 Exaltation (+4) 的度數  */
    internal val exaltDegreeMap = mapOf<AstroPoint, Double>(
      SUN to 19.0  // 太陽在戌宮 19度 exalted.
      , MOON to 33.0  // 月亮在酉宮 03度 exalted.
      , MERCURY to 165.0  // 水星在巳宮 15度 exalted.
      , VENUS to 357.0  // 金星在亥宮 27度 exalted.
      , MARS to 298.0  // 火星在丑宮 28度 exalted.
      , JUPITER to 105.0  // 木星在未宮 15度 exalted.
      , SATURN to 201.0  // 土星在辰宮 21度 exalted.
      , LunarNode.NORTH_TRUE to 63.0  //北交點在 申宮 03度 exalted.
      , LunarNode.NORTH_MEAN to 63.0  //北交點在 申宮 03度 exalted.
      , LunarNode.SOUTH_TRUE to 243.0  //南交點在 寅宮 03度 exalted.
      , LunarNode.SOUTH_MEAN to 243.0  //南交點在 寅宮 03度 exalted.
    )

    /** 不考量「日、夜」的 ruler */
    internal val rulingMap: Map<Planet, Set<ZodiacSign>> = rulerMap
      .map { (sign, planet) -> planet to sign }
      .groupBy { (planet, _) -> planet }
      .mapValues { (_, v) -> v.map { p -> p.second } }
      .mapValues { it.value.toSet() }


    /** 此行星，在哪些星座 陷 (-5) */
    internal val detrimentMap: Map<Planet, Set<ZodiacSign>> = rulerMap
      .map { (sign, planet) -> planet to sign.oppositeSign }
      .groupBy { (planet, _) -> planet }
      .mapValues { (_, v) -> v.map { p -> p.second } }
      .mapValues { it.value.toSet() }


    /**
     * 考量日夜的 rulerMap , 參考表格 : https://imgur.com/a/bZ6ij
     * 讀作 : 什麼星座的日/夜 的 ruler 是誰(maybe null)
     * */
    internal val rulerDayNightMap = mapOf(
      (ARIES to DAY) to MARS,
      (TAURUS to NIGHT) to VENUS,
      (GEMINI to DAY) to MERCURY,
      (CANCER to DAY) to MOON,
      (CANCER to NIGHT) to MOON,
      (LEO to DAY) to SUN,
      (LEO to NIGHT) to SUN,
      (VIRGO to NIGHT) to MERCURY,
      (LIBRA to DAY) to VENUS,
      (SCORPIO to NIGHT) to MARS,
      (SAGITTARIUS to DAY) to JUPITER,
      (CAPRICORN to NIGHT) to SATURN,
      (AQUARIUS to DAY) to SATURN,
      (PISCES to NIGHT) to JUPITER
    )

    /**
     * (火星, DAY) ==> 牡羊
     * (火星, NIGHT) ==> 天蠍
     *
     * (金星, NIGHT) ==> 金牛
     * (金星, DAY) ==> 天秤
     *
     * (水星, DAY) ==> 雙子
     * (水星, NIGHT) ==> 處女
     *
     * (月亮, DAY) ==> 巨蟹
     * (月亮, NIGHT) ==> 巨蟹
     *
     * (太陽, DAY) ==> 獅子
     * (太陽, NIGHT) ==> 獅子
     *
     * (木星, DAY) ==> 射手
     * (木星, NIGHT) ==> 雙魚
     *
     * (土星, NIGHT) ==> 摩羯
     * (土星, DAY) ==> 水瓶
     */
    internal val rulingDayNightMap: Map<Pair<Planet, DayNight>, ZodiacSign> =
      rulerDayNightMap.entries.map { (sign_to_DN, planet) -> planet to sign_to_DN }
        .groupBy { (planet, _) -> planet }
        .flatMap { (_, sign_to_DN) -> sign_to_DN }
        .associate { (planet, sign_to_DN) -> (planet to sign_to_DN.second) to sign_to_DN.first }


    /** 承上，儲存的是星座值 */
    internal val exaltSignMap: Map<AstroPoint, ZodiacSign> = exaltDegreeMap
      .mapValues { (_, degree) -> of(degree) }
      .toMap()

    /** Fall Degree Map , 即為 Exalt 對沖的度數 */
    internal val fallDegreeMap: Map<AstroPoint, Double> = exaltDegreeMap
      .mapValues { (_, deg) -> (deg + 180).normalize() }
      .toMap()

    /** 承上，儲存的是星座 */
    internal val fallSignMap = fallDegreeMap
      .mapValues { (_, degree) -> of(degree) }
      .toMap()


  } // companion object
}
