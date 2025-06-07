/**
 * Created by smallufo on 2015-05-27.
 */
package destiny.core.chinese.liuren

import destiny.core.Descriptive
import destiny.core.chinese.liuren.General.*
import destiny.tools.asDescriptive
import java.io.Serializable
import java.util.*


class GeneralSeqDefaultImpl : IGeneralSeq,
                              Descriptive by GeneralSeq.Default.asDescriptive(),
                              Serializable {

  override fun next(from: General, n: Int): General {
    return get(getIndex(from) + n)
  }

  companion object {

    private val ARRAY = arrayOf(貴人, 螣蛇, 朱雀, 六合, 勾陳, 青龍, 天空, 白虎, 太常, 玄武, 太陰, 天后)

    private operator fun get(index: Int): General {
      return ARRAY[index.mod(ARRAY.size)]
    }

    private fun getIndex(g: General): Int {
      // 與原本 enum 排序相同，可以直接 binary search
      return Arrays.binarySearch(ARRAY, g)
    }
  }
}
