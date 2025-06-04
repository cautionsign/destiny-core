package destiny.core.chinese.impls

import destiny.core.Descriptive
import destiny.core.chinese.*
import destiny.core.chinese.Branch.*
import destiny.core.chinese.Stem.*
import destiny.tools.asDescriptive
import java.io.Serializable

/** 羊刃 : 「祿」  的下一位 , 陰干 的羊刃，會落在辰戌丑未 四庫中。 參考 https://imgur.com/bZZQRIw */
object YangBladeNextBlissImpl : IYangBlade,
                               Descriptive by YangBlade.NextBliss.asDescriptive(),
                               Serializable {
  private fun readResolve(): Any = YangBladeNextBlissImpl
  override fun getYangBlade(stem: Stem): Branch {
    return Characters.getBliss(stem).next(1)
  }
}

/** 羊刃 : 劫財 算法 , 陰干 的羊刃，會落在 寅巳申亥 四驛馬中。 參考 https://imgur.com/bZZQRIw */
object YangBladeRobCashImpl : IYangBlade,
                             Descriptive by YangBlade.RobCash.asDescriptive(),
                             Serializable {
  private fun readResolve(): Any = YangBladeRobCashImpl
  override fun getYangBlade(stem: Stem): Branch {
    return when (stem) {
      甲     -> 卯      // 甲的帝旺在卯，卯中藏乙木，乙是甲的劫財，故卯是甲的羊刃。
      乙     -> 寅      // 乙的帝旺在寅，寅藏甲丙戊，甲是乙的劫財，故寅是乙的羊刃。
      丙, 戊 -> 午      // 丙戊帝旺在午，午中藏丁己，丁是丙的劫財，己是戊的劫財，故午是丙戊的羊刃。
      丁, 己 -> 巳      // 丁己帝旺在巳，巳藏丙戊庚，丙是丁的劫財，戊是己的劫財，故巳是丁己的羊刃。
      庚     -> 酉      // 庚的帝旺在酉，酉獨藏辛金，辛是庚的劫財，故酉是庚的羊刃。
      辛     -> 申      // 辛的帝旺在申，申藏庚壬戊，庚是辛的劫財，故申是辛的羊刃。
      壬     -> 子      // 壬的帝旺在子，子藏單癸水，癸是壬的劫財，故子是壬的羊刃。
      癸     -> 亥      // 癸的帝旺在亥，亥中藏壬甲，壬是子的劫財，故亥是癸的羊刃。
    }
  }
}
