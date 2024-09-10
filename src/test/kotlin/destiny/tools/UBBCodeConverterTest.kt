/**
 * Created by smallufo on 2017-03-23.
 */
package destiny.tools

import destiny.tools.KotlinLogging
import kotlin.test.Test
import kotlin.test.assertEquals

class UBBCodeConverterTest {

  private val logger = KotlinLogging.logger { }

  @Test
  fun reQuote() {
    val codeConverter = UBBCodeConverter()

    val s1 = "123 [quote] 內文 [/quote] 456"
    val s2 = "123 \n" + "<br>[quote] 內文 [/quote] \n" + "<br>456"
    val s3 = "123 \n" + "<br>[quote] 內文<br>哈囉 [/quote] \n" + "<br>456"
    val s4 = "123 \n" + "<br>[quote] 內文\n哈囉 [/quote] \n" + "<br>456"


    assertEquals("123 <hr noshade size=1 ><blockquote> 內文 </blockquote><hr noshade size=1> 456",
                 codeConverter.getAll(s1))

    logger.info("s1 : {}", codeConverter.getAll(s1))
    logger.info("s2 : {}", codeConverter.getAll(s2))
    logger.info("s3 : {}", codeConverter.getAll(s3))
    logger.info("s4 : {}", codeConverter.getAll(s4))

  }

  @Test
  fun testOthers() {
    val codeConverter = UBBCodeConverter()
    logger.info("email : {}", codeConverter.getAll("""[email]service@google.com[/email]"""))
    logger.info("italic : {}", codeConverter.getAll("""[i]italic1[/i] [i]斜體2[/i]"""))
  }

}