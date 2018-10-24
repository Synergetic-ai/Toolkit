package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.results.HHPredResult
import de.proteinevolution.services.ToolConfig

import scala.collection.immutable.ListMap

case class HHPredAlignResultView(
    jobId: String,
    result: HHPredResult,
    toolConfig: ToolConfig,
    constants: ConstantsV2
) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.HITLIST ->
    views.html.resultpanels.hhpred.hitlist(
      jobId,
      result,
      toolConfig.values(ToolName.HHPRED_ALIGN.value),
      s"${constants.jobPath}/$jobId/results/$jobId.html_NOIMG"
    ),
    "FullAlignment" -> views.html.resultpanels.msaviewer(s"${constants.jobPath}/$jobId/results/alignment.fas")
  )

}
