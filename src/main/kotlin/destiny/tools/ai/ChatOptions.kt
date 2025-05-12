/**
 * Created by smallufo on 2025-05-12.
 */
package destiny.tools.ai


@JvmInline
value class Temperature(val value: Double) {
  init {
    require(value in 0.0..1.0) { "Temperature must be in 0.0..1.0" }
  }
}

@JvmInline
value class TopP(val value: Double) {
  init {
    require(value in 0.0..1.0) { "topP must be in 0.0..1.0" }
  }
}

@JvmInline
value class TopK(val value: Int) {
  init {
    require(value >= 1) { "topK must be >= 1" }
  }
}

data class ChatOptions(
  val temperature: Temperature? = null,
  /**
   * 更一致： topP = 0.8 ~ 0.9
   * 更發散 : topP = 0.95 ~ 1.0
   */
  val topP: TopP? = null,
  /**
   * 更一致 : topK = 20 ~ 50
   * 更發散 : topK = 100+
   */
  val topK: TopK? = null,
)
