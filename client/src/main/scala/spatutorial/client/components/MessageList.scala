package spatutorial.client.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import spatutorial.client.components.Bootstrap.{CommonStyle, Button}
import spatutorial.shared._
import scalacss.ScalaCssReact._

object MessageList {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class MessageListProps(
                            items: Seq[MessageItem],
                            stateChange: MessageItem => Callback,
                            editItem: MessageItem => Callback,
                            deleteItem: MessageItem => Callback
                          )

  private val MessageList = ReactComponentB[MessageListProps]("MessageList")
    .render_P(p => {
      val style = bss.listGroup
      def renderItem(item: MessageItem) = {
        // convert priority into Bootstrap style

        <.li(style.item,
           <.span(item.content),
          Button(Button.Props(p.editItem(item), addStyles = Seq(bss.pullRight, bss.buttonXS)), "Edit"),
          Button(Button.Props(p.deleteItem(item), addStyles = Seq(bss.pullRight, bss.buttonXS)), "Delete")
        )
      }
      <.ul(style.listGroup)(p.items map renderItem)
    })
    .build

  def apply(items: Seq[MessageItem], stateChange: MessageItem => Callback, editItem: MessageItem => Callback, deleteItem: MessageItem => Callback) =
    MessageList(MessageListProps(items, stateChange, editItem, deleteItem))
}
