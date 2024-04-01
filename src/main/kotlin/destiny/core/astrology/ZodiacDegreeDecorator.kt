/**
 * @author smallufo
 * Created on 2008/1/15 at 上午 12:55:13
 */
package destiny.core.astrology

import destiny.tools.LocaleTools
import destiny.tools.getOutputString
import destiny.tools.getTitle
import java.util.*

object ZodiacDegreeDecorator {
  private val implMap = mapOf(
    Locale.TAIWAN to ZodiacDegreeDecoratorTradChinese(),
    Locale.US to ZodiacDegreeDecoratorEnglish()
  )


  fun getOutputString(degree: Double, locale: Locale): String {
    return implMap.getOutputString(degree, locale)
  }

  fun getSimpOutputString(degree: Double, locale: Locale): String {
    return implMap.getValue(LocaleTools.getBestMatchingLocaleOrFirst(locale, implMap.keys)).getSimpOutString(degree)
  }
}

private val commonSimpleDegToString = { sb: StringBuilder, degree: Double, locale: Locale, min: Int ->
  val sign = ZodiacSign.of(degree)
  val degInt = (degree - sign.degree).toInt()

  if (degInt < 10)
    sb.append("0")
  sb.append(degInt)
  sb.append(sign.getAbbreviation(locale))
  if (min < 10)
    sb.append("0")
  sb.append(min)
}


class ZodiacDegreeDecoratorTradChinese : IZodiacDegreeDecorator {

  override val locale: Locale = Locale.TAIWAN

  private var deg: Double = 0.toDouble()

  private val min: Int
    get() = ((deg - getDeg()) * 60).toInt()

  private val sec: Double
    get() = ((deg - getDeg()) * 60 - min) * 60

  override fun getOutputString(value: Double): String {
    val sign = ZodiacSign.of(value)
    this.deg = value - sign.degree

    val sb = StringBuilder()

    sb.append(sign.getTitle(locale)).append(" ")
    if (getDeg() < 10)
      sb.append("0")
    sb.append(getDeg()).append("度 ")
    if (min < 10)
      sb.append("0")
    sb.append(min).append("分 ")
    if (sec < 10) {
      sb.append("0")
      sb.append(sec.toString().substring(0, 1))
    } else
      sb.append(sec.toString().substring(0, 2))
    sb.append("秒")
    return sb.toString()
  }

  override fun getSimpOutString(degree: Double): String {
    val sign = ZodiacSign.of(degree)
    this.deg = degree - sign.degree

    val sb = StringBuilder()
    commonSimpleDegToString(sb, degree, locale, min)
    return sb.toString()
  }

  private fun getDeg(): Int {
    return deg.toInt()
  }

} // Chinese


class ZodiacDegreeDecoratorEnglish : IZodiacDegreeDecorator {

  override val locale: Locale = Locale.US

  private var deg: Double = 0.toDouble()

  private val min: Int
    get() = ((deg - getDeg()) * 60).toInt()

  private val sec: Double
    get() = ((deg - getDeg()) * 60 - min) * 60

  override fun getOutputString(value: Double): String {
    val sign = ZodiacSign.of(value)
    this.deg = value - sign.degree

    return sign.getTitle(locale) + " " + getDeg() + "Deg " + min + "Min " + sec + "Sec"
  }

  override fun getSimpOutString(degree: Double): String {
    val sign = ZodiacSign.of(degree)
    this.deg = degree - sign.degree

    val sb = StringBuilder()
    commonSimpleDegToString.invoke(sb, degree, locale, min)
    return sb.toString()
  }

  private fun getDeg(): Int {
    return deg.toInt()
  }

} // English
