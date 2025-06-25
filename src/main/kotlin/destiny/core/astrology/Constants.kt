package destiny.core.astrology

import destiny.core.calendar.Constants.SECONDS_OF_DAY

object Constants {
  /** 平均日  */
  const val MEAN_DAY = (24 * 60 * 60).toDouble() //86400

  /** 恆星日 , 23h 56m 4s  */
  const val SIDEREAL_DAY = (23 * 60 * 60).toDouble() + (56 * 60).toDouble() + 4.09074 //86 164.0907

  /** 平年 (Gregorian Year , 365 or 366)  */
  const val MEAN_YEAR = SECONDS_OF_DAY * 365.0000    //31536000

  /** 回歸年 (From one equinox to the next) */
  const val TROPICAL_YEAR_DAYS = 365.242190
  const val TROPICAL_YEAR_SECONDS = SECONDS_OF_DAY * TROPICAL_YEAR_DAYS  //31556925.2

  /** 恆星年 (Between maximum elevation passages of a fixed star) */
  const val SIDEREAL_YEAR_DAYS = 365.256363
  const val SIDEREAL_YEAR_SECONDS = SECONDS_OF_DAY * SIDEREAL_YEAR_DAYS  //31558149.8

  /** 恆星月 (1994-2000)  */
  const val SIDEREAL_MONTH_DAYS = 27.321662
  const val SIDEREAL_MONTH_SECONDS = SECONDS_OF_DAY * SIDEREAL_MONTH_DAYS // 2360591.6

  /** 會合月 (新月到新月)  */
  const val SYNODIC_MONTH_DAYS = 29.530589
  const val SYNODIC_MONTH_SECONDS = SECONDS_OF_DAY * SYNODIC_MONTH_DAYS // 2551442.89

  /** 回歸月  */
  const val TROPICAL_MONTH_DAYS = 27.321582
  const val TROPICAL_MONTH_SECONDS = SECONDS_OF_DAY * TROPICAL_MONTH_DAYS // 2360584.68

  /** 異常月 (通過近地點 , perigee) , 1994-2000  */
  const val ANOMALISTIC_MONTH_DAYS = 27.554550
  const val ANOMALISTIC_MONTH_SECONDS = SECONDS_OF_DAY * ANOMALISTIC_MONTH_DAYS // 2380713.12

  /** 龍月 ? (The Draconic month is the time between one node passage to the next.) */
  const val DRACONIC_MONTH_DAYS = 27.212221
  const val DRACONIC_MONTH_SECONDS = SECONDS_OF_DAY * DRACONIC_MONTH_DAYS // 2351135.89

}
