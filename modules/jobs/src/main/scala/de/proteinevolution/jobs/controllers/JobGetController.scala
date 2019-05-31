/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.jobs.controllers

import java.time.ZonedDateTime

import cats.implicits._
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.{Job, JobHashError}
import de.proteinevolution.jobs.services.{JobFolderValidation, JobHashCheckService, JobSearchService}
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.tools.ToolConfig
import io.circe.syntax._
import io.circe.{Json, JsonObject}
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext

@Singleton
class JobGetController @Inject()(
                                  jobHashService: JobHashCheckService,
                                  userSessions: UserSessionService,
                                  jobDao: JobDao,
                                  cc: ControllerComponents,
                                  toolConfig: ToolConfig,
                                  constants: ConstantsV2,
                                  jobSearchService: JobSearchService,
                                  userAction: UserAction
)(implicit ec: ExecutionContext, config: Configuration)
    extends ToolkitController(cc)
    with JobFolderValidation {

  def jobManagerListJobs: Action[AnyContent] = userAction.async { implicit request =>
    jobDao
      .findJobs(BSONDocument(Job.OWNERID -> request.user.userID, Job.DELETION -> BSONDocument("$exists" -> false)))
      .map { jobs =>
        NoCache(Ok(jobs.filter(job => jobFolderIsValid(job.jobID, constants)).map(_.cleaned(toolConfig)).asJson))
      }
  }

  def listJobs: Action[AnyContent] = userAction.async { implicit request =>
    jobDao.findJobs(BSONDocument(Job.JOBID -> BSONDocument("$in" -> request.user.jobs))).map { jobs =>
      Ok(jobs.filter(job => jobFolderIsValid(job.jobID, constants)).map(_.cleaned(toolConfig)).asJson)
    }
  }

  /**
   * Returns the last updated job
   */
  def recentJob: Action[AnyContent] = userAction.async { implicit request =>
    jobSearchService.recentJob(request.user).map { lastJob =>
      Ok(lastJob.map(_.cleaned(toolConfig)).asJson)
    }
  }

  /**
   * if no tool is found for a given query,
   * it looks for jobs which belong to the current user.
   * only jobIDs that belong to the user are autocompleted
   */
  def suggestJobsForJobId(queryString_ : String): Action[AnyContent] = userAction.async { implicit request =>
    jobSearchService.autoComplete(request.user, queryString_).value.map {
      case Some(jobs) => Ok(jobs.map(_.cleaned(toolConfig)).asJson)
      case None       => NoContent
    }
  }

  def loadJob(jobID: String): Action[AnyContent] = userAction.async { implicit request =>
    jobDao.selectJob(jobID).map {
      case Some(job) if jobFolderIsValid(job.jobID, constants) => Ok(job.cleaned(toolConfig).asJson)
      case _                                                   => NotFound
    }
  }

  def checkHash(jobID: String): Action[AnyContent] = userAction.async { implicit request =>
    (for {
      job <- jobHashService.checkHash(jobID)
    } yield {
      (job.jobID, job.dateCreated.getOrElse(ZonedDateTime.now).toInstant.toEpochMilli)
    }).value.map {
      case Some((latestJobId, dateCreated)) =>
        Ok(JsonObject("jobID" -> Json.fromString(latestJobId), "dateCreated" -> Json.fromLong(dateCreated)).asJson)
      case None => NotFound(errors(JobHashError.JobNotFound.msg))
    }
  }

}
