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

object Message {

  case class Props(proxy: ModelProxy[Pot[Messages]])

  case class State(selectedItem: Option[MessageItem] = None, showMessageForm: Boolean = false)

  class Backend($: BackendScope[Props, State]) {
    def mounted(props: Props) =
    // dispatch a message to refresh the Messages, which will cause Messagestore to fetch Messages from the server
      Callback.ifTrue(props.proxy().isEmpty, props.proxy.dispatch(RefreshMessages))

    def editMessage(item: Option[MessageItem]) =
    // activate the edit dialog
      $.modState(s => s.copy(selectedItem = item, showMessageForm = true))

    def MessageEdited(item: MessageItem, cancelled: Boolean) = {
      val cb = if (cancelled) {
        // nothing to do here
        Callback.log("Message editing cancelled")
      } else {
        Callback.log(s"Message edited: $item") >>
          $.props >>= (_.proxy.dispatch(UpdateMessage(item)))
      }
      // hide the edit dialog, chain callbacks
      cb >> $.modState(s => s.copy(showMessageForm = false))
    }

    def render(p: Props, s: State) =
      Panel(Panel.Props("Message History"), <.div(
        p.proxy().renderFailed(ex => "Error loading"),
        p.proxy().renderPending(_ > 500, _ => "Loading..."),
        p.proxy().render(Messages => MessageList(Messages.items, item => p.proxy.dispatch(UpdateMessage(item)),
          item => editMessage(Some(item)), item => p.proxy.dispatch(DeleteMessage(item)))),
        Button(Button.Props(editMessage(None)), Icon.plusSquare, " New")),
        // if the dialog is open, add it to the panel
        if (s.showMessageForm) MessageForm(MessageForm.Props(s.selectedItem, MessageEdited))
        else // otherwise add an empty placeholder
          Seq.empty[ReactElement])
  }

  // create the React component for To Do management
  val component = ReactComponentB[Props]("Message")
    .initialState(State()) // initial state from Messagestore
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build

  /** Returns a function compatible with router location system while using our own props */
  def apply(proxy: ModelProxy[Pot[Messages]]) = component(Props(proxy))
}

object MessageForm {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(item: Option[MessageItem], submitHandler: (MessageItem, Boolean) => Callback)

  case class State(item: MessageItem, cancelled: Boolean = true)

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
      // update MessageItem content
      t.modState(s => s.copy(item = s.item.copy(content = text)))
    }

    def updatePriority(e: ReactEventI) = {
      // update MessageItem priority
      val newPri = e.currentTarget.value match {
        case p if p == MessageHigh.toString => MessageHigh
        case p if p == MessageNormal.toString => MessageNormal
        case p if p == MessageLow.toString => MessageLow
      }
      t.modState(s => s.copy(item = s.item.copy(priority = newPri)))
    }

    def render(p: Props, s: State) = {
      log.debug(s"User is ${if (s.item.id == "") "adding" else "editing"} a Message or two")
      val headerText = if (s.item.id == "") "Add new Message" else "Edit Message"
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
            <.option(^.value := MessageHigh.toString, "High"),
            <.option(^.value := MessageNormal.toString, "Normal"),
            <.option(^.value := MessageLow.toString, "Low")
          )
        )
      )
    }
  }

  val component = ReactComponentB[Props]("MessageForm")
    .initialState_P(p => State(p.item.getOrElse(MessageItem("", 0, "", MessageNormal, false))))
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)
}