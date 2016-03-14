package spatutorial.client.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import spatutorial.client.components.Bootstrap.{CommonStyle, Button}
import spatutorial.shared._
import scalacss.ScalaCssReact._

object MemberList {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class TodoListProps(
                            items: Seq[MemberItem],
                            stateChange: MemberItem => Callback,
                            editItem: MemberItem => Callback,
                            deleteItem: MemberItem => Callback
  )

  private val MemberList = ReactComponentB[TodoListProps]("MemberList")
    .render_P(proxy => {
      val style = bss.listGroup
      def renderItem(item: MemberItem) = {
        // convert priority into Bootstrap style
        /*val itemStyle = item.priority match {
          case TodoLow => style.itemOpt(CommonStyle.info)
          case TodoNormal => style.item
          case TodoHigh => style.itemOpt(CommonStyle.danger)
        }
        */
        <.li(style.item,
          //<.input.checkbox(^.checked := item.completed, ^.onChange --> proxy.stateChange(item.copy(completed = !item.completed))),
          <.span(" "),
          if (item.completed) <.s(item.name) else <.span(item.name),
          Button(Button.Props(proxy.editItem(item), addStyles = Seq(bss.pullRight, bss.buttonXS)), "Edit"),
          Button(Button.Props(proxy.deleteItem(item), addStyles = Seq(bss.pullRight, bss.buttonXS)), "Delete")
        )
      }
      <.ul(style.listGroup)(proxy.items map renderItem)
    })
    .build

  def apply(items: Seq[MemberItem], stateChange: MemberItem => Callback, editItem: MemberItem => Callback, deleteItem: MemberItem => Callback) =
    MemberList(TodoListProps(items, stateChange, editItem, deleteItem))
}
