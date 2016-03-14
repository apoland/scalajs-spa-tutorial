package spatutorial.client.modules

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import spatutorial.client.SPAMain.{DashboardLoc, Loc, TodoLoc, MessagesLoc, EventsLoc}
import spatutorial.client.components.Bootstrap.CommonStyle
import spatutorial.client.components.Icon._
import spatutorial.client.components._
import spatutorial.client.modules.MainMenu.MenuItem
import spatutorial.client.services._

import scalacss.ScalaCssReact._

object MainMenu {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles


  case class Counters(todos: Option[Int], messages: Option[Int], events: Option[Int])

  case class Props(router: RouterCtl[Loc], currentLoc: Loc, proxy: ModelProxy[Counters])

  private case class MenuItem(idx: Int, label: (Props) => ReactNode, icon: Icon, location: Loc)

  // build the Todo menu item, showing the number of open todos
  private def buildMembersMenu(props: Props): ReactElement = {
    val todoCount = props.proxy().todos.getOrElse(0)
    <.span(
      <.span("Members ")
      //todoCount > 0 ?= <.span(bss.labelOpt(CommonStyle.danger), bss.labelAsBadge, todoCount)
    )
  }

  private def buildEventsMenu(props: Props): ReactElement = {
    val eventCount = props.proxy().events.getOrElse(0)
    <.span(
      <.span("Events ")
      //messageCount > 0 ?= <.span(bss.labelOpt(CommonStyle.danger), bss.labelAsBadge, messageCount)
    )
  }

  private def buildMessagesMenu(props: Props): ReactElement = {
    val messageCount = props.proxy().messages.getOrElse(0)
    <.span(
      <.span("Messages ")
      //messageCount > 0 ?= <.span(bss.labelOpt(CommonStyle.danger), bss.labelAsBadge, messageCount)
    )
  }

  private val menuItems = Seq(
    MenuItem(1, _ => "Dashboard", Icon.dashboard, DashboardLoc),
    MenuItem(2, buildMembersMenu, Icon.user, TodoLoc),
    MenuItem(3, buildEventsMenu, Icon.calendarO, EventsLoc),
    MenuItem(4, buildMessagesMenu, Icon.envelopeO, MessagesLoc)
  )

  private class Backend($: BackendScope[Props, Unit]) {
    def mountedTodos(props: Props) = {
      // dispatch a message to refresh the todos
      Callback.ifTrue(props.proxy.value.todos.isEmpty, props.proxy.dispatch(RefreshTodos))
    }

    def mountedMessages(props: Props) = {
      // dispatch a message to refresh the messages
      Callback.ifTrue(props.proxy.value.messages.isEmpty, props.proxy.dispatch(RefreshMessages))
    }

    def mountedEvents(props: Props) = {
      // dispatch a message to refresh the messages
      Callback.ifTrue(props.proxy.value.events.isEmpty, props.proxy.dispatch(RefreshEvents))
    }

    def render(props: Props) = {
      <.ul(bss.navbar)(
        // build a list of menu items
        for (item <- menuItems) yield {
          <.li(^.key := item.idx, (props.currentLoc == item.location) ?= (^.className := "active"),
            props.router.link(item.location)(item.icon, " ", item.label(props))
          )
        }
      )
    }
  }

  private val component = ReactComponentB[Props]("MainMenu")
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mountedTodos(scope.props))
    .componentDidMount(scope => scope.backend.mountedMessages(scope.props))
    .componentDidMount(scope => scope.backend.mountedEvents(scope.props))
    .build

  def apply(ctl: RouterCtl[Loc], currentLoc: Loc, proxy: ModelProxy[Counters]): ReactElement =
    component(Props(ctl, currentLoc, proxy))
}
