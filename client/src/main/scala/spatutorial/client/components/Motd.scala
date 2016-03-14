package spatutorial.client.components

import diode.react.ReactPot.potWithReact
import diode.react.ModelProxy
import diode.data.Pot
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import spatutorial.client.components.Bootstrap.{Panel, Button, CommonStyle}
import spatutorial.client.services.UpdateMotd

/**
  * This is a simple component demonstrating how to display async data coming from the server
  */
object Motd {

  case class Nothing()

  // create the React component for holding the Message of the Day
  val Motd = ReactComponentB[ModelProxy[Pot[String]]]("Motd")
    .render_P { proxy =>
      Panel(Panel.Props("Message of the day"),
        // render messages depending on the state of the Pot
        proxy().renderPending(_ => <.p("Loading...")),
        proxy().renderFailed(ex => <.p("Failed to load")),
        proxy().render(m => <.p(m))
      //  Button(Button.Props(proxy.dispatch(UpdateMotd()), CommonStyle.danger), Icon.refresh, " Update")
      )
    }
    .componentDidMount(scope =>
      // update only if Motd is empty
      {
        println("Here")
        Callback.ifTrue(scope.props.value.isEmpty, scope.props.dispatch(UpdateMotd()))
      }

    )
    .build


  def apply(proxy: ModelProxy[Pot[String]]) = Motd(proxy)
}
