/**
 * @author smallufo
 * Created on 2007/12/12 at 下午 8:29:27
 */
package destiny.core.astrology.classical

import destiny.core.DayNight
import destiny.core.astrology.AstroPoint
import destiny.core.astrology.IHoroscopeModel
import destiny.core.astrology.ZodiacDegree
import destiny.core.astrology.ZodiacSign
import destiny.tools.KotlinLogging

/**
 * Facade Interface of Essential Dignities and Debilities <br></br>
 * 具備計算 Ptolemy's Table of Essential Dignities and Debilities 的所有介面
 */
interface IEssential {

  /**
   * 此顆星，目前在此星盤中的強度 [Dignity] 有哪些 , 可能有多種組合
   * 例如 太陽 白天 位於 [ZodiacSign.ARIES] , 則為 [Dignity.EXALTATION] 以及 [Dignity.TRIPLICITY] , 若在 20度 , 還要加上 [Dignity.FACE]
   * 故，回傳為 List , 由強至弱排序
   */
  fun AstroPoint.getDignitiesFromSignMap(map: Map<AstroPoint, ZodiacSign>, dayNight: DayNight? = null) : List<Dignity>

  /**
   * 承上，完整度數版 (可回傳 [Dignity.TERM] 以及 [Dignity.FACE] )
   */
  fun AstroPoint.getDignities(map: Map<AstroPoint, Double>, dayNight: DayNight? = null) : List<Dignity>

  /**
   * 哪一顆星，透過 [Dignity.RULER] 接納了 [this]顆星
   * [this]為 guest , 意味：要查出目前 [this]這顆星所在星座的主人 ( [Dignity.RULER] ) 是哪顆星
   */
  fun AstroPoint.receivingRulerFromSignMap(map: Map<AstroPoint, ZodiacSign>): AstroPoint?

  /** 承上 , double map 版本 */
  fun AstroPoint.receivingRulerFrom(map: Map<AstroPoint, ZodiacDegree>): AstroPoint? {
    return this.receivingRulerFromSignMap(map.mapValues { (_, degree) -> degree.sign })
  }

  /**
   * 哪一顆星，透過 [Dignity.EXALTATION] 接納了 [this]顆星 ( [this]這顆星 為 guest )
   */
  fun AstroPoint.receivingExaltFromSignMap(map: Map<AstroPoint, ZodiacSign>): AstroPoint?

  /** 承上 , double map 版本 */
  fun AstroPoint.receivingExaltFrom(map: Map<AstroPoint, ZodiacDegree>): AstroPoint? {
    return this.receivingExaltFromSignMap(map.mapValues { (_, degree) -> degree.sign })
  }

  /** 哪一顆星，透過 [Dignity.TRIPLICITY] 接納了 [this]顆星  ( [this]這顆星 為 guest ) */
  fun AstroPoint.receivingTriplicityFromSignMap(map: Map<AstroPoint, ZodiacSign>, dayNight: DayNight): AstroPoint?

  /** 承上 , double map 版本 */
  fun AstroPoint.receivingTriplicityFrom(map: Map<AstroPoint, ZodiacDegree>, dayNight: DayNight): AstroPoint? {
    return this.receivingTriplicityFromSignMap(map.mapValues { (_, degree) -> degree.sign }, dayNight)
  }

  /** 那一顆星，透過 [Dignity.TERM] 接納了 [this]顆星  ( [this]這顆星 為 guest ) */
  fun AstroPoint.receivingTermFrom(map: Map<AstroPoint, ZodiacDegree>): AstroPoint?

  /** 哪一顆星，透過 [Dignity.FACE] 接納了 [this]顆星  ( [this]這顆星 為 guest ) */
  fun AstroPoint.receivingFaceFrom(map: Map<AstroPoint, ZodiacDegree>): AstroPoint?

  /** 哪一顆星，透過 [Dignity.FALL] 接納了 [this]顆星  ( [this]這顆星 為 guest ) */
  fun AstroPoint.receivingFallFromSignMap(map: Map<AstroPoint, ZodiacSign>): AstroPoint?

  /** 承上 , double map 版本 */
  fun AstroPoint.receivingFallFrom(map: Map<AstroPoint, ZodiacDegree>): AstroPoint? {
    return this.receivingFallFromSignMap(map.mapValues { (_, degree) -> degree.sign })
  }

  /** 哪一顆星，透過 [Dignity.DETRIMENT] 接納了 [this]顆星  ( [this]這顆星 為 guest ) */
  fun AstroPoint.receivingDetrimentFromSignMap(map: Map<AstroPoint, ZodiacSign>): AstroPoint?

  fun AstroPoint.receivingDetrimentFrom(map: Map<AstroPoint, ZodiacDegree>): AstroPoint? {
    return this.receivingDetrimentFromSignMap(map.mapValues { (_, degree) -> degree.sign })
  }

  /**
   * 此星體 從哪顆星 接收到了此種 [dignity]
   */
  fun AstroPoint.receiving(dignity: Dignity, map: Map<AstroPoint, ZodiacDegree>, dayNight: DayNight? = null) : AstroPoint? {
    return when(dignity) {
      Dignity.RULER -> this.receivingRulerFrom(map)
      Dignity.EXALTATION -> this.receivingExaltFrom(map)
      Dignity.TRIPLICITY -> dayNight?.let { dn -> this.receivingTriplicityFrom(map , dn) }
      Dignity.TERM -> this.receivingTermFrom(map)
      Dignity.FACE -> this.receivingFaceFrom(map)
      Dignity.FALL -> this.receivingFallFrom(map)
      Dignity.DETRIMENT -> this.receivingDetrimentFrom(map)
    }
  }

  /** 取得此顆星，各從哪些星體，接受哪種 [Dignity] 的招待 (承上 , 只是這是傳回 map )*/
  fun AstroPoint.getReceptions(
    map: Map<AstroPoint, ZodiacDegree>,
    dayNight: DayNight? = null,
    dignities: Set<Dignity>): Map<Dignity, AstroPoint> {

    return Dignity.entries.filter { dignities.contains(it) }.map { dignity ->


      when (dignity) {
        Dignity.RULER -> Dignity.RULER to this.receivingRulerFrom(map)
        Dignity.EXALTATION -> Dignity.EXALTATION to this.receivingExaltFrom(map)
        Dignity.TRIPLICITY -> Dignity.TRIPLICITY to dayNight?.let { this.receivingTriplicityFrom(map, it) }
        Dignity.TERM -> Dignity.TERM to this.receivingTermFrom(map)
        Dignity.FACE -> Dignity.FACE to this.receivingFaceFrom(map)
        Dignity.FALL -> Dignity.FALL to this.receivingFallFrom(map)
        Dignity.DETRIMENT -> Dignity.DETRIMENT to this.receivingDetrimentFrom(map)
      }
    }
      .filter { (_, point) -> point != null }.associate { (k, v) -> k to v!! }
  }

  /** 取得此顆星，各從哪些星體，接受哪種 [Dignity] 的招待 , 但是不計算 [Dignity.TERM] 以及 [Dignity.FACE] , 因為這兩者需要度數 */
  fun AstroPoint.getReceptionsFromSign(map: Map<AstroPoint, ZodiacSign>,
                                       dayNight: DayNight? = null,
                                       dignities: Set<Dignity>): Map<Dignity, AstroPoint> {
    return Dignity.entries.filter { dignities.contains(it) }.map { dignity ->
      when (dignity) {
        Dignity.RULER -> Dignity.RULER to this.receivingRulerFromSignMap(map)
        Dignity.EXALTATION -> Dignity.EXALTATION to this.receivingExaltFromSignMap(map)
        Dignity.TRIPLICITY -> Dignity.TRIPLICITY to dayNight?.let { this.receivingTriplicityFromSignMap(map, it) }
        Dignity.FALL -> Dignity.FALL to this.receivingFallFromSignMap(map)
        Dignity.DETRIMENT -> Dignity.DETRIMENT to this.receivingDetrimentFromSignMap(map)

        Dignity.TERM -> Dignity.TERM to null
        Dignity.FACE -> Dignity.FACE to null
      }
    }
      .filter { (_, point) -> point != null }.associate { (k, v) -> k to v!! }
  }

  /**
   * 製作出 Reception 表格
   * 參考 : http://www.skyscript.co.uk/dig6.html
   * */
  fun getReceptionMap(map: Map<AstroPoint, ZodiacDegree>,
                      dayNight: DayNight,
                      dignities: Set<Dignity>): Set<Triple<AstroPoint, Dignity, AstroPoint?>> {
    return map.keys.flatMap { p ->
      p.getReceptions(map, dayNight, dignities).map { (dignity, point) ->
        Triple(p, dignity, point)
      }
    }.toSet()
  }

  /** 查詢 [this]此星 在此星盤中 , 是否有與其他任何星，互相接納 (不論 Dignity 是否相等) */
  fun AstroPoint.getMutualData(map: Map<AstroPoint, ZodiacDegree>,
                               dayNight: DayNight?,
                               dignities: Set<Dignity>): Set<MutualData> {
    return map.keys.filter { it !== this }
      .flatMap { p2 ->
        p2.getReceptions(map, dayNight, dignities)
          .filter { (_, p1) -> p1 === this }
          .map { (dig1, p1) -> p1 to dig1 }
          .flatMap { (p1, dig1) ->
            p1.getReceptions(map, dayNight, dignities)
              .filter { (_, p) -> p === p2 }
              .map { (dig2, p2) -> MutualData(p1, dig1, p2, dig2) }
          }
      }.toSet()
  }

  /** 查詢 p 在此星盤中 , 是否有與其他任何星，互相接納 (不論 Dignity 是否相等) . 只考量星座，故，無法計算 [Dignity.TERM] 或 [Dignity.FALL] */
  fun AstroPoint.getMutualDataFromSign(map: Map<AstroPoint, ZodiacSign>,
                                       dayNight: DayNight?,
                                       dignities: Set<Dignity>): Set<MutualData> {
    return map.keys.filter { it !== this }
      .flatMap { p2 ->
        p2.getReceptionsFromSign(map, dayNight, dignities)
          .filter { (_, p1) -> p1 === this }
          .map { (dig1, p1) -> p1 to dig1 }
          .flatMap { (p1, dig1) ->
            p1.getReceptionsFromSign(map, dayNight, dignities)
              .filter { (_, p) -> p === p2 }
              .map { (dig2, p2) ->
                MutualData(p1, dig1, p2, dig2)
              }
          }
      }.toSet()
  }

  /** 所有能量的互容 , 不論相等或是不相等 */
  fun getMutualReceptionMap(map: Map<AstroPoint, ZodiacDegree>,
                            dayNight: DayNight?,
                            dignities: Set<Dignity>): Set<MutualData> {
    return map.keys
      .flatMap { p1 ->
        p1.getReceptions(map, dayNight, dignities)
          .filter { (_, p2) -> p2 !== p1 }
          .map { (dig2, p2) -> p2 to dig2 }
          .flatMap { (p2, dig2) ->
            p2.getReceptions(map, dayNight, dignities)
              .filter { (_, point) -> point === p1 && p1 !== p2 }
              .map { (dig1, _) -> MutualData(p1, dig1, p2, dig2) }
          }
      }.toSet()
  }

  /** 能量不相等的互容 */
  fun getMixedReceptionMap(map: Map<AstroPoint, ZodiacDegree>,
                           dayNight: DayNight,
                           dignities: Set<Dignity>): Set<MutualData> {
    return map.keys.flatMap { p ->
      p.getReceptions(map, dayNight, dignities)
        .filter { (_, p2) -> p2 !== p }
        .map { (dig2, p2) -> p2 to dig2 }
        .flatMap { (p2, dig2) ->
          p2.getReceptions(map, dayNight, dignities.filter { it !== dig2 }.toSet())
            .filter { (_, point) -> point === p && p !== p2 }
            .map { (dig1, _) -> MutualData(p, dig1, p2, dig2) }
        }
    }.toSet()
  }

  /**
   * 取得黃道帶上某星座，其 Dignity 之 廟旺陷落 各是何星
   * @param dignity [Dignity.RULER] 與 [Dignity.DETRIMENT] 不會傳回 null ,
   * 但 [Dignity.EXALTATION] 與 [Dignity.FALL] 就有可能為 null
   */
  fun getPoint(sign: ZodiacSign, dignity: Dignity): AstroPoint?


  /** 取得黃道帶上某星座，其 Triplicity 是什麼星   */
  fun getTriplicityPoint(sign: ZodiacSign, dayNight: DayNight): AstroPoint


  /** [this] 是否 接納 receivee by Essential Debilities (Detriment/Fall)  */
  fun AstroPoint.isReceivingFromDebilities(receivee: AstroPoint, h: IHoroscopeModel): Boolean

  /**
   * [this] 是否 接納 [receivee] by Essential Dignities (Ruler/Exaltation/Triplicity/Term/Face) <br></br>
   * 主人是 [this] , 客人是 [receivee] , 如果客人進入了主人的地盤 ( 旺 / 廟 / 三分 / Terms / Faces ) , 則「主人接納客人」、「客人接收到主人的 Dignity」
   */
  fun AstroPoint.isReceivingFromDignities(receivee: AstroPoint, h: IHoroscopeModel): Boolean

  /** 如果 兩顆星都處於 [Dignity.RULER] 或是  [Dignity.EXALTATION] , 則為 true  */
  fun isBothInGoodSituation(p1: AstroPoint, sign1: ZodiacSign, p2: AstroPoint, sign2: ZodiacSign): Boolean

  /** 是否兩顆星都處於不佳的狀態. 如果 兩顆星都處於 [Dignity.DETRIMENT] 或是  [Dignity.FALL] , 則為 true  */
  fun isBothInBadSituation(p1: AstroPoint, sign1: ZodiacSign, p2: AstroPoint, sign2: ZodiacSign): Boolean



  companion object {
    val logger = KotlinLogging.logger { }
  }

}


