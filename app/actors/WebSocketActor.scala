package actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import play.api.libs.json.{JsValue, Json}
import play.api.Logger



class WebSocketActor(uid: String, out: ActorRef) extends Actor with ActorLogging {

  /** The user actor subscribes at the JobActor on Startup */
  override def preStart() = {

    // Attach this Websocket to the corresponding user Actor
    UserManager() ! TellUser(uid, AttachWS(self))
  }


  def receive = LoggingReceive {

    /*
    Messages received from the websocket and passed to the User
     */
    case js : JsValue =>

      Logger.info("???????????????????????????????????")



    /* Messages received from the UserActor and passed to the WebSocket
      */
    case UserJobStateChanged(job, jobID)  =>

      out ! Json.obj("type" ->  "jobstate", "newState" -> job.state.no, "jobid" -> jobID)
  }
}

object WebSocketActor {

  def props(uid: String)(out: ActorRef) = Props(new WebSocketActor(uid, out))
}
