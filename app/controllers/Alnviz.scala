package controllers

import javax.inject.Inject

import actors.{JobManager, PrepWD, SubscribeUser, UserManager}
import models.{Session, Alnviz}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.Logger


/**
  *
  * Created by lukas on 1/16/16.
  */
class Alnviz @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val UID = "uid"
  val toolname = "alnviz"


  // Input Form Definition of this tool
  val inputForm = Form(
    mapping(
      "alignment" -> text,
      "format" -> text
    )(Alnviz.apply)(Alnviz.unapply)
  )

  def ccToMap(cc: AnyRef) =

    (Map[String, Any]() /: cc.getClass.getDeclaredFields) {
      (a, f) =>
        f.setAccessible(true)
        a + (f.getName -> f.get(cc))
    }


  def show = Action { implicit request =>


    Ok(views.html.alnviz.form(inputForm)).withSession {

      val uid = request.session.get(UID).getOrElse {

        val res = Session.next.toString
        UserManager() ! SubscribeUser(res)
        res
      }
      request.session + (UID -> uid)
    }
  }

  def submit = Action { implicit request =>

      inputForm.bindFromRequest.fold(

        formWithErrors => {
          BadRequest("this was an error")
        },
        formdata => {

          Logger.info("Alnviz received formdata" + formdata + "from uid " + request.session.get(UID).get + "\n")

          // TODO Do we really need to cast formdata into a map?
          JobManager() ! PrepWD(ccToMap(formdata), toolname, request.session.get(UID).get)
          Ok
        })
    }
}
