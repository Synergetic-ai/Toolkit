package de.proteinevolution.results.results

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class HHBlitsInfo(
    aligned_cols: Int,
    eval: Double,
    identities: Double,
    probab: Double,
    score: Double,
    similarity: Double
) extends SearchToolInfo

object HHBlitsInfo {

  implicit val hhblitsInfoDecoder: Decoder[HHBlitsInfo] = deriveDecoder[HHBlitsInfo]

}