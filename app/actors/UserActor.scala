package actors

import java.io.File
import javax.inject._

import actors.Worker.{WStart, WPrepare}
import akka.actor._
import akka.event.LoggingReceive
import models.jobs._
import play.api.Logger
import com.google.inject.assistedinject.Assisted
import play.api.Play.current
import reflect.io._


/**
  *  The User actor will represent each user who is present on the toolkit and
  *  encompass its possible interactions with the server.
  *
  * Created by lukas on 1/13/16.
  *
  */

object UserActor {

  case class JobStateChanged(jobid : String, state : JobState)
  case class JobDone(job: Job)

  case class PrepWD(toolname : String, params : Map[String, Any], startImmediately : Boolean, jobid : Option[String])
  case class PrepWDDone(job : Job)

  case object JobIDInvalid

  case class GetJob(jobID : String)

  case class GetAllJobs()

  case class AttachWS(uid : String, ws : ActorRef)

  trait Factory {

    def apply(uid: String): Actor
  }
}


class UserActor @Inject() (@Named("worker") worker : ActorRef,
                           @Assisted uid: String)
                  extends Actor with ActorLogging {

  import UserActor._

  // The websocket that is attached to the User
  var ws: ActorRef = null

  // The UserActor knows all Jobs that belong to him
  val userJobs = new collection.mutable.HashMap[String, Job]()

  var jobIDCounter : Long = 0
  val sep = File.separator
  val path = s"${current.configuration.getString("job_path").get}$sep$uid$sep"


  /**
    * Executed when the UserActor fires up
    */
  // TODO Only for prototype
  override def preStart() = {

    val d = Directory(path)

    // Delete the Job Directory
    val deleteSuccess = d.deleteRecursively()
    Logger.info("Try to delete user folder: State " + deleteSuccess)

    d.createDirectory(force = false, failIfExists = false)

    // Flush User Jobs
    userJobs.clear()
  }


  def receive = LoggingReceive {

    /*
      *   General Messages
      */

    //
    case AttachWS(_, ws_new) =>

      ws = ws_new
      context watch ws
      Logger.info("WebSocket attached successfully\n")


     /* Prepare Routine */
    case PrepWD(toolname, params, startImmediately, jobID) =>

      // Determine the Job ID for the Job that was submitted
      val jobid = jobID match {

        case Some(id) => id

        case None =>

          while(userJobs.keySet contains jobIDCounter.toString) {

            jobIDCounter += 1
          }
          jobIDCounter.toString
      }
      Logger.info("UserActor wants to prepare job directory for tool " + toolname + " with id " + jobid)

      if(userJobs.keySet contains jobid) {

        self ! JobIDInvalid
      }
      else {

        // Everything seems to be fine, send Job to workers
        val sjob = Job(toolname, Running, jobid, uid).attachParams(params)
        userJobs.put(sjob.id, sjob)

        self ! JobStateChanged(jobid, Submitted)
        worker ! WPrepare(sjob)
      }

    /*  Job Dir has been prepared successfully    */
    case PrepWDDone(job) =>

      Logger.info("[UserActor] Job with ID " + job.id + " was prepared successfully")
      self ! JobStateChanged(job.id, Prepared)
      worker ! WStart(job)


      // Notifies the user about a Job Status change
    case m @ JobStateChanged(jobid, state) =>

      Logger.info("[UserActor]"  + uid + " received Job state change: " + state)

      // Update the Job state in the Job Table
      val job = userJobs.get(jobid).get
      job.state = state
      userJobs.put(jobid, job)

      // Forward to WebSocket
      ws ! m

    case GetJob(jobid) =>

      sender() ! userJobs.get(jobid).get

    case GetAllJobs() =>

      sender() ! userJobs.values

    case m @ JobIDInvalid =>

      ws ! m


    /*
     * Job Manager was told that the Job has been finished executing
     */
    case JobDone(job) =>

      Logger.info("[UserActor] Job Execution was done for job " + job.id)
      self ! JobStateChanged(job.id, job.state)







    case Terminated(ws_new) =>

      ws = null
  }
}




/*

val myActor = system.actorOf(Props[MyActor].withDispatcher("my-dispatcher"), name = "myactor2")

import akka.actor.{ Actor, Props, Terminated }

class WatchActor extends Actor {
val child = context.actorOf(Props.empty, "child")
context.watch(child) // <-- this is the only call needed for registration
var lastSender = system.deadLetters

def receive = {
  case "kill" =>
    context.stop(child); lastSender = sender()
  case Terminated(`child`) => lastSender ! "finished"
}
}
 */

























