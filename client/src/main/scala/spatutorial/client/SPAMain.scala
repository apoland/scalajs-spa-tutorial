package spatutorial.client

import japgolly.scalajs.react.ReactDOM
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom
import spatutorial.client.components.GlobalStyles
import spatutorial.client.logger._
import spatutorial.client.modules._
import spatutorial.client.services.{RootModel, SPACircuit}
import spatutorial.client.modules.MainMenu.Counters

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scalacss.Defaults._
import scalacss.ScalaCssReact._

@JSExport("SPAMain")
object SPAMain extends js.JSApp {

  // Define the locations (pages) used in this application
  sealed trait Loc

  case object DashboardLoc extends Loc

  case object TodoLoc extends Loc

  case object EventsLoc extends Loc

  case object MessagesLoc extends Loc

  // configure the router
  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    // wrap/connect components to the circuit
    (staticRoute(root, DashboardLoc) ~> renderR(ctl => SPACircuit.wrap(_.motd)(proxy => Dashboard(ctl, proxy)))
      | staticRoute("#todo", TodoLoc) ~> renderR(ctl => SPACircuit.connect(_.todos)(Todo(_)))
      | staticRoute("#events", EventsLoc) ~> renderR(ctl => SPACircuit.connect(_.messages)(Message(_)))
      | staticRoute("#messages", MessagesLoc) ~> renderR(ctl => SPACircuit.connect(_.messages)(Message(_)))
      ).notFound(redirectToPage(DashboardLoc)(Redirect.Replace))
  }.renderWith(layout)

  // base layout for all pages
  def layout(c: RouterCtl[Loc], r: Resolution[Loc]) = {
    div(
      // here we use plain Bootstrap class names as these are specific to the top level layout defined here
      nav(className := "navbar navbar-inverse navbar-fixed-top",
        div(className := "container",
          div(className := "navbar-header", span(className := "navbar-brand", "Red Card Robot")),
          div(className := "collapse navbar-collapse",
            // connect menu to model, because it needs to update when the number of open todos changes
            SPACircuit.connect(buildCounters(_))(proxy => MainMenu(c, r.page, proxy))
          )
        )
      ),
      // currently active module is shown in this container
      div(className := "container", r.render())
    )
  }

  def buildCounters(model: RootModel): Counters = {
     Counters(model.todos.map(_.items.count(!_.completed)).toOption, model.messages.map(_.items.count(!_.completed)).toOption)
  }

  @JSExport
  def main(): Unit = {
    log.warn("Application starting")
    // send log messages also to the server
    log.enableServerLogging("/logging")
    log.info("This message goes to server as well")

    // create stylesheet
    GlobalStyles.addToDocument()
    // create the router
    val router = Router(BaseUrl.until_#, routerConfig)
    // tell React to render the router in the document body
    ReactDOM.render(router(), dom.document.getElementById("root"))
  }
}
