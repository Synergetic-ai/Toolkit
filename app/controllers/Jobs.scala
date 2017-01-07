package controllers

import akka.actor.ActorRef
import javax.inject.{Inject, Named, Singleton}

import actors.JobActor.JobStateChanged
import actors.Master.JobMessage
import models.database._
import modules.CommonModule
import org.joda.time.DateTime
import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDateTime, BSONDocument}
import scala.concurrent.ExecutionContext.Implicits.global



/*
TODO
We can introduce auto-coercion of the Job MainID to the BSONObject ID
 */
/**
  * This controller is supposed to handle request coming from the Backend, such as compute
  * nodes from a gridengine.
  *
  */
@Singleton
final class Jobs @Inject()(@Named("master") master: ActorRef,
                           val reactiveMongoApi: ReactiveMongoApi,
                           @NamedCache("jobActorCache") val jobActorCache: CacheApi) extends Controller with CommonModule {

  def jobStatusDone(jobID: String) = Action {

    master ! JobMessage(jobID, JobStateChanged(jobID, Done))
    Ok
  }

  def jobStatusError(jobID: String) = Action {
    master ! JobMessage(jobID, JobStateChanged(jobID, Error))
    Ok
  }

  def jobStatusRunning(jobID: String) = Action {
    master ! JobMessage(jobID, JobStateChanged(jobID, Running))
    Ok
  }

  def jobStatusQueued(jobID: String) = Action {
    master ! JobMessage(jobID, JobStateChanged(jobID, Queued))
    Ok
  }


  def SGEID(jobID: String, sgeID: String) = Action {

    println("SGE ID: " + sgeID + " for job: " + jobID)
    findJob(BSONDocument(Job.JOBID -> jobID)).foreach {

      case Some(job) =>
        modifyJob(BSONDocument(Job.JOBID -> job.jobID),
          BSONDocument("$set" -> BSONDocument(Job.SGEID -> sgeID)))
        Logger.info(jobID + " gets job-ID " + sgeID + " on SGE")
      case None =>
        //userManager ! JobIDUnknown(jobID)
        Logger.info("Unknown ID " + jobID.toString)
    }

    Ok

  }

  def pushMessage(jobID : String, message : String)  = Action {
    //userManager ! RunningJobMessage(reactivemongo.bson.BSONObjectID.parse(jobID).get, message)
    Ok
  }

  def updateDateViewed(jobID : String)  = Action {

    modifyJob(BSONDocument(Job.JOBID -> jobID),
      BSONDocument("$set"   -> BSONDocument(Job.DATEVIEWED -> BSONDateTime(DateTime.now().getMillis))))
    Ok
  }
}