/**
 * Created by smallufo on 2022-07-13.
 */
package destiny.core.chinese.eightwords.hazards

import destiny.core.Gender
import destiny.core.calendar.eightwords.IEightWords
import destiny.core.chinese.Branch
import destiny.core.chinese.Branch.*
import destiny.core.chinese.FiveElement.*
import destiny.core.chinese.SimpleBranch
import destiny.core.chinese.Stem.*
import destiny.core.chinese.eightwords.hazards.Book.*
import destiny.core.chinese.eightwords.hazards.ChildHazard.*
import destiny.core.chinese.trilogy

private val 子午卯酉 = setOf(子, 午, 卯, 酉)
private val 寅申巳亥 = setOf(寅, 申, 巳, 亥)
private val 辰戌丑未 = setOf(辰, 戌, 丑, 未)

private val 寅卯辰 = setOf(寅, 卯, 辰)
private val 巳午未 = setOf(巳, 午, 未)
private val 申酉戌 = setOf(申, 酉, 戌)
private val 亥子丑 = setOf(亥, 子, 丑)

private val 巳酉丑 = setOf(巳, 酉, 丑)
private val 申子辰 = setOf(申, 子, 辰)
private val 亥卯未 = setOf(亥, 卯, 未)
private val 寅午戌 = setOf(寅, 午, 戌)


interface IHazard : java.io.Serializable {

  fun getBooks(): Set<Book> = emptySet()

  fun test(eightWords: IEightWords, gender: Gender?): Boolean
}

interface IHazardFactory : java.io.Serializable {

  val hazard: ChildHazard

  val impls: Set<IHazard>

  fun test(eightWords: IEightWords, gender: Gender?, preferredBook: Book? = null): Boolean {
    return impls.map { hazard: IHazard ->
      hazard.getBooks() to hazard.test(eightWords, gender)
    }.let { pairs: List<Pair<Set<Book>, Boolean>> ->
      if (preferredBook != null) {
        pairs.firstOrNull { it.first.contains(preferredBook) }?.second
          ?: pairs.map { it.second }.firstOrNull { it } ?: false
      } else {
        pairs.map { it.second }.firstOrNull { it } ?: false
      }
    }
  }

  /** 專測某本書籍 , 若該書未定義，則傳回 null */
  fun testBook(eightWords: IEightWords, gender: Gender?, book: Book): Boolean? {
    return impls.firstOrNull { hazard: IHazard ->
      hazard.getBooks().contains(book)
    }?.test(eightWords, gender)
  }
}


val h百日關 = object : IHazardFactory {

  override val hazard: ChildHazard = 百日關

  /**
   * 寅申巳亥月：辰戌丑未時。
   * 子午卯酉月：寅申巳亥時。
   * 辰戌丑未月：子午卯酉時。
   * 俗忌百日不出大門，房門不忌。夫百日關者，專以十二生肖月忌各所內百犯之，童限月內百日必有星辰難養。(象吉)
   *
   * 寅申巳亥月生辰戌丑未時是。
   * 子午卯酉月生寅申巳亥時是。
   * 辰戌丑未月生子午卯酉時是。
   * 俗忌百日不出門，止忌大門、房門不忌無妨。夫百日關者，專以十二生肖月忌各所值時犯之，童限月內百日必有星辰難養。又云：忌過週歲，又云：止忌百日、之外則無妨。(鰲頭)
   *
   * 寅申巳亥月忌辰戌丑未時。
   * 辰戌丑未月忌子午卯酉時，
   * 子午卯酉月忌寅申巳亥時。
   * 生孩百日內勿出大門前。(星平會海)
   *
   * 寅申巳亥月：辰戌丑未時。
   * 子午卯酉月：寅申巳亥時。
   * 辰戌丑未月：子午卯酉時。
   * 童限犯之初生百日內、勿出大門外。凡正月寅巳時生人，犯此百日內忌出入門前。(生育禮俗)
   *
   * 凡
   * 正、四、七、十月逢辰、戌、丑、未時，
   * 二、五、八、十一月逢寅、申、巳、亥時，
   * 三、六、九、十二月逢子、午、卯、酉時生人是。
   * 犯此百日內忌出入門前。(黃曆解秘)
   */
  val p百日關 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書, 星平會海, 生育禮俗, 黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅申巳亥.contains(eightWords.month.branch) && 辰戌丑未.contains(eightWords.hour.branch)) ||
          (子午卯酉.contains(eightWords.month.branch) && 寅申巳亥.contains(eightWords.hour.branch)) ||
          (辰戌丑未.contains(eightWords.month.branch) && 子午卯酉.contains(eightWords.hour.branch))
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p百日關)
}

val h千日關 = object : IHazardFactory {

  override val hazard: ChildHazard = 千日關

  /**
   * 甲乙馬頭龍不住，
   * 丙丁雞猴奔山崗，
   * 戊己逢藏蛇在草，
   * 庚辛遇虎於林下，
   * 壬癸丑亥時須忌，
   * 孩兒直此有煩惱。
   * 夫千日關者，如甲乙生人午時是也，余仿此。犯之主有驚風、吐乳之災，忌住難星。（象吉）
   *
   * 甲乙馬龍頭、
   * 丙丁猴雞山、
   * 庚辛虎林下、
   * 戊己蛇藏草、
   * 壬癸丑亥時。
   * 未滿千日勿往外媽厝，主驚風、吐乳，制化即安。凡午年寅巳亥時生人，犯上忌三歲上高落低之患。（生育禮俗）
   */
  private val p千日關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(甲, 乙).contains(eightWords.year.stem) && setOf(午).contains(eightWords.hour.branch)) ||
          (setOf(丙, 丁).contains(eightWords.year.stem) && setOf(酉, 申).contains(eightWords.hour.branch)) ||
          (setOf(戊, 己).contains(eightWords.year.stem) && setOf(巳).contains(eightWords.hour.branch)) ||
          (setOf(庚, 辛).contains(eightWords.year.stem) && setOf(寅).contains(eightWords.hour.branch)) ||
          (setOf(壬, 癸).contains(eightWords.year.stem) && setOf(丑, 亥).contains(eightWords.hour.branch))
        )
    }
  }


  /**
   * 甲乙馬頭龍不住、
   * 丙丁雞叫奔山崗、
   * 戊己逢蛇藏在草、
   * 庚辛遇虎竹林下、
   * 壬癸丑亥時須忌。
   * 孩兒值此有吁嗟。夫千日關者，且如甲乙生人忌午時是、餘仿此，犯之乃驚風、吐乳。（鰲頭）
   *
   * 甲乙馬頭籠不住、
   * 丙丁雞叫奔山岡、
   * 戊己逢蛇在草藏、
   * 庚辛遇虎於林下、
   * 壬癸丑亥時須忌。
   * 孩兒值此有嗟。過千日之外則不妨。（星平會海）
   */
  private val p千日關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(鰲頭通書, 星平會海)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(甲, 乙).contains(eightWords.year.stem) && setOf(午).contains(eightWords.hour.branch)) ||
          (setOf(丙, 丁).contains(eightWords.year.stem) && setOf(酉).contains(eightWords.hour.branch)) ||
          (setOf(戊, 己).contains(eightWords.year.stem) && setOf(巳).contains(eightWords.hour.branch)) ||
          (setOf(庚, 辛).contains(eightWords.year.stem) && setOf(寅).contains(eightWords.hour.branch)) ||
          (setOf(壬, 癸).contains(eightWords.year.stem) && setOf(丑, 亥).contains(eightWords.hour.branch))
        )
    }
  }

  /**
   * 凡午年寅、申、巳、亥時生人，犯此忌三歲上高落低之患。
   * 另一說法；三歲之前不宜到外婆家，或莫至外婆供奉祖先牌位處。（黃曆解秘）
   */
  private val p千日關C = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (eightWords.year.branch == 午 && 寅申巳亥.contains(eightWords.hour.branch))
    }
  }


  override val impls: Set<IHazard> = setOf(p千日關A, p千日關B, p千日關C)
}

val h閻王關 = object : IHazardFactory {

  override val hazard: ChildHazard = 閻王關

  /**
   * 春忌牛羊水上波，夏逢辰戌見閻羅，秋逢子午君須避，冬時生人虎兔時，
   * 甲乙丙丁申子辰，戊己庚生亥卯未，辛兼壬癸寅午戌，生孩切慮不成人。日主旺不防，弱則難養。（象吉）
   *
   * 春季牛羊水上波，夏逢辰戌見閻羅，秋遇子午君須避，冬季生人虎兔嗟。
   * 甲乙丙丁申子辰，戊己庚辛亥卯未，辛兼壬癸寅午戌，生孩切慮不成人。日主生人不旺，弱則難養。（鰲頭）
   *
   * 春忌牛羊水上波，夏逢辰戌見閻羅，秋逢子午當須避，冬季生人虎兔磨。日主旺無妨。（星平會海）
   *
   * 春忌牛羊水上波，
   * 夏逢辰戌見閻王，
   * 秋怕子午當迴避，
   * 冬季生人虎兔磨。
   * 勿看功果做佛事，日主弱宜制化。凡七、八、九、十二月子午寅卯時生人，犯此帶天德、月德可解。（生育禮俗）
   */
  private val p閻王關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書, 星平會海, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && setOf(丑, 未).contains(eightWords.hour.branch)) ||
          (巳午未.contains(eightWords.month.branch) && setOf(辰, 戌).contains(eightWords.hour.branch)) ||
          (申酉戌.contains(eightWords.month.branch) && setOf(子, 午).contains(eightWords.hour.branch)) ||
          (亥子丑.contains(eightWords.month.branch) && setOf(寅, 卯).contains(eightWords.hour.branch))
        )
    }
  }

  /**
   * 凡七、八、九、十、十二月子、午、寅、卯時生人。
   * 另正、二、三月丑、未時，
   * 四、五、六月辰、戌時，
   * 七、八、九月子、午時，
   * 十、十一、十二月寅、卯時生人是，
   * 犯此小時應該避免看誦經作法或作功德場合，難養，帶天德、月德可解。（黃曆解秘）
   */
  val p閻王關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(午, 未, 申, 酉, 亥).contains(eightWords.month.branch) && setOf(子, 午, 寅, 卯).contains(eightWords.hour.branch)) ||
          (寅卯辰.contains(eightWords.month.branch) && setOf(丑, 未).contains(eightWords.hour.branch)) ||
          (巳午未.contains(eightWords.month.branch) && setOf(辰, 戌).contains(eightWords.hour.branch)) ||
          (申酉戌.contains(eightWords.month.branch) && setOf(子, 午).contains(eightWords.hour.branch)) ||
          (亥子丑.contains(eightWords.month.branch) && setOf(寅, 卯).contains(eightWords.hour.branch))
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p閻王關A, p閻王關B)
}

val h鬼門關 = object : IHazardFactory {

  override val hazard: ChildHazard = 鬼門關

  /**
   * 子丑寅生人、酉午未時真。
   * 卯辰巳生人、申戌亥為刑。
   * 午未申生人、莫犯丑寅卯。
   * 酉戌亥生人、子辰巳難乎。
   * 夫鬼門關者，以十二支生人、逢各所值時辰、論小兒時上、並童限逢之不可遠行。 (象吉)
   *
   * 子丑寅生人，巳午未時嗔，
   * 卯辰巳生人，申亥戌為刑，
   * 午未申生命，莫犯丑寅卯，
   * 酉戌亥生命，子巳辰時嗔。
   * 支生取時嗔，限忌遠行。（鰲頭）
   *
   */
  private val p鬼門關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(子, 丑, 寅).contains(eightWords.year.branch) && setOf(酉, 午, 未).contains(eightWords.hour.branch)) ||
          (setOf(卯, 辰, 巳).contains(eightWords.year.branch) && setOf(申, 戌, 亥).contains(eightWords.hour.branch)) ||
          (setOf(午, 未, 申).contains(eightWords.year.branch) && setOf(丑, 寅, 卯).contains(eightWords.hour.branch)) ||
          (setOf(酉, 戌, 亥).contains(eightWords.year.branch) && setOf(子, 辰, 巳).contains(eightWords.hour.branch))
        )
    }
  }

  /**
   * 子嫌酉上午嫌丑，
   * 寅未申卯不須安，
   * 亥怕辰宮戌怕巳，
   * 古賢立定鬼門關。
   * 人命生時莫犯此，
   * 六月炎天也遭寒。
   * 命限逢之、不可遠行。（星平會海）
   *
   * 子嫌酉年午嫌丑，
   * 寅未申卯不須安，
   * 亥怕辰兮戌怕巳，
   * 古賢立號鬼門關。
   * 不宜遠行、勿入陰廟宮寺。
   * 凡甲子丙子戊子生人，犯此忌夜出入門外。（生育禮俗）
   *
   *
   */
  private val p鬼門關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(星平會海, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (setOf(
        Pair(子, 酉),
        Pair(午, 丑),
        Pair(亥, 辰),
        Pair(戌, 巳),
      ).contains(eightWords.year.branch to eightWords.hour.branch)
        )
    }
  }

  /**
   * 凡
   * 子年逢酉時生，
   * 丑年逢午時生，
   * 寅年逢未時生，
   * 卯年逢申時生，
   * 辰年逢亥時生，
   * 巳年逢戌時生，
   * 午年逢丑時生，
   * 未年逢寅時生，
   * 申年逢卯時生，
   * 酉年逢子時生，
   * 戌年逢巳時生，
   * 亥年逢辰時生是。
   * 一生不宜進陰廟、有應公、萬善祠、墳墓區及殯儀館等。宜燒地府錢給地府眾鬼神制化則吉。（黃曆解秘）
   */
  private val p鬼門關C = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (setOf(
        Pair(子, 酉),
        Pair(丑, 午),
        Pair(寅, 未),
        Pair(卯, 申),
        Pair(辰, 亥),
        Pair(巳, 戌),
        Pair(午, 丑),
        Pair(未, 寅),
        Pair(申, 卯),
        Pair(酉, 子),
        Pair(戌, 巳),
        Pair(亥, 辰),
      ).contains(eightWords.year.branch to eightWords.hour.branch)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p鬼門關A, p鬼門關B, p鬼門關C)

}

val h雞飛關 = object : IHazardFactory {

  override val hazard: ChildHazard = 雞飛關

  /**
   * 甲己巳酉丑、孩兒難保守。庚辛亥卯未、父母哭斷腸。壬癸寅午戌、生下不見日。乙戊丙丁子、不過三朝死。此關童命犯之難養，童限遇之亦凶，以午明順。(鰲頭)
   * 甲己巳酉丑、孩兒難保守。庚辛亥卯年、爺娘哭斷腸。壬癸寅午戌、生下不見日。乙戊丙丁子、不過三朝死。(星平會海)
   */
  private val p雞飛關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(鰲頭通書, 星平會海)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(甲, 己).contains(eightWords.day.stem) && 巳酉丑.contains(eightWords.hour.branch)) ||
          (setOf(庚, 辛).contains(eightWords.day.stem) && 亥卯未.contains(eightWords.hour.branch)) ||
          (setOf(壬, 癸).contains(eightWords.day.stem) && 寅午戌.contains(eightWords.hour.branch)) ||
          (setOf(乙, 戊, 丙, 丁).contains(eightWords.day.stem) && eightWords.hour.branch == 子)
        )
    }
  }

  /**
   * 甲乙巳酉丑、孩兒難保守。庚辛亥卯未、父母哭斷腸。壬癸寅午戌、生下不見日。己戊丙丁子、不過三朝死。此關童命犯之難養，夜生不妨，限遇亦凶，以年干生人取用。(象吉)
   */
  private val p雞飛關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(甲, 乙).contains(eightWords.day.stem) && 巳酉丑.contains(eightWords.hour.branch)) ||
          (setOf(庚, 辛).contains(eightWords.day.stem) && 亥卯未.contains(eightWords.hour.branch)) ||
          (setOf(壬, 癸).contains(eightWords.day.stem) && 寅午戌.contains(eightWords.hour.branch)) ||
          (setOf(己, 戊, 丙, 丁).contains(eightWords.day.stem) && eightWords.hour.branch == 子)
        )
    }
  }

  /**
   * 甲己巳酉丑、庚辛亥卯未、壬庚午戌、乙戊丙丁子，不過三日關。勿看殺生，童限難養，夜生不妨。凡辰戌未時生人，犯此忌雞對面啼叫。(生育禮俗)
   */
  private val p雞飛關C = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(甲, 己).contains(eightWords.day.stem) && 巳酉丑.contains(eightWords.hour.branch)) ||
          (setOf(庚, 辛).contains(eightWords.day.stem) && 亥卯未.contains(eightWords.hour.branch)) ||
          (setOf(乙, 戊, 丙, 丁).contains(eightWords.day.stem) && eightWords.hour.branch == 子)
        )
    }
  }

  /**
   * 甲、己日逢巳、酉、丑時生，
   * 乙、丙、丁、戊日逢子時生，
   * 庚日逢亥、卯、未時生，
   * 辛、壬、癸日逢寅、午、戌時生是。
   * 避免孩子看殺雞、殺魚、殺鴨等行為，可燒牛頭馬面錢、或稱牛馬將軍錢，制化即吉。(黃曆解秘)
   */
  private val p雞飛關D = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(甲, 己).contains(eightWords.day.stem) && 巳酉丑.contains(eightWords.hour.branch)) ||
          (庚 == eightWords.day.stem && 亥卯未.contains(eightWords.hour.branch)) ||
          (setOf(辛, 壬, 癸).contains(eightWords.day.stem) && 寅午戌.contains(eightWords.hour.branch)) ||
          (setOf(乙, 丙, 丁, 戊).contains(eightWords.day.stem) && eightWords.hour.branch == 子)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p雞飛關A, p雞飛關B, p雞飛關C, p雞飛關D)
}

val h鐵蛇關 = object : IHazardFactory {

  override val hazard: ChildHazard = 鐵蛇關

  /**
   * 金戌化成鐵，火向未申絕，木辰枝葉枯，水上丑寅滅。此關最凶，童命時上帶、童限更值之則難養。並忌見麻痘凶，壯命行限值之亦有災凶，重則倒壽、十有九驗，以命納音取用，如甲子生人戌時是。(象吉)
   * 金戌化成鐵，火向未申絕，木辰枝葉見，水土丑寅滅。此關最凶，童命時上帶、童限更值之則難養。並忌見麻痘凶，旺命行限值之亦有災，以命納音如甲子生戌時。(鰲頭)
   *
   * 金戌化為鐵，火向未申絕，木辰枝葉枯，水土丑寅滅。童限遇此最忌疹痘疾。此限老少皆忌。(星平會海)  !未考慮納音!
   * 金戌化為鐵，火向未申絕，木辰枝葉枯，水土丑寅滅。鐵蛇來浸害。出疹痘小心，出疹痘前宜制化。凡金木水火土生人，犯此忌痲痘之災。(生育禮俗) !未考慮納音!
   */
  private val p鐵蛇關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書, 星平會海, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return eightWords.year.naYin.fiveElement.let { yearNaYinFiveElement ->
        when (yearNaYinFiveElement) {
          金    -> setOf(戌)
          火    -> setOf(未, 申)
          木    -> setOf(辰)
          水    -> setOf(丑, 寅)
          else -> emptySet()
        }.contains(eightWords.hour.branch)
      }
    }
  }

  /**
   * 生年屬金逢卯、戌時，
   * 生年屬木逢酉、辰時，
   * 生年屬火逢子、未、申時，
   * 生年屬水、土逢丑、寅、午時是。
   * 兒時出痲疹、疹痘須特別小心，也容易遭動物咬傷，若遭動物咬傷速就醫，回家用陰陽水燒化銅蛇鐵狗錢於其中則吉。(黃曆解秘)
   *
   * 生年屬金，逢卯、戌時；
   * 木年逢酉、辰時；
   * 火年逢子、申、未時；
   * 水年、土年逢午、丑、寅時生的人，才會犯到這個關煞。 (小兒關煞圖)
   * 這些人在兒童時期出疹痘、麻疹時要特別小心照料，否則身體上會留下許多像被狗或蛇咬過的疤痕，為了避免意外發生，最好還是找機會燒一些銅蛇鐵狗錢，制化那些不祥的蛇拘貓們。
   * 如果孩子已遭到這些動物咬傷，則表示這些動物已在償冤報，人們不得怨恨，追打那些動物，只得趁機燒一盆熱水，再倒入相當的冷水，成了冷（陰）熱（陽）調和的陰陽水，再燒化銅蛇鐵狗錢於其中，用這種陰陽水來洗傷口或塗抹傷口治療，很快就可以痊癒。
   */
  private val p鐵蛇關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘, 小兒關煞圖)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return SimpleBranch.getFiveElement(eightWords.year.branch).let { yearFiveElement ->
        when (yearFiveElement) {
          金 -> setOf(卯, 戌)
          木 -> setOf(酉, 辰)
          火 -> setOf(子, 未, 申)
          水 -> setOf(丑, 寅, 午)
          土 -> setOf(丑, 寅, 午)
        }.contains(eightWords.hour.branch)
      }
    }
  }

  override val impls: Set<IHazard> = setOf(p鐵蛇關A, p鐵蛇關B)
}

val h斷橋關 = object : IHazardFactory {

  override val hazard: ChildHazard = 斷橋關

  /**
   * 正寅二兔三猴走，
   * 四月耕牛懶下田，
   * 五犬六雞門外立，
   * 七龍戲水八蛇纏，
   * 九馬十羊十一豬，
   * 冬季老鼠鬧喧喧。
   * 此關以月建遇十二時支論，乃指南中第一關，小兒生時帶著難養，
   * 壯命行限值之、加以刻度凶星到，必倒壽無有不驗。犯此忌過橋、汲水照影。宜用水官錢祭祀水官大帝。(象吉)、(鰲頭)
   *
   * 一虎二兔三猿子，
   * 四牛五犬六雞鬥，
   * 七龍八蛇九馬丁，
   * 十羊十一豬十二鼠。
   * 勿渡舟、過竹橋、遙籃，幼時難養、壯年亦忌。凡正二月寅卯時生人，犯此忌過橋汲水照影。(生育禮俗)
   */
  private val p斷橋關 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return setOf(
        Pair(寅, 寅),
        Pair(卯, 卯),
        Pair(辰, 申),
        Pair(巳, 丑),
        Pair(午, 戌),
        Pair(未, 酉),
        Pair(申, 辰),
        Pair(酉, 巳),
        Pair(戌, 午),
        Pair(亥, 未),
        Pair(子, 亥),
        Pair(丑, 子),
      ).contains(eightWords.month.branch to eightWords.hour.branch)
    }
  }

  override val impls: Set<IHazard> = setOf(p斷橋關)
}

val h落井關 = object : IHazardFactory {

  override val hazard: ChildHazard = 落井關

  /**
   * 甲己見蛇傷，乙庚鼠內藏，丙辛猴覓果，丁壬犬吠汪，戊癸愁逢兔，孩兒有水殃。此關以年干取、如甲己生人巳時是，君流年浮沉合之少壯命皆有水災之厄，切宜防慎吉。(象吉)
   * 甲己見蛇傷，乙庚鼠內藏，丙辛猴見果，丁壬犬吠汪，戊癸愁逢兔，孩兒有水殃。如甲己生人巳時是，若流年浮沉合之災，旺命該有水厄之災，切宜防吉。(鰲頭)
   * 甲己見蛇傷，乙庚鼠內藏，丙辛猴覓果，丁壬犬吠汪，戊癸愁逢兔，孩兒有水殃。(星平會海)
   * 甲己見蛇傷，乙庚鼠內藏，丙辛猴見果，丁壬犬吠汪，戊癸愁逢兔，孩兒有水殃。勿近井水邊、渡舟、游泳有水厄之災。凡午巳卯申戌時生人，犯此忌見井泉、池塘、涌溪。(生育禮俗)
   *
   */
  private val p落井關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書, 星平會海, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return setOf(
        Pair(土, 巳),
        Pair(金, 子),
        Pair(水, 申),
        Pair(木, 戌),
        Pair(火, 卯),
      ).contains(eightWords.year.stem.fiveElement to eightWords.hour.branch)
    }
  }

  /**
   * 凡甲己日逢巳時，乙庚日逢子時，丙辛日逢申時，丁壬日逢戌時，戊癸日逢卯時生人是。犯此須遠離井泉、池塘、河邊、溪流、海邊避免遭厄難。(黃曆解秘)
   */
  private val p落井關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return setOf(
        Pair(土, 巳),
        Pair(金, 子),
        Pair(水, 申),
        Pair(木, 戌),
        Pair(火, 卯),
      ).contains(eightWords.day.stem.fiveElement to eightWords.hour.branch)
    }
  }

  override val impls: Set<IHazard> = setOf(p落井關A, p落井關B)

}

val h四柱關 = object : IHazardFactory {

  override val hazard: ChildHazard = 四柱關

  /**
   * 正七休生巳亥時，二八辰戌不堪推，三九卯酉生兒惡，四十寅申主哭悲。五十一月丑未死，六十二月子午啼。此關俗忌坐轎車、止忌生時，帶著童限不忌，大抵亦無甚凶。(象吉)
   * 正七休生巳亥時，二八辰戌不堪推，三九卯酉生兇惡，四十寅申主哭悲。五十一月丑未死，六十二月子午真。此關俗雲忌坐轎車、存忌，止忌生時，帶童限不忌。(鰲頭)
   * 正七休生巳亥時，二八辰戌不堪推，三九卯酉主人惡，四十寅申主交悲。五十一逢丑未死，六十二子午非宜。小兒若犯此關煞，父母不久主分離。(星平會海)
   * 正七巳亥時，二八辰戌時，三九卯酉時，四十寅申時，五十一丑未時，六十二子午時。與父母暫分離，俗稱忌座轎子而已。凡巳亥年正二月辰寅時生人，犯此忌坐桿竹椅太早。(生育禮俗)
   */
  private val p四柱關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書, 星平會海, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(寅, 申).contains(eightWords.month.branch) && setOf(巳, 亥).contains(eightWords.hour.branch)) ||
          (setOf(卯, 酉).contains(eightWords.month.branch) && setOf(辰, 戌).contains(eightWords.hour.branch)) ||
          (setOf(辰, 戌).contains(eightWords.month.branch) && setOf(卯, 酉).contains(eightWords.hour.branch)) ||
          (setOf(巳, 亥).contains(eightWords.month.branch) && setOf(寅, 申).contains(eightWords.hour.branch)) ||
          (setOf(午, 子).contains(eightWords.month.branch) && setOf(丑, 未).contains(eightWords.hour.branch)) ||
          (setOf(未, 丑).contains(eightWords.month.branch) && setOf(子, 午).contains(eightWords.hour.branch))
        )
    }
  }

  /**
   * 凡巳、亥年正、二月辰、巳時生人，犯此忌坐欄杆、竹椅太早。(黃曆解秘)
   */
  private val p四柱關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        setOf(巳, 亥).contains(eightWords.year.branch) &&
          setOf(寅, 卯).contains(eightWords.month.branch) &&
          setOf(辰, 巳).contains(eightWords.hour.branch)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p四柱關A, p四柱關B)
}

val h短命關 = object : IHazardFactory {

  override val hazard: ChildHazard = 短命關

  /**
   * 寅午戌龍當，巳酉丑虎亡，申子辰蛇上，亥卯未尋羊。此關生時上帶、主驚呼夜啼難養，如日干健則無事，日主弱凶。(象吉)
   * 寅午戌生龍位當，巳酉丑人在虎亡，申子辰生蛇頭立，亥卯未人去尋羊。此關生時上帶、主驚呼夜啼難養，如日干旺健則可無事矣。(鰲頭)
   * 寅午戌龍當，巳酉丑虎亡，申子辰蛇上，亥卯未尋羊。時上見主驚叫夜啼患。(星平會海)
   * 寅午戌龍當，巳酉丑虎郎，申子辰蛇上，亥卯未尋羊。時上見主驚叫夜啼。限內主小心看護，宜制化方可平安。凡子辰年巳時生人，犯此主多驚怖、夜啼之患。(生育禮俗)
   * 凡申、子、辰年逢巳時，巳、酉、丑年逢寅時，寅、午、戌年逢辰時，亥、卯、未年逢未時出生人是。犯此主多驚怖、夜啼之患。(黃曆解秘)
   */
  private val p短命關 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書, 星平會海, 生育禮俗, 黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return when (eightWords.year.branch.trilogy()) {
        火    -> 辰
        金    -> 寅
        水    -> 巳
        木    -> 未
        else -> throw IllegalArgumentException("error")
      }.let { branch ->
        (branch == eightWords.hour.branch)
      }
    }
  }

  override val impls: Set<IHazard> = setOf(p短命關)
}

val h浴盆關 = object : IHazardFactory {

  override val hazard: ChildHazard = 浴盆關

  /**
   * 浴盆之煞最無良，春月忌龍夏忌羊，秋季犬兒須切忌，冬月逢牛定主傷。此生下地之時不可用腳盆，須用鐵鍋、火盆之類，洗之後無忌也，只忌月內一周之外不忌。(象吉)
   * 浴盆之煞最無良，春忌龍兮夏忌羊，秋忌犬兒須切忌，冬月逢丑定須防。出母勿用浴盆。(星平會海)
   * 浴盆之煞四季論，春忌龍兮夏忌羊，三秋忌犬冬忌丑，小兒初浴要提防。小兒初次洗浴時小心，解厄平安。正、二、三月申時生人，犯此忌沐浴太早。(生育禮俗)
   *
   * 浴盆之煞最無良，春忌龍兮夏忌羊，秋季人見切須忌，冬月逢午定主傷。此煞生子俗時不用瓦盆，宜用鐵銅大盆洗之後無忌。(鰲頭) (人 -> 犬? , 午 -> 牛? , 應該是抄錄錯誤, 不採用)
   */
  private val p浴盆關 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 星平會海, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && 辰 == eightWords.hour.branch) ||
          (巳午未.contains(eightWords.month.branch) && 未 == eightWords.hour.branch) ||
          (申酉戌.contains(eightWords.month.branch) && 戌 == eightWords.hour.branch) ||
          (亥子丑.contains(eightWords.month.branch) && 丑 == eightWords.hour.branch)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p浴盆關)
}

val h湯火關 = object : IHazardFactory {

  override val hazard: ChildHazard = 湯火關

  /**
   * 子午卯酉休逢馬，寅申巳亥虎驚人，辰戌丑未羊相觸，常防湯火厄相侵。此殺如子午卯酉生人忌午時，若小兒犯此招湯火之災，亦忌此限。蓋人生水火乃養生之要、何能免乎，知防慎則吉矣。(象吉)
   * 子午卯酉休遇馬，寅申巳亥虎驚人，辰戌丑未羊相撞，常防湯火厄相侵。其煞如子午卯酉生人忌午時，若小兒犯此招湯火之厄。(鰲頭)
   * 子午卯酉休騎馬，寅申巳亥虎驚人，辰戌丑未羊相觸，常防湯火厄相侵。（星平會海）
   * 子午卯酉莫逢馬，寅申巳亥怕見虎，辰戌丑未若遇羊，湯火傷身切須防。限內湯火油小心，宜制化保安寧。(生育禮俗)
   */
  private val p湯火關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書, 星平會海, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (子午卯酉.contains(eightWords.year.branch) && eightWords.hour.branch == 午) ||
          (寅申巳亥.contains(eightWords.year.branch) && eightWords.hour.branch == 寅) ||
          (辰戌丑未.contains(eightWords.year.branch) && eightWords.hour.branch == 未)
        )
    }
  }

  /**
   * 凡子、午、卯、酉年午時生人，犯此忌疵痲之患。另：丑、申、亥年逢未時，寅年逢巳時，卯年逢子時，午、酉年逢寅時出生人是。用火神錢制化、或到火神廟拜拜即吉。(黃曆解秘)
   */
  private val p湯火關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (子午卯酉.contains(eightWords.year.branch) && eightWords.hour.branch == 午) ||
          (setOf(丑, 申, 亥).contains(eightWords.year.branch) && eightWords.hour.branch == 未) ||
          (寅 == eightWords.year.branch && eightWords.hour.branch == 巳) ||
          (setOf(午, 酉).contains(eightWords.year.branch) && eightWords.hour.branch == 寅)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p湯火關A, p湯火關B)
}

val h水火關 = object : IHazardFactory {

  override val hazard: ChildHazard = 水火關

  /**
   * 春月生戌未，夏月見丑辰，秋月生丑戌，冬月見未辰。須防水火之災。(星平會海)
   * 春月生人見戌未，夏月生人見丑辰，秋月生人見丑戌，冬月生人見未辰。須防水厄、火湯油之災，解化保平安。凡正二三月未戌時生人，犯此主膿血、瘡疾太多及小心水火。(生育禮俗)
   */
  private val p水火關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(星平會海, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && setOf(戌, 未).contains(eightWords.hour.branch)) ||
          (巳午未.contains(eightWords.month.branch) && setOf(丑, 辰).contains(eightWords.hour.branch)) ||
          (申酉戌.contains(eightWords.month.branch) && setOf(丑, 戌).contains(eightWords.hour.branch)) ||
          (亥子丑.contains(eightWords.month.branch) && setOf(未, 辰).contains(eightWords.hour.branch))
        )
    }
  }

  /**
   * 凡正、二、三月逢未、戌時生人，四、五、六月逢丑、辰時生人，七、八、九月逢酉時生人，十、十一、十二月逢丑時生人是。犯此主膿血、瘡疾太多及小心水火。民俗制化法：祭火神及水官大帝，用火神錢拜火神、用水官錢拜水官大帝。(黃曆解秘)
   */
  private val p水火關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && setOf(戌, 未).contains(eightWords.hour.branch)) ||
          (巳午未.contains(eightWords.month.branch) && setOf(丑, 辰).contains(eightWords.hour.branch)) ||
          (申酉戌.contains(eightWords.month.branch) && setOf(酉).contains(eightWords.hour.branch)) ||
          (亥子丑.contains(eightWords.month.branch) && setOf(丑).contains(eightWords.hour.branch))
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p水火關A, p水火關B)

}

val h深水關 = object : IHazardFactory {

  override val hazard: ChildHazard = 深水關

  /**
   * 春忌寅申夏忌羊，秋生雞嘴實堪傷，三冬切忌牛生角。十個孩兒九個亡。童限最忌痲痘災。(星平會海)
   * 春忌寅申時，夏怕未時當，秋季嫌酉時，冬天勿丑時。清明、七夕之日主不拜、多病多災。正二三月寅申時生人，犯此忌病疹災害。(生育禮俗)
   */
  private val p深水關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(星平會海, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && setOf(寅, 申).contains(eightWords.hour.branch)) ||
          (巳午未.contains(eightWords.month.branch) && 未 == eightWords.hour.branch) ||
          (申酉戌.contains(eightWords.month.branch) && 酉 == eightWords.hour.branch) ||
          (亥子丑.contains(eightWords.month.branch) && 丑 == eightWords.hour.branch)
        )
    }
  }

  /**
   * 凡正、二、三月寅、申時生人，犯此忌病疹災害。俗云：犯此關之人與前世父母糾纏不清，應該避免在清明節、端午節、中秋節、除夕祭拜祖先，滿月及周歲皆要提前一天舉行。(黃曆解秘)
   */
  private val p深水關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (寅卯辰.contains(eightWords.month.branch) && setOf(寅, 申).contains(eightWords.hour.branch))
    }
  }

  override val impls: Set<IHazard> = setOf(p深水關A, p深水關B)

}

val h夜啼關 = object : IHazardFactory {

  override val hazard: ChildHazard = 夜啼關


  /**
   * 子午卯酉單怕羊，寅申巳亥虎羊當，辰戌丑未雞常叫，連霄不睡到天光，春人怕馬夏逢雞，秋子冬卯不暫移，小兒若犯此關煞，定是三周半夜啼。此關利害難治，只是兩樣起例不同，因並錄之、云後一例有驗。(象吉)
   * 子午卯酉單怕羊，寅申巳亥虎羊當，辰戌丑未雞常叫，連霄不睡到天光，春馬夏雞秋怕子，冬兔三周半夜啼。(星平會海)
   */
  private val p夜啼關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 星平會海)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (子午卯酉.contains(eightWords.month.branch) && 未 == eightWords.hour.branch) ||
          (寅申巳亥.contains(eightWords.month.branch) && setOf(寅, 未).contains(eightWords.hour.branch)) ||
          (辰戌丑未.contains(eightWords.month.branch) && 酉 == eightWords.hour.branch) ||
          (寅卯辰.contains(eightWords.month.branch) && 午 == eightWords.hour.branch) ||
          (巳午未.contains(eightWords.month.branch) && 酉 == eightWords.hour.branch) ||
          (申酉戌.contains(eightWords.month.branch) && 子 == eightWords.hour.branch) ||
          (亥子丑.contains(eightWords.month.branch) && 卯 == eightWords.hour.branch)
        )
    }
  }

  /**
   * 子午卯酉單怕羊，寅申巳亥處牛當，辰戌丑未蟬常叫，連霄不睡到天光，春人怕馬夏逢雞，秋子冬卯不暫移，小兒若犯此關煞，定有三周半夜啼。此關利害難治，只是兩樣起例不同，因並錄之、云後則有驗。(鰲頭)
   */
  private val p夜啼關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(鰲頭通書)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (子午卯酉.contains(eightWords.month.branch) && 未 == eightWords.hour.branch) ||
          (寅申巳亥.contains(eightWords.month.branch) && 丑 == eightWords.hour.branch) ||
          // (辰戌丑未.contains(eightWords.month.branch) && 酉 == eightWords.hour.branch) || (蟬?)
          (寅卯辰.contains(eightWords.month.branch) && 午 == eightWords.hour.branch) ||
          (巳午未.contains(eightWords.month.branch) && 酉 == eightWords.hour.branch) ||
          (申酉戌.contains(eightWords.month.branch) && 子 == eightWords.hour.branch) ||
          (亥子丑.contains(eightWords.month.branch) && 卯 == eightWords.hour.branch)
        )
    }
  }

  /**
   * 辰戌丑未虎雞當，子午卯酉喜怕羊，寅申巳亥羊又忌，兒命犯之定有殃。限內夜間不要火，逢者制化自安。凡子午丑未時生人，犯此主夜間啾唧不寧。(生育禮俗)
   */
  private val p夜啼關C = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (子午卯酉.contains(eightWords.month.branch) && 未 == eightWords.hour.branch) ||
          (寅申巳亥.contains(eightWords.month.branch) && 未 == eightWords.hour.branch) ||
          (辰戌丑未.contains(eightWords.month.branch) && 寅 == eightWords.hour.branch)
        )
    }
  }

  /**
   * 凡正二三月逢午時生，四五六月逢酉時生，七八九月逢子時生，十十一十二月逢卯時生是。犯此主夜間啾唧不寧，制化保平安。(黃曆解秘)
   */
  private val p夜啼關D = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && 午 == eightWords.hour.branch) ||
          (巳午未.contains(eightWords.month.branch) && 酉 == eightWords.hour.branch) ||
          (申酉戌.contains(eightWords.month.branch) && 子 == eightWords.hour.branch) ||
          (亥子丑.contains(eightWords.month.branch) && 卯 == eightWords.hour.branch)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p夜啼關A, p夜啼關B, p夜啼關C, p夜啼關D)
}

val h白虎關 = object : IHazardFactory {

  override val hazard: ChildHazard = 白虎關

  /**
   * (季節來看?)
   * 火人白虎鬚在子，金人白虎在卯方，水土生人白虎午，木人白虎酉中藏。此關時上帶，主見驚風之症，又忌出痘時帶難養，童限遇之則有血光損傷之厄。(象吉)
   * 火人白虎鬚在子，金人白虎在卯方，水土生人白虎午，木人白虎酉中藏。此關時上帶，主見驚風之症，又忌出痘時帶難養，童限遇之則有血光損傷之厄。(鰲頭)
   * 火人白虎鬚在子，金人白虎卯之方，水土生人白虎午，木人白虎酉中藏。主有血光、驚風、傷損。(星平會海)
   */
  private val p白虎關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書, 星平會海)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (when (SimpleBranch.getFiveElement(eightWords.month.branch)) {
        火    -> 子
        金    -> 卯
        水, 土 -> 午
        木    -> 酉
      } == eightWords.hour.branch
        )
    }
  }

  /**
   * 正二月犯申酉時，三月犯子怕戌時，四六月犯卯丑時，八十月犯卯時當。出痲小心。凡金木水火土生人，犯此主多血光災厄。(生育禮俗)
   */
  private val p白虎關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(寅, 卯).contains(eightWords.month.branch) && setOf(申, 酉).contains(eightWords.hour.branch)) ||
          (辰 == eightWords.month.branch && setOf(子, 戌).contains(eightWords.hour.branch)) ||
          (setOf(巳, 未).contains(eightWords.month.branch) && setOf(卯, 丑).contains(eightWords.hour.branch)) ||
          (setOf(酉, 亥).contains(eightWords.month.branch) && 卯 == eightWords.hour.branch)
        )
    }
  }

  /**
   * 凡金、木、水、火、土生人，犯此多主血光災厄。
   * 另：
   * 金年逢卯、戌時，
   * 木年逢辰、酉時，
   * 火年逢子、未、申時，
   * 水年、土年逢丑、寅、午時生人是。燒白虎錢制化。(黃曆解秘)
   */
  private val p白虎關C = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (when (SimpleBranch.getFiveElement(eightWords.year.branch)) {
        金    -> setOf(卯, 戌)
        木    -> setOf(辰, 酉)
        火    -> setOf(子, 未, 申)
        水, 土 -> setOf(丑, 寅, 午)
      }.contains(eightWords.hour.branch)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p白虎關A, p白虎關B, p白虎關C)
}

val h天狗關 = object : IHazardFactory {

  override val hazard: ChildHazard = 天狗關

  /**
   * 子人見戌丑人亥，寅人見子卯人丑，辰人見寅巳人卯，午人見辰未人巳，申人見午酉人未，戌人見申亥人酉。此即天狗星是也，如子生人則從戌上起子順行亥卯丑寅子丑辰寅巳卯也，小兒行年童限值之有驚怖血光之疾，命中時上帶則有狗傷之厄，切宜防之、慎之。(象吉)
   * 子人見戌丑人亥，寅人見子卯人丑，辰人見寅巳人卯，午人見辰未人巳，申人見午酉人未，戌人見申亥人酉。此即天狗星是也，如子生人則從戌上起子順行丑亥寅子卯丑辰寅巳卯也，小兒行年童限值之有驚風血光之疾，命中時上帶則有狗傷之厄，切宜防之、慎之。(鰲頭)
   * 子人見戌丑人亥，寅人見子卯見丑，辰人見寅巳見卯，午人見辰未見巳，申人見午酉見未，戌人見申亥見酉。(星平會海)
   * 子見戌丑見亥，寅見子卯見丑，辰見寅巳見卯，午見辰未見巳，申見午酉見未，戌見申亥見酉。此關有血光、傷害、破相等，宜制化。凡八字五行全者生人，犯此月內怕聞犬吠聲。(生育禮俗)
   * 凡子年逢戌時，丑年逢亥時，寅年逢子時，卯年逢丑時，辰年逢寅時，巳年逢卯時，午年逢辰時，未年逢巳時，申年逢午時，酉年逢未時，戌年逢申時，亥年逢酉時出生人是。犯此月內怕聞犬吠聲。小孩易有顏面傷害破相，刀、剪、鑽、針須收好，用天狗錢制天狗煞。(黃曆解秘)
   */
  private val p天狗關 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書, 星平會海, 生育禮俗, 黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (Branch.entries
        .map { b -> b to b.prev(2) }
        .toSet().contains(eightWords.year.branch to eightWords.hour.branch)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p天狗關)
}

val h四季關 = object : IHazardFactory {

  override val hazard: ChildHazard = 四季關

  /**
   * 春生巳丑不為祥，夏遇申辰惹禍殃，秋季豬羊都不吉，冬逢虎兔兩傷亡。此關詳考乃四季天地荒蕪日，人命逢此日有始無終，苗而不秀亦難養。(象吉)
   * 春生巳丑不為祥，夏遇申辰惹禍殃，秋季豬羊都不吉，冬逢虎兔兩相傷。此關乃四季天地荒蕪日，人命逢此日有始無終，苗而不秀亦難養。(鰲頭)
   */
  private val p四季關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && setOf(巳, 丑).contains(eightWords.hour.branch)) ||
          (巳午未.contains(eightWords.month.branch) && setOf(申, 辰).contains(eightWords.hour.branch)) ||
          (申酉戌.contains(eightWords.month.branch) && setOf(亥, 未).contains(eightWords.hour.branch)) ||
          (亥子丑.contains(eightWords.month.branch) && setOf(寅, 卯).contains(eightWords.hour.branch))
        )
    }
  }

  /**
   * 春關牛與蛇，夏生龍猴嗟，秋忌羊豬位，冬犬虎交加。忌四位全只一位不為關，名兩頭關閑也。又命逢之，有始無終。一名月關，正月忌巳、十月忌亥，余同此。(星平會海)
   * 春忌牛與蛇，夏生龍猴差，秋怕豬羊位，冬犬虎交加。四季小心，苗而不秀，必然難養多疾。凡正、二、三月壬辰時生人，犯此關忌一歲，出入凶、喜事。(生育禮俗)
   *
   * 凡正、二、三月壬辰時生人，犯此關忌一歲，出入凶、喜事。
   * 另：
   * 正、二、三月丑、巳時，
   * 四、五、六月辰、申時，
   * 七、八、九月亥、未時，
   * 十、十一、十二月寅、戌時生人是。季節交換注意身體保護。(黃曆解秘)
   */
  private val p四季關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(星平會海, 生育禮俗, 黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && setOf(巳, 丑).contains(eightWords.hour.branch)) ||
          (巳午未.contains(eightWords.month.branch) && setOf(申, 辰).contains(eightWords.hour.branch)) ||
          (申酉戌.contains(eightWords.month.branch) && setOf(亥, 未).contains(eightWords.hour.branch)) ||
          (亥子丑.contains(eightWords.month.branch) && setOf(寅, 戌).contains(eightWords.hour.branch))
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p四季關A, p四季關B)
}

val h急腳關 = object : IHazardFactory {

  override val hazard: ChildHazard = 急腳關

  /**
   * 春忌亥子不過關，夏逢卯未在中間，秋季寅戌還須忌，冬月辰戌死不難。此關即八座殺，惟忌修造動土凶。(象吉)
   * 春忌亥子不過關，夏逢卯未在中間，秋季寅戌還須忌，冬月辰戌死不難。此關即八座，惟忌修造動土犯之主凶。(鰲頭)
   */
  private val p急腳關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && setOf(亥, 子).contains(eightWords.hour.branch)) ||
          (巳午未.contains(eightWords.month.branch) && setOf(卯, 未).contains(eightWords.hour.branch)) ||
          (申酉戌.contains(eightWords.month.branch) && setOf(寅, 戌).contains(eightWords.hour.branch)) ||
          (亥子丑.contains(eightWords.month.branch) && setOf(辰, 戌).contains(eightWords.hour.branch))
        )
    }
  }

  /**
   * 三春亥子不過關，夏月卯未實堪嗔，秋寅戌位還須忌，冬丑辰宮死弗難。(星平會海)
   * 二春亥子難過關，夏逢卯未實堪傷，秋寅戌位還須忌，冬丑辰宮死不難。修造、動土勿看，限內宜制化。此正二三月子亥時生人，犯此忌驚嚇、跌扑之患。(生育禮俗)
   * 凡正、二、三月逢子、亥時生人，四、五、六月逢卯、未時生人，七、八、九月逢寅、戌時生人，十、十一、十二月逢丑、辰時生人是。犯此須避動土、修造、開挖事。宜用山神土地錢制化。(黃曆解秘)
   */
  private val p急腳關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(星平會海, 生育禮俗, 黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && setOf(亥, 子).contains(eightWords.hour.branch)) ||
          (巳午未.contains(eightWords.month.branch) && setOf(卯, 未).contains(eightWords.hour.branch)) ||
          (申酉戌.contains(eightWords.month.branch) && setOf(寅, 戌).contains(eightWords.hour.branch)) ||
          (亥子丑.contains(eightWords.month.branch) && setOf(丑, 戌).contains(eightWords.hour.branch))
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p急腳關A, p急腳關B)
}

val h急腳煞 = object : IHazardFactory {

  override val hazard: ChildHazard = 急腳煞

  /**
   * 甲乙命人申酉是，丙丁亥子實堪悲，戊己怕寅卯上逢，庚辛巳午不須疑，壬癸切須防丑未，更加辰戌命遭危。此煞如甲乙年生人遇申酉時是也，主幼小之年難養。(象吉)
   * 甲乙命人申酉是，丙丁亥子實堪悲，戊己怕逢寅卯字，庚辛巳午不須防，壬癸切須防丑未，更加辰戌命遭殃。此煞如甲乙年生人遇申酉時是也，主幼小之年難養。(鰲頭)
   * 甲乙命人申酉是，丙丁亥子實堪悲，戊己怕逢寅卯上，庚辛巳午不須宜，壬癸切須防丑未，更加辰戌命遭厄。即同八座忌犯修造、動土。(星平會海)
   */
  private val p急腳煞 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書, 星平會海)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(甲, 乙).contains(eightWords.day.stem) && setOf(申, 酉).contains(eightWords.hour.branch)) ||
          (setOf(丙, 丁).contains(eightWords.day.stem) && setOf(亥, 子).contains(eightWords.hour.branch)) ||
          (setOf(戊, 己).contains(eightWords.day.stem) && setOf(寅, 卯).contains(eightWords.hour.branch)) ||
          (setOf(庚, 辛).contains(eightWords.day.stem) && setOf(巳, 午).contains(eightWords.hour.branch)) ||
          (setOf(壬, 癸).contains(eightWords.day.stem) && setOf(丑, 未, 辰, 戌).contains(eightWords.hour.branch))
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p急腳煞)
}

val h五鬼關 = object : IHazardFactory {

  override val hazard: ChildHazard = 五鬼關

  /**
   * 年 -> 時
   *
   * 子人見辰丑人見卯，寅人見寅卯人見丑，辰人見子巳人見亥，午人見戌未人見申，申人見酉酉人見未，戌人見午亥人見巳。此關只是死氣也，四柱多見難養，童限值之主有跌傷。(象吉)
   * 子人見辰丑人卯，寅人見寅卯人丑，辰人見子巳人亥，午人見戌未人申，申人見酉酉人未，戌人見午亥人巳。此關只是死氣也，四柱多見難養，童限值之主有跌傷之患。(鰲頭)
   */
  private val p五鬼關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (setOf(
        Pair(子, 辰),
        Pair(丑, 卯),
        Pair(寅, 寅),
        Pair(卯, 丑),
        Pair(辰, 子),
        Pair(巳, 亥),
        Pair(午, 戌),
        Pair(未, 申),
        Pair(申, 酉),
        Pair(酉, 未),
        Pair(戌, 午),
        Pair(亥, 巳)
      ).contains(eightWords.year.branch to eightWords.hour.branch)
        )
    }
  }

  /**
   * 凡子年逢辰時，丑年逢卯時，寅年逢寅時，卯年逢丑時，辰年逢子時，巳年逢亥時，午年逢戌時，未年逢酉時，申年逢申時，酉年逢未時，戌年逢午時，亥年逢巳時出生人是。犯此忌近病殯儀館、墓地、亂葬崗、棺木店及入庵堂、寺觀。(黃曆解秘)
   */
  private val p五鬼關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (setOf(
        Pair(子, 辰),
        Pair(丑, 卯),
        Pair(寅, 寅),
        Pair(卯, 丑),
        Pair(辰, 子),
        Pair(巳, 亥),
        Pair(午, 戌),
        Pair(未, 酉),
        Pair(申, 申),
        Pair(酉, 未),
        Pair(戌, 午),
        Pair(亥, 巳)
      ).contains(eightWords.year.branch to eightWords.hour.branch)
        )
    }
  }

  /**
   * 子龍丑蛇寅馬宮，卯羊辰猿巳雞中，午犬未豬申角鼠，酉牛戌虎亥兔逢。(星平會海)
   * 子忌龍丑忌蛇，寅忌馬卯忌羊，辰忌猿巳忌雞，午忌犬未忌豬，申忌鼠酉忌牛，戌忌虎亥忌兔。限內勿過冢埔，勿近棺木板。凡壬子庚子丙子戊寅年寅時生人，犯此忌入庵堂、寺觀。(生育禮俗)
   */
  private val p五鬼關C = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(星平會海, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (setOf(
        Pair(子, 辰),
        Pair(丑, 巳),
        Pair(寅, 午),
        Pair(卯, 未),
        Pair(辰, 申),
        Pair(巳, 酉),
        Pair(午, 戌),
        Pair(未, 亥),
        Pair(申, 子),
        Pair(酉, 丑),
        Pair(戌, 寅),
        Pair(亥, 卯)
      ).contains(eightWords.year.branch to eightWords.hour.branch)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p五鬼關A, p五鬼關B, p五鬼關C)
}

val h金鎖關 = object : IHazardFactory {

  override val hazard: ChildHazard = 金鎖關

  /**
   * 正七逢申人必死，二八雞哥命必厄，三九犬兒尋聲吠，四十逢豬是鎖匙，五十一逢子巳死，六十二與丑非奇。(星平會海)
   */
  private val p金鎖關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(星平會海)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (setOf(
        Pair(寅, 申),
        Pair(卯, 酉),
        Pair(辰, 戌),
        Pair(巳, 亥),
        Pair(午, 子),
        Pair(未, 丑),
        Pair(申, 申),
        Pair(酉, 酉),
        Pair(戌, 戌),
        Pair(亥, 亥),
        Pair(子, 子),
        Pair(丑, 丑),
      ).contains(eightWords.month.branch to eightWords.hour.branch)
        )
    }
  }

  /**
   * 正七月申時，二八月酉時，三九月戌時，四十月亥時，五十一月子時，六十二月丑時。童限忌帶金銀鎖片、錢索等物。凡人生正二月申卯時，生人犯此忌帶金銀器物。(生育禮俗)
   * 凡正、二月申、卯時生人，犯此忌帶金銀器物。另：正、七月申時，二、八月酉時，三、九月戌時，四、十月亥時，五、十一月子時，六、十二月丑時生人是。少帶金銀飾品即吉。(黃曆解秘)
   */
  private val p金鎖關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(生育禮俗, 黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {

      return (setOf(
        Pair(寅, 申),
        Pair(卯, 酉),
        Pair(辰, 戌),
        Pair(巳, 亥),
        Pair(午, 子),
        Pair(未, 丑),
        Pair(申, 申),
        Pair(酉, 酉),
        Pair(戌, 戌),
        Pair(亥, 亥),
        Pair(子, 子),
        Pair(丑, 丑),
      ).contains(eightWords.month.branch to eightWords.hour.branch)
        || (setOf(寅, 卯).contains(eightWords.month.branch) && setOf(申, 卯).contains(eightWords.hour.branch))
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p金鎖關A, p金鎖關B)
}

val h直難關 = object : IHazardFactory {

  override val hazard: ChildHazard = 直難關

  /**
   * ? 小兒最怕逢直難，羊刃劫殺不須輪，甲壬戊旬逆申起，庚丙旬人數起寅，數至本年方是殺，三九六十二為真，小兒若是逢斯難，父母徒然生此身。此關主多啾唧、星卻不為害，琴堂所說更靈，而此復載隨人用。(象吉)
   * ? 小兒最怕逢直難，羊刃劫煞不須輪，甲壬戊旬逆申起，庚丙旬人數起字，數至本年方是煞，三九六十二為真，小兒若是逢此難，父母徒然生此身。此關主多啾唧、星辰卻不為大害，琴堂所說更靈，而此復載隨人用。(鰲頭)
   * ? 小兒最怕逢直難，羊刃劫煞不須論，甲壬戊旬逆申起，庚丙旬人數起寅，數至本年方是煞，三九六十二為真，小兒若是逢斷煞，父母徒勞生此身。甲子壬子戊子旬生人，甲上起子逆數，庚子戊子旬入庚上起子數，木甲上起一歲是命宮，佯若逢三六九宜數也。(星平會海)
   * 凡寅、卯月逢午時生人，辰、巳月逢未時生人，午、未月逢卯、戌時生人，申、酉月逢巳、申時生人，戌、亥月逢寅、卯時生人，子、丑月逢辰、酉時生人是。易受又硬又直的利器所傷。(黃曆解秘)
   */
  private val p直難關 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(寅, 卯).contains(eightWords.month.branch) && 午 == eightWords.hour.branch) ||
          (setOf(辰, 巳).contains(eightWords.month.branch) && 未 == eightWords.hour.branch) ||
          (setOf(午, 未).contains(eightWords.month.branch) && setOf(卯, 戌).contains(eightWords.hour.branch)) ||
          (setOf(申, 酉).contains(eightWords.month.branch) && setOf(巳, 申).contains(eightWords.hour.branch)) ||
          (setOf(戌, 亥).contains(eightWords.month.branch) && setOf(寅, 卯).contains(eightWords.hour.branch)) ||
          (setOf(子, 丑).contains(eightWords.month.branch) && setOf(辰, 酉).contains(eightWords.hour.branch))
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p直難關)
}

val h取命關 = object : IHazardFactory {

  override val hazard: ChildHazard = 取命關


  /**
   * 甲乙丙丁申子辰，戊己庚主亥卯未，辛申壬癸寅午戌，生還切慮不成人。日主旺則多病疾，主弱則難養成。(星平會海)
   * 甲乙丙丁申子辰，戊己庚生亥卯未，辛金壬癸寅午戌，生兒祇怕不成人。勿入中元壇內，勿看普渡。(生育禮俗)
   *
   * 凡甲、乙、丙、丁日逢申、子、辰時生，
   * 戊、己、庚日逢亥、卯、未時生，
   * 辛、壬、癸日逢寅、午、戌時生是。莫至供奉七爺、八爺廟、有應公、萬善祠、十八王公廟，並須遠離喪葬場合、廟宇建醮、法會、中元普渡場合皆忌。宜用本命錢制化。(黃曆解秘)
   */
  private val p取命關 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(星平會海, 生育禮俗, 黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(甲, 乙, 丙, 丁).contains(eightWords.day.stem) && 申子辰.contains(eightWords.hour.branch)) ||
          (setOf(戊, 己, 庚).contains(eightWords.day.stem) && 亥卯未.contains(eightWords.hour.branch)) ||
          (setOf(辛, 壬, 癸).contains(eightWords.day.stem) && 寅午戌.contains(eightWords.hour.branch))
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p取命關)
}

val h斷腸關 = object : IHazardFactory {

  override val hazard: ChildHazard = 斷腸關

  /**
   * ? 甲乙生逢午未時，丙寅辰巳報君知，庚子生孩元申虎，壬癸逢牛不負犁。(生育禮俗)
   * 甲乙怕見午未時，丙丁生忌辰巳逢，庚辛秋金怕寅虎，壬癸冬水怕逢牛。勿看殺豬羊，勿入屠宰場。(生育禮俗)
   */
  private val p斷腸關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(甲, 乙).contains(eightWords.day.stem) && setOf(午, 未).contains(eightWords.hour.branch)) ||
          (setOf(丙, 丁).contains(eightWords.day.stem) && setOf(辰, 巳).contains(eightWords.hour.branch)) ||
          (setOf(庚, 辛).contains(eightWords.day.stem) && 寅 == eightWords.hour.branch) ||
          (setOf(壬, 癸).contains(eightWords.day.stem) && 丑 == eightWords.hour.branch)
        )
    }
  }

  /**
   * 凡甲乙日生午未時，丙丁日生辰巳時，戊日生於午時，己日生於未時，庚辛日生於寅時，壬癸日生於丑時是。勿看殺豬羊，勿入屠宰場。小孩應避免看殺雞、殺鴨等殺生行為。宜用牛馬將軍錢制化。(黃曆解秘)
   */
  private val p斷腸關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(甲, 乙).contains(eightWords.day.stem) && setOf(午, 未).contains(eightWords.hour.branch)) ||
          (setOf(丙, 丁).contains(eightWords.day.stem) && setOf(辰, 巳).contains(eightWords.hour.branch)) ||
          (戊 == eightWords.day.stem && 午 == eightWords.hour.branch) ||
          (己 == eightWords.day.stem && 未 == eightWords.hour.branch) ||
          (setOf(庚, 辛).contains(eightWords.day.stem) && 寅 == eightWords.hour.branch) ||
          (setOf(壬, 癸).contains(eightWords.day.stem) && 丑 == eightWords.hour.branch)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p斷腸關A, p斷腸關B)
}

val h埋兒關 = object : IHazardFactory {

  override val hazard: ChildHazard = 埋兒關

  /**
   * 子午卯酉逢野牛，寅申巳亥山猿群，辰戌丑未兔驚鬧，此關埋兒驚凶喪。（生育禮俗）
   *
   * 依照民間術士的說法，凡子、午、卯、酉年逢丑時生人，寅、申、巳、亥年逢申時生人，辰、戌、丑、未年逢卯時生人。（黃曆解秘）
   * 。此關埋兒驚凶喪，自幼難養。犯此關主勿看出殯喪葬、出山凶喪保平安。宜重拜父母或拜神明為契子保平安。
   */
  private val p埋兒關 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(生育禮俗, 黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (子午卯酉.contains(eightWords.year.branch) && 丑 == eightWords.hour.branch) ||
          (寅申巳亥.contains(eightWords.year.branch) && 申 == eightWords.hour.branch) ||
          (辰戌丑未.contains(eightWords.year.branch) && 卯 == eightWords.hour.branch)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p埋兒關)
}

val h天吊關 = object : IHazardFactory {

  override val hazard: ChildHazard = 天吊關

  /**
   * 日支 -> 時辰
   * 申子辰生巳午時，寅午戌生龍馬侵，亥卯未生申午起，巳酉丑生卯子真。逆病庶生之子。(星平會海)
   * 申子辰怕巳午時，寅午戌怕辰午時，亥卯未怕申午時，巳酉丑怕卯子時。重拜父母或過房，若私生子無妨。凡寅午戌年辰時生人，犯此主煩惱不寧、眼睛直朢。(生育禮俗)
   */
  private val p天吊關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(星平會海, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (申子辰.contains(eightWords.day.branch) && setOf(巳, 午).contains(eightWords.hour.branch)) ||
        (寅午戌.contains(eightWords.day.branch) && setOf(辰, 午).contains(eightWords.hour.branch)) ||
        (亥卯未.contains(eightWords.day.branch) && setOf(卯, 子).contains(eightWords.hour.branch))
    }

  }

  /**
   * 凡寅、午、戌年辰時生人，犯此主煩惱不寧、眼睛直望。另：
   * 申子辰年巳、午時生，
   * 巳酉丑年子、卯時生，
   * 寅午戌年辰、午時生，
   * 亥卯未年午、申時生是。從小兩眼獃滯、眼珠翻白。(黃曆解秘)
   */
  private val p天吊關B = object : IHazard {

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (申子辰.contains(eightWords.year.branch) && setOf(巳, 午).contains(eightWords.hour.branch)) ||
          (巳酉丑.contains(eightWords.year.branch) && setOf(子, 卯).contains(eightWords.hour.branch)) ||
          (寅午戌.contains(eightWords.year.branch) && setOf(辰, 午).contains(eightWords.hour.branch)) ||
          (亥卯未.contains(eightWords.year.branch) && setOf(午, 申).contains(eightWords.hour.branch))
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p天吊關A, p天吊關B)
}

val h和尚關 = object : IHazardFactory {

  override val hazard: ChildHazard = 和尚關

  /**
   * 子午卯酉忌辰戌丑未，辰戌丑未忌子午卯酉，寅申巳亥忌寅申巳亥，論坐命推便為關。(星平會海)
   * 子午卯酉怕辰戌丑未，辰戌丑未怕子午卯酉，寅申巳亥怕寅申巳亥，便是休庵和尚關。勿隨母入宮廟寺觀燒香拜佛即可解。凡子午卯酉年辰戌丑未時生人，犯此忌入庵寺見僧尼。(生育禮俗)
   * 凡子午卯酉年，辰戌丑未時生人，辰戌丑未年，子午卯酉時生人，寅申巳亥年，寅申巳亥時生人是。犯此忌入庵寺見僧尼。制化法：燒七星錢、請七星娘娘來保護、燒床母經及三十六婆姐錢則吉。(黃曆解秘)
   */
  private val p和尚關 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(星平會海, 生育禮俗, 黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (子午卯酉.contains(eightWords.year.branch) && 辰戌丑未.contains(eightWords.hour.branch)) ||
          (辰戌丑未.contains(eightWords.year.branch) && 子午卯酉.contains(eightWords.hour.branch)) ||
          (寅申巳亥.contains(eightWords.year.branch) && 寅申巳亥.contains(eightWords.hour.branch))
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p和尚關)
}

val h撞命關 = object : IHazardFactory {

  override val hazard: ChildHazard = 撞命關

  /**
   * 日 -> 時
   * 子蛇丑羊寅巳宮, 卯鼠午未俱怕牛, 辰巳申生同忌馬, 酉亥怕豬戌忌羊。犯此關主難養夭壽, 生後宜過房圖解制化。(生育禮俗)
   */
  private val p撞命關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return setOf(
        Pair(子, 巳),
        Pair(丑, 未),
        Pair(寅, 巳),
        Pair(卯, 子),
        Pair(辰, 午),
        Pair(巳, 午),
        Pair(午, 丑),
        Pair(未, 丑),
        Pair(申, 午),
        Pair(酉, 亥),
        Pair(戌, 未),
        Pair(亥, 亥),
      ).contains(eightWords.day.branch to eightWords.hour.branch)
    }
  }

  /**
   * 子年生於丑時，
   * 丑年生於未時，
   * 寅年生於子時，
   * 卯年生於巳時，
   * 辰年生於辰時，
   * 巳年生於午時，
   * 午年生於午時，
   * 未年生於丑時，
   * 申年生於午時，
   * 酉年生於酉時，
   * 戌年生於未時，
   * 亥年生於亥時的人，
   * 天生就犯了此關，從小難養，體弱多病，易有夭折現象，犯此關主宜過房拜認乾爹乾媽可保平安。（黃曆解秘）
   */
  private val p撞命關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return setOf(
        Pair(子, 丑),
        Pair(丑, 未),
        Pair(寅, 子),
        Pair(卯, 巳),
        Pair(辰, 辰),
        Pair(巳, 午),
        Pair(午, 午),
        Pair(未, 丑),
        Pair(申, 午),
        Pair(酉, 酉),
        Pair(戌, 未),
        Pair(亥, 亥),
      ).contains(eightWords.year.branch to eightWords.hour.branch)
    }
  }

  override val impls: Set<IHazard> = setOf(p撞命關A, p撞命關B)
}

val h下情關 = object : IHazardFactory {

  override val hazard: ChildHazard = 下情關

  /**
   * 春生寅酉子，夏生戌亥巳，秋生申丑亡，冬生子午推。(星平會海)
   */
  private val p下情關A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(星平會海)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && setOf(寅, 酉, 子).contains(eightWords.hour.branch)) ||
          (巳午未.contains(eightWords.month.branch) && setOf(戌, 亥, 巳).contains(eightWords.hour.branch)) ||
          (申酉戌.contains(eightWords.month.branch) && setOf(申, 丑).contains(eightWords.hour.branch)) ||
          (亥子丑.contains(eightWords.month.branch) && setOf(子, 午).contains(eightWords.hour.branch))
        )
    }
  }

  /**
   * 凡正二三月子寅時出生之人命犯此關煞，忌聞刀斧之聲。(生育禮俗)
   */
  private val p下情關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return 寅卯辰.contains(eightWords.month.branch) && setOf(子, 寅).contains(eightWords.hour.branch)
    }
  }

  override val impls: Set<IHazard> = setOf(p下情關A, p下情關B)
}

val h劫煞關 = object : IHazardFactory {

  override val hazard: ChildHazard = 劫煞關

  /**
   * 申子辰年生於四月，巳酉丑年生於正月，寅午戌年生於十月，亥卯未年生於七月是。小心提防錢財損失。(黃曆解秘)
   */
  private val p劫煞關 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (申子辰.contains(eightWords.year.branch) && 巳 == eightWords.month.branch) ||
          (巳酉丑.contains(eightWords.year.branch) && 寅 == eightWords.month.branch) ||
          (寅午戌.contains(eightWords.year.branch) && 亥 == eightWords.month.branch) ||
          (亥卯未.contains(eightWords.year.branch) && 申 == eightWords.month.branch)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p劫煞關)
}

val h血刃關 = object : IHazardFactory {

  override val hazard: ChildHazard = 血刃關

  /**
   * 凡正月生於丑時，二月生於未時，三月生於寅時，四月生於申時，五月生於卯時，六月生於酉時，七月生於辰時，八月生於戌時，九月生於巳時，十月生於亥時，十一月生於午時，十二月生於子時是。易有意外血光之災，或車禍及撞傷，用本命錢或車厄錢制化。(黃曆解秘)
   */
  private val p血刃關 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return setOf(
        Pair(寅, 丑),
        Pair(卯, 未),
        Pair(辰, 寅),
        Pair(巳, 申),
        Pair(午, 卯),
        Pair(未, 酉),
        Pair(申, 辰),
        Pair(酉, 戌),
        Pair(戌, 巳),
        Pair(亥, 亥),
        Pair(子, 午),
        Pair(丑, 子),
      ).contains(eightWords.month.branch to eightWords.hour.branch)
    }
  }

  override val impls: Set<IHazard> = setOf(p血刃關)
}

val h基敗關 = object : IHazardFactory {

  override val hazard: ChildHazard = 基敗關

  /**
   * 凡
   * 正、二、三月生於未、戌、亥時，
   * 四、五、六月生於子、辰、巳時，
   * 七、八、九月生於丑、申、酉時，
   * 十、十一、十二月生於寅、卯、午時是。
   * 出生後身體狀況差、容易營養不良，忌看蓋房子、打地基，宜拜神明為契子、或注意腸胃方面的治療便能轉危為安。(黃曆解秘)
   */
  private val p基敗關 = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && setOf(未, 戌, 亥).contains(eightWords.hour.branch)) ||
          (巳午未.contains(eightWords.month.branch) && setOf(子, 辰, 巳).contains(eightWords.hour.branch)) ||
          (申酉戌.contains(eightWords.month.branch) && setOf(丑, 申, 酉).contains(eightWords.hour.branch)) ||
          (亥子丑.contains(eightWords.month.branch) && setOf(寅, 卯, 午).contains(eightWords.hour.branch))
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p基敗關)
}

val h將軍箭 = object : IHazardFactory {

  override val hazard: ChildHazard = 將軍箭


  /**
   * 酉戌辰時春不旺，未卯子時夏中亡，丑寅午時秋並忌，冬季亥申巳為殃，一箭傷人三歲死，二箭傷人六歲亡，三箭傷人九歲亡，四箭傷人十二亡。此箭有弓則凶何也？十二支相衝是也。假如酉戌辰時春生帶箭，八字有相衝弓箭全凶，無沖則不忌。(象吉)
   * 酉戌辰時春不旺，未卯子時夏中亡，子寅丑時秋並忌，冬季亥申巳為殃，一箭傷人三歲死，二箭傷人六歲亡，三箭傷人七歲夭，四箭傷之十二傷。此箭有弓則凶何也？十二支相衝是。如酉戌辰時春生帶箭，八字有相衝遇箭全不吉，無沖則不忌矣。(鰲頭)
   * 酉戌辰時春不旺，未卯子時夏中亡，午寅丑時秋並忌，冬季亥申巳為殃，一箭傷人三歲死，二箭傷人六歲亡。(星平會海)
   * 酉戌辰時春不旺，未卯子時夏中亡，午寅丑時秋並忌，冬季亥申巳為殃，一箭管三歲、柱中遇沖災禍臨至、勿入將軍廟。凡辰酉戌年未時生人，犯此忌見弓箭，二歲亦忌流年中箭。(生育禮俗)
   */
  private val p將軍箭A = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(象吉通書, 鰲頭通書, 星平會海, 生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (寅卯辰.contains(eightWords.month.branch) && setOf(酉, 戌, 辰).contains(eightWords.hour.branch)) ||
          (巳午未.contains(eightWords.month.branch) && setOf(未, 卯, 子).contains(eightWords.hour.branch)) ||
          (申酉戌.contains(eightWords.month.branch) && setOf(丑, 寅, 午).contains(eightWords.hour.branch)) ||
          (亥子丑.contains(eightWords.month.branch) && setOf(亥, 申, 巳).contains(eightWords.hour.branch))
        )
    }

  }

  /**
   * 凡辰酉戌年，未時生人，犯此忌見弓箭，二歲忌流年中箭。民間祭刀箭方法，備將軍錢及草人當替身，將孩子生辰八字寫在草人上，再用竹箭象徵性射過草人後，同將軍一起制化即吉。或至銀樓打一把黃金小剪刀隨身攜帶亦吉。(黃曆解秘)
   */
  private val p將軍箭B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return setOf(辰, 酉, 戌).contains(eightWords.year.branch) && 未 == eightWords.hour.branch
    }
  }

  override val impls: Set<IHazard> = setOf(p將軍箭A, p將軍箭B)
}

val h雷公關 = object : IHazardFactory {

  override val hazard: ChildHazard = 雷公關

  /**
   * 甲牛乙馬丙丁鼠，戊己原來在犬，庚辛逢虎鬚防避，壬雞癸豬有憂煌。限遇此關流年天厄、卒暴、陽刃、火值之，主雷火之厄，若遇天月二德可解。(象吉)
   * 甲牛乙馬丙丁鼠，戊己原來在戌，庚辛逢虎鬚防避，壬雞癸豬有夢場。此關遇流年天厄、卒暴、明月星值之，主雷火之厄，若遇天月三德可以解之。(鰲頭)
   * 甲午乙馬丙丁鼠，戊己原來在戌，庚辛生孩原怕虎，壬雞癸亥有憂惶。(星平會海)
   */
  private val p雷公關A = object : IHazard {

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return setOf(
        Pair(甲, 丑),
        Pair(乙, 午),
        Pair(丙, 子),
        Pair(丁, 子),
        Pair(戊, 戌),
        Pair(己, 戌),
        Pair(庚, 寅),
        Pair(辛, 寅),
        Pair(壬, 酉),
        Pair(癸, 亥),
      ).contains(eightWords.day.stem to eightWords.hour.branch)
    }
  }

  /**
   * 甲己逢馬、丙丁忌鼠、戊乙生犬、庚辛遇虎、壬癸雞鼠、災見閻王。勿抱高空，額處弄跳，注意蹊倒。凡寅午申酉辰亥時生人，犯此忌驚聞鼓雷公及大聲叫喊。(生育禮俗)
   */
  private val p雷公關B = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(生育禮俗)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(甲, 己).contains(eightWords.day.stem) && 午 == eightWords.hour.branch) ||
          (setOf(丙, 丁).contains(eightWords.day.stem) && 子 == eightWords.hour.branch) ||
          (setOf(乙, 戊).contains(eightWords.day.stem) && 戌 == eightWords.hour.branch) ||
          (setOf(庚, 辛).contains(eightWords.day.stem) && 寅 == eightWords.hour.branch) ||
          (setOf(壬, 癸).contains(eightWords.day.stem) && setOf(酉, 子).contains(eightWords.hour.branch))
        )
    }
  }

  /**
   * 凡甲己日逢午時，乙戊日逢戌時，丙丁壬癸日逢子時，庚辛日逢寅時生人是。犯此忌驚聞鑼鼓、雷公及大聲叫喊。(黃曆解秘)
   */
  private val p雷公關C = object : IHazard {

    override fun getBooks(): Set<Book> {
      return setOf(黃曆解秘)
    }

    override fun test(eightWords: IEightWords, gender: Gender?): Boolean {
      return (
        (setOf(甲, 己).contains(eightWords.day.stem) && 午 == eightWords.hour.branch) ||
          (setOf(乙, 戊).contains(eightWords.day.stem) && 戌 == eightWords.hour.branch) ||
          (setOf(丙, 丁, 壬, 癸).contains(eightWords.day.stem) && 子 == eightWords.hour.branch) ||
          (setOf(庚, 辛).contains(eightWords.day.stem) && 寅 == eightWords.hour.branch)
        )
    }
  }

  override val impls: Set<IHazard> = setOf(p雷公關A, p雷公關B, p雷公關C)
}
