/**
 * @author smallufo
 * Created on 2005/7/2 at 上午 05:46:48
 */
package destiny.core.chinese

import destiny.core.News
import java.io.Serializable

enum class FiveElement : IFiveElement, Serializable {
  木,
  火,
  土,
  金,
  水;

  /** 取得此五行所生的五行（木生火）  */
  val product: FiveElement
    get() {
      return when (this) {
        木 -> 火
        火 -> 土
        土 -> 金
        金 -> 水
        水 -> 木
      }
    }

  /** 取得哪個五行生此五行 （生木者為水）  */
  val producer: FiveElement
    get() {
      return when (this) {
        木 -> 水
        火 -> 木
        土 -> 火
        金 -> 土
        水 -> 金
      }
    }


  /** 取得此五行所剋之五行 （木剋土）  */
  val dominateOver: FiveElement
    get() {
      return when (this) {
        木 -> 土
        火 -> 金
        土 -> 水
        金 -> 木
        水 -> 火
      }
    }

  /** 取得此五行被哪個五行剋 （木被金剋）  */
  val dominator: FiveElement
    get() {
      return when (this) {
        木 -> 金
        火 -> 水
        土 -> 木
        金 -> 火
        水 -> 土
      }
    }

  override val fiveElement: FiveElement
    get() = this

  fun isSame(f: IFiveElement): Boolean {
    return f.fiveElement == this
  }


  /** 此五行是否生另一五行  */
  fun isProducingTo(f: IFiveElement): Boolean {
    return f.fiveElement.producer == this
  }


  /** 此五行是否被另一五行所生  */
  fun isProducedBy(f: IFiveElement): Boolean {
    return f.fiveElement.product == this
  }


  /** 此五行是否剋另一五行 , Dominator : 支配者  */
  fun isDominatorOf(f: IFiveElement): Boolean {
    return f.fiveElement.dominator == this
  }

  /** 此五行是否被另一五行所剋  */
  fun isDominatedBy(f: IFiveElement): Boolean {
    return f.fiveElement.dominateOver == this
  }

  companion object {
    fun of(news: News): FiveElement {
      return when (news) {
        News.EastWest.EAST -> 木
        News.NorthSouth.SOUTH -> 火
        News.EastWest.WEST -> 金
        News.NorthSouth.NORTH -> 水
      }
    }

    fun FiveElement.sameCount(vararg elements: IFiveElement): Int {
      return elements.map { each: IFiveElement ->
        if (this.isSame(each)) 1 else 0
      }.sum()
    }

    fun FiveElement.producingCount(vararg elements: IFiveElement): Int {
      return elements.map { each ->
        if (this.isProducingTo(each)) 1 else 0
      }.sum()
    }

    fun FiveElement.producedCount(vararg elements: IFiveElement): Int {
      return elements.map { each ->
        if (this.isProducedBy(each)) 1 else 0
      }.sum()
    }

    fun FiveElement.dominatorCount(vararg elements: IFiveElement): Int {
      return elements.map { each ->
        if (this.isDominatorOf(each)) 1 else 0
      }.sum()
    }

    fun FiveElement.beatenCount(vararg elements: IFiveElement): Int {
      return elements.map { each ->
        if (this.isDominatedBy(each)) 1 else 0
      }.sum()
    }
  }
}
