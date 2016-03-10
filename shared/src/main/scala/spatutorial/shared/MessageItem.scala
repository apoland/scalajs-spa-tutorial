package spatutorial.shared

import boopickle.Default._

sealed trait MessagePriority

case object MessageLow extends MessagePriority

case object MessageNormal extends MessagePriority

case object MessageHigh extends MessagePriority

case class MessageItem(id: String, timeStamp: Int, content: String, priority: MessagePriority, completed: Boolean)

object MessagePriority {
  implicit val MessagePriorityPickler: Pickler[MessagePriority] = generatePickler[MessagePriority]
}
