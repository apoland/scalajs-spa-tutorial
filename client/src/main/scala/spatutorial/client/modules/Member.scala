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

object Member {

  case class Props(proxy: ModelProxy[Pot[Todos]])

  case class State(selectedItem: Option[MemberItem] = None, showTodoForm: Boolean = false)

  class Backend($: BackendScope[Props, State]) {
    def mounted(props: Props) =
      // dispatch a message to refresh the todos, which will cause TodoStore to fetch todos from the server
      Callback.ifTrue(props.proxy().isEmpty, props.proxy.dispatch(RefreshTodos))

    def editTodo(item: Option[MemberItem]) =
      // activate the edit dialog
      $.modState(s => s.copy(selectedItem = item, showTodoForm = true))

    def todoEdited(item: MemberItem, cancelled: Boolean) = {
      val cb = if (cancelled) {
        // nothing to do here
        Callback.log("Todo editing cancelled")
      } else {
        Callback.log(s"Todo edited: $item") >>
          $.props >>= (_.proxy.dispatch(UpdateTodo(item)))
      }
      // hide the edit dialog, chain callbacks
      cb >> $.modState(s => s.copy(showTodoForm = false))
    }

    def render(p: Props, s: State) =
      Panel(Panel.Props("Red Card Regiment Members"), <.div(
        p.proxy().renderFailed(ex => "Error loading"),
        p.proxy().renderPending(_ > 500, _ => "Loading..."),
        p.proxy().render(todos => MemberList(todos.items, item => p.proxy.dispatch(UpdateTodo(item)),
          item => editTodo(Some(item)), item => p.proxy.dispatch(DeleteTodo(item)))),
        Button(Button.Props(editTodo(None)), Icon.plusSquare, " New")),
        // if the dialog is open, add it to the panel
        if (s.showTodoForm) MemberForm(MemberForm.Props(s.selectedItem, todoEdited))
        else // otherwise add an empty placeholder
          Seq.empty[ReactElement])
  }

  // create the React component for To Do management
  val component = ReactComponentB[Props]("TODO")
    .initialState(State()) // initial state from TodoStore
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build

  /** Returns a function compatible with router location system while using our own props */
  def apply(proxy: ModelProxy[Pot[Todos]]) = component(Props(proxy))
}

object MemberForm {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(item: Option[MemberItem], submitHandler: (MemberItem, Boolean) => Callback)

  case class State(item: MemberItem, cancelled: Boolean = true)

  class Backend(t: BackendScope[Props, State]) {
    def submitForm(): Callback = {
      // mark it as NOT cancelled (which is the default)
      t.modState(s => s.copy(cancelled = false))
    }

    def formClosed(state: State, props: Props): Callback =
      // call parent handler with the new item and whether form was OK or cancelled
      props.submitHandler(state.item, state.cancelled)

    def updateName(e: ReactEventI) = {
      val text = e.target.value
      // update TodoItem content
      t.modState(s => s.copy(item = s.item.copy(name = text)))
    }

    def updatePhone(e: ReactEventI) = {
      val text = e.target.value
      // update TodoItem content
      t.modState(s => s.copy(item = s.item.copy(phone = text)))
    }

    def render(p: Props, s: State) = {
      log.debug(s"User is ${if (s.item.id == "") "adding" else "editing"} a member")
      val headerText = if (s.item.id == "") "Add new Member" else "Edit Member"
      Modal(Modal.Props(
        // header contains a cancel button (X)
        header = hide => <.span(<.button(^.tpe := "button", bss.close, ^.onClick --> hide, Icon.close), <.h4(headerText)),
        // footer has the OK button that submits the form before hiding it
        footer = hide => <.span(Button(Button.Props(submitForm() >> hide), "OK")),
        // this is called after the modal has been hidden (animation is completed)
        closed = formClosed(s, p)),
        <.div(bss.formGroup,
          <.label(^.`for` := "name", "Name"),
          <.input.text(bss.formControl, ^.id := "description", ^.value := s.item.name,
            ^.placeholder := "Name", ^.onChange ==> updateName)),
        <.div(bss.formGroup,
          <.label(^.`for` := "phone", "Phone"),
          <.input.text(bss.formControl, ^.id := "phone", ^.value := s.item.phone,
            ^.placeholder := "Phone", ^.onChange ==> updatePhone))


      )
    }
  }

  val component = ReactComponentB[Props]("MemberForm")
    .initialState_P(p => State(p.item.getOrElse(MemberItem("", 0, "", "", false))))
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)
}