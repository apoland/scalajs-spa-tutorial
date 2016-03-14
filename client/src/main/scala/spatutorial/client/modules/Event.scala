package spatutorial.client.modules

import diode.react.ReactPot._
import diode.react._
import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import spatutorial.client.components.Bootstrap._
import spatutorial.client.components._
import spatutorial.client.logger._
import spatutorial.client.services._
import spatutorial.shared._

import scalacss.ScalaCssReact._

object Event {

  case class Props(proxy: ModelProxy[Pot[Events]])

  case class State(selectedItem: Option[EventItem] = None, showEventForm: Boolean = false)

  class Backend($: BackendScope[Props, State]) {
    def mounted(props: Props) =
    // dispatch a message to refresh the Events, which will cause Eventstore to fetch Events from the server
      Callback.ifTrue(props.proxy().isEmpty, props.proxy.dispatch(RefreshEvents))

    def editEvent(item: Option[EventItem]) =
    // activate the edit dialog
      $.modState(s => s.copy(selectedItem = item, showEventForm = true))

    def EventEdited(item: EventItem, cancelled: Boolean) = {
      val cb = if (cancelled) {
        // nothing to do here
        Callback.log("Event editing cancelled")
      } else {
        Callback.log(s"Event edited: $item") >>
          $.props >>= (_.proxy.dispatch(UpdateEvent(item)))
      }
      // hide the edit dialog, chain callbacks
      cb >> $.modState(s => s.copy(showEventForm = false))
    }

    def render(p: Props, s: State) =
      Panel(Panel.Props("Upcoming Events"), <.div(
        p.proxy().renderFailed(ex => "Error loading"),
        p.proxy().renderPending(_ > 500, _ => "Loading..."),
        p.proxy().render(Events => EventList(Events.items, item => p.proxy.dispatch(UpdateEvent(item)),
          item => editEvent(Some(item)), item => p.proxy.dispatch(DeleteEvent(item)))),
        Button(Button.Props(editEvent(None)), Icon.plusSquare, " New")),
        // if the dialog is open, add it to the panel
        if (s.showEventForm) EventForm(EventForm.Props(s.selectedItem, EventEdited))
        else // otherwise add an empty placeholder
          Seq.empty[ReactElement])
  }

  // create the React component for To Do management
  val component = ReactComponentB[Props]("Event")
    .initialState(State()) // initial state from Eventstore
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build

  /** Returns a function compatible with router location system while using our own props */
  def apply(proxy: ModelProxy[Pot[Events]]) = component(Props(proxy))
}

object EventForm {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(item: Option[EventItem], submitHandler: (EventItem, Boolean) => Callback)

  case class State(item: EventItem, cancelled: Boolean = true)

  class Backend(t: BackendScope[Props, State]) {
    def submitForm(): Callback = {
      // mark it as NOT cancelled (which is the default)
      t.modState(s => s.copy(cancelled = false))
    }

    def formClosed(state: State, props: Props): Callback =
    // call parent handler with the new item and whether form was OK or cancelled
      props.submitHandler(state.item, state.cancelled)

    def updateDescription(e: ReactEventI) = {
      val text = e.target.value
      // update EventItem content
      t.modState(s => s.copy(item = s.item.copy(content = text)))
    }

    def updatePriority(e: ReactEventI) = {
      // update EventItem priority
      val newPri = e.currentTarget.value match {
        case p if p == EventHigh.toString => EventHigh
        case p if p == EventNormal.toString => EventNormal
        case p if p == EventLow.toString => EventLow
      }
      t.modState(s => s.copy(item = s.item.copy(priority = newPri)))
    }

    def render(p: Props, s: State) = {
      log.debug(s"User is ${if (s.item.id == "") "adding" else "editing"} a Event or two")
      val headerText = if (s.item.id == "") "Add new Event" else "Edit Event"
      Modal(Modal.Props(
        // header contains a cancel button (X)
        header = hide => <.span(<.button(^.tpe := "button", bss.close, ^.onClick --> hide, Icon.close), <.h4(headerText)),
        // footer has the OK button that submits the form before hiding it
        footer = hide => <.span(Button(Button.Props(submitForm() >> hide), "OK")),
        // this is called after the modal has been hidden (animation is completed)
        closed = formClosed(s, p)),
        <.div(bss.formGroup,
          <.label(^.`for` := "description", "Description"),
          <.input.text(bss.formControl, ^.id := "description", ^.value := s.item.content,
            ^.placeholder := "write description", ^.onChange ==> updateDescription)),
        <.div(bss.formGroup,
          <.label(^.`for` := "priority", "Priority"),
          // using defaultValue = "Normal" instead of option/selected due to React
          <.select(bss.formControl, ^.id := "priority", ^.value := s.item.priority.toString, ^.onChange ==> updatePriority,
            <.option(^.value := EventHigh.toString, "High"),
            <.option(^.value := EventNormal.toString, "Normal"),
            <.option(^.value := EventLow.toString, "Low")
          )
        )
      )
    }
  }

  val component = ReactComponentB[Props]("EventForm")
    .initialState_P(p => State(p.item.getOrElse(EventItem("", 0, "", EventNormal, false))))
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)
}