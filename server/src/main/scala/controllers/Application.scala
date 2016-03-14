package controllers

import java.nio.ByteBuffer
import javax.inject.Inject

import _root_.forms.SignInForm
import _root_.forms.SignUpForm
import boopickle.Default._
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.ApiService
import spatutorial.shared.Api
import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry

import models.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Router extends autowire.Server[ByteBuffer, Pickler, Pickler] {
  override def read[R: Pickler](p: ByteBuffer) = Unpickle[R].fromBytes(p)
  override def write[R: Pickler](r: R) = Pickle.intoBytes(r)
}

class ApplicationController @Inject()
 (val messagesApi: MessagesApi,
  val env: Environment[User, CookieAuthenticator],
  socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User, CookieAuthenticator] {

  val apiService = new ApiService()

  /*
  def index = Action {
    Ok(views.html.index("SPA tutorial"))
  }
  */

  def index = Action.async { implicit request =>
    Future.successful(Ok(views.html.index("SPA tutorial")))
  }

  /**
    * Handles the Sign In action.
    *
    * @return The result to display.
    */
  def signIn = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.ApplicationController.index()))
      case None => Future.successful(Ok(views.html.signIn(SignInForm.form, socialProviderRegistry)))
    }
  }

  /**
    * Handles the Sign Up action.
    *
    * @return The result to display.
    */
  def signUp = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.ApplicationController.index()))
      case None => Future.successful(Ok(views.html.signUp(SignUpForm.form)))
    }
  }

  /**
    * Handles the Sign Out action.
    *
    * @return The result to display.
    */
  def signOut = SecuredAction.async { implicit request =>
    val result = Redirect(routes.ApplicationController.index())
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))

    env.authenticatorService.discard(request.authenticator, result)
  }

  def autowireApi(path: String) = Action.async(parse.raw) {
    implicit request =>
      println(s"Request path: $path")
      //println(s"User: ${request.identity.loginInfo}")

      // get the request body as Array[Byte]
      val b = request.body.asBytes(parse.UNLIMITED).get

      // call Autowire route
      Router.route[Api](apiService)(
        autowire.Core.Request(path.split("/"), Unpickle[Map[String, ByteBuffer]].fromBytes(ByteBuffer.wrap(b)))
      ).map(buffer => {
        val data = Array.ofDim[Byte](buffer.remaining())
        buffer.get(data)
        Ok(data)
      })
  }

  def logging = Action(parse.anyContent) {
    implicit request =>
      request.body.asJson.foreach { msg =>
        println(s"CLIENT - $msg")
      }
      Ok("")
  }
}
