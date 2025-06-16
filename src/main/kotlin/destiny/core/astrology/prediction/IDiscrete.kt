package destiny.core.astrology.prediction

import destiny.core.calendar.GmtJulDay

/**
 * 繼承自 Mappable , 只是此推運法是「離散收斂」的 : 只要日期不同，有可能收斂到同一個日子
 * 例如太陽返照，太陰返照。同年（月）當中的時間，其實是收斂到同樣的盤
 */
interface IDiscrete : Mappable {

  /**
   * 收斂到哪兩個日期之間
   * 取得此離散收斂事件的有效時間區間 (validity period)。
   * 在這個區間內的所有 nowGmtJulDay 都會收斂到同一個結果。
   * @return Pair<GmtJulDay, GmtJulDay> 代表區間的開始與結束。
   * */
  fun getConvergentPeriod(natalGmtJulDay: GmtJulDay, nowGmtJulDay: GmtJulDay): Pair<GmtJulDay, GmtJulDay>
}
