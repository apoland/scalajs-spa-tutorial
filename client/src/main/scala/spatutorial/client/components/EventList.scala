package spatutorial.client.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import spatutorial.client.components.Bootstrap.{Button, CommonStyle}
import spatutorial.shared._

import scalacss.ScalaCssReact._

object EventList {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class EventListProps(
                            items: Seq[EventItem],
                            stateChange: EventItem => Callback,
                            editItem: EventItem => Callback,
                            deleteItem: EventItem => Callback
                          )

  private val EventList = ReactComponentB[EventListProps]("EventList")
    .render_P(p => {
      val style = bss.listGroup
      def renderItem(item: EventItem) = {
        // convert priority into Bootstrap style
        val itemStyle = item.priority match {
          case EventLow => style.itemOpt(CommonStyle.info)
          case EventNormal => style.item
          case EventHigh => style.itemOpt(CommonStyle.danger)
        }
        <.li(style.item,
          <.span(" "),
          <.span(item.content),
          Button(Button.Props(p.editItem(item), addStyles = Seq(bss.pullRight, bss.buttonXS)), "Edit"),
          Button(Button.Props(p.deleteItem(item), addStyles = Seq(bss.pullRight, bss.buttonXS)), "Delete")
        )
      }
      <.ul(style.listGroup)(p.items map renderItem)
    })
    .build

  def apply(items: Seq[EventItem], stateChange: EventItem => Callback, editItem: EventItem => Callback, deleteItem: EventItem => Callback) =
    EventList(EventListProps(items, stateChange, editItem, deleteItem))
}
