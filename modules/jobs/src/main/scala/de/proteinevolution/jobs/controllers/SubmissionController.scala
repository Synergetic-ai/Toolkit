package de.proteinevolution.jobs.controllers

import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.actors.JobActor.{CheckIPHash, Delete}
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.services._
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.tools.ToolConfig
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import io.circe.{Decoder, Json, JsonObject}
import io.circe.syntax._

import scala.concurrent.ExecutionContext

@Singleton
class SubmissionController @Inject()(
    jobActorAccess: JobActorAccess,
    userSessions: UserSessions,
    jobDispatcher: JobDispatcher,
    constants: ConstantsV2,
    cc: ControllerComponents,
    jobDao: JobDao,
    toolConfig: ToolConfig,
    jobResubmitService: JobResubmitService,
    jobIdProvider: JobIdProvider,
    jobFrontendToolsService: JobFrontendToolsService
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc) {

  private val logger = Logger(this.getClass)

  def startJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { _ =>
      jobActorAccess.sendToJobActor(jobID, CheckIPHash(jobID))
      Ok(JsonObject("message" -> Json.fromString("Starting Job...")).asJson)
    }
  }

  def frontend(toolName: String): Action[AnyContent] = Action.async { implicit request =>
    if (toolConfig.isTool(toolName)) {
      // Add Frontend Job to Database
      jobFrontendToolsService.logFrontendJob(toolName).map(_ => NoContent)
    } else {
      fuccess(BadRequest)
    }
  }

  def delete(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info("Delete Action in JobController reached")
    userSessions.getUser.map { user =>
      jobActorAccess.sendToJobActor(jobID, Delete(jobID, Some(user.userID)))
      NoContent
    }
  }

  implicit def eitherDecoder[A,B](implicit a: Decoder[A], b: Decoder[B]): Decoder[Either[A,B]] = {
    val l: Decoder[Either[A,B]]= a.map(Left.apply)
    val r: Decoder[Either[A,B]]= b.map(Right.apply)
    l or r
  }

  def submitJob(toolName: String): Action[Map[String, Either[Seq[String], String]]] =
    Action(circe.json[Map[String, Either[Seq[String], String]]]).async { implicit request =>
      val parts = request.body.mapValues {
        case Left(list) => list.mkString(constants.formMultiValueSeparator)
        case Right(str) => str
      }
      userSessions.getUser.flatMap { user =>
        jobDispatcher
          .submitJob(
            toolName,
            parts,
            user
          )
          .value
          .map {
            case Right(job) =>
              Ok(
                JsonObject(
                  "successful" -> Json.fromBoolean(true),
                  "code"       -> Json.fromInt(0),
                  "message"    -> Json.fromString("Submission successful."),
                  "jobID"      -> Json.fromString(job.jobID)
                ).asJson
              ).withSession(userSessions.sessionCookie(request, user.sessionID.get))
            case Left(error) => BadRequest(errors(error.msg))
          }
      }
    }

  def resubmitJob(newJobID: String, resubmitForJobID: Option[String]): Action[AnyContent] = Action.async {
    implicit request =>
      jobResubmitService.resubmit(newJobID, resubmitForJobID).map(r => Ok(r.asJson))
  }

}
