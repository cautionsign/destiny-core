/**
 * Created by smallufo on 2022-03-11.
 */
package destiny.core.astrology


enum class HouseType {
  ANGULAR,
  SUCCEDENT,
  CADENT
}

data class House(val index: Int,
                 val cusp: ZodiacDegree,
                 val pointPositions: List<Pair<AstroPoint, IPosWithAzimuth>>) : java.io.Serializable
