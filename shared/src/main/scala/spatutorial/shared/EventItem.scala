package spatutorial.shared

import boopickle.Default._

sealed trait EventPriority

case object EventLow extends EventPriority

case object EventNormal extends EventPriority

case object EventHigh extends EventPriority

case class EventItem(id: String, timeStamp: Int, content: String, priority: EventPriority, completed: Boolean)

object EventPriority {
  implicit val EventPriorityPickler: Pickler[EventPriority] = generatePickler[EventPriority]
}
