package spatutorial.client.services

import autowire._
import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector
import spatutorial.shared.{EventItem, MemberItem, Api, MessageItem}
import boopickle.Default._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

// Actions
case object RefreshTodos

case class UpdateAllTodos(todos: Seq[MemberItem])

case class UpdateTodo(item: MemberItem)

case class DeleteTodo(item: MemberItem)

case class UpdateMotd(potResult: Pot[String] = Empty) extends PotAction[String, UpdateMotd] {
  override def next(value: Pot[String]) = UpdateMotd(value)
}

case object RefreshMessages

case class UpdateAllMessages(messages: Seq[MessageItem])

case class UpdateMessage(item: MessageItem)

case class DeleteMessage(item: MessageItem)


case object RefreshEvents

case class UpdateAllEvents(messages: Seq[EventItem])

case class UpdateEvent(item: EventItem)

case class DeleteEvent(item: EventItem)




// The base model of our application
case class RootModel(todos: Pot[Todos], motd: Pot[String], messages: Pot[Messages], events: Pot[Events])

case class Todos(items: Seq[MemberItem]) {
  def updated(newItem: MemberItem) = {
    items.indexWhere(_.id == newItem.id) match {
      case -1 =>
        // add new
        Todos(items :+ newItem)
      case idx =>
        // replace old
        Todos(items.updated(idx, newItem))
    }
  }
  def remove(item: MemberItem) = Todos(items.filterNot(_ == item))
}

/**
  * Handles actions related to todos
  *
  * @param modelRW Reader/Writer to access the model
  */
class TodoHandler[M](modelRW: ModelRW[M, Pot[Todos]]) extends ActionHandler(modelRW) {
  override def handle = {
    case RefreshTodos =>
      effectOnly(Effect(AjaxClient[Api].getTodos().call().map(UpdateAllTodos)))
    case UpdateAllTodos(todos) =>
      // got new todos, update model
      updated(Ready(Todos(todos)))
    case UpdateTodo(item) =>
      // make a local update and inform server
      updated(value.map(_.updated(item)), Effect(AjaxClient[Api].updateTodo(item).call().map(UpdateAllTodos)))
    case DeleteTodo(item) =>
      // make a local update and inform server
      updated(value.map(_.remove(item)), Effect(AjaxClient[Api].deleteTodo(item.id).call().map(UpdateAllTodos)))
  }
}

/**
  * Handles actions related to the Motd
  *
  * @param modelRW Reader/Writer to access the model
  */
class MotdHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {
  implicit val runner = new RunAfterJS

  override def handle = {
    case action: UpdateMotd =>
      val updateF = action.effect(AjaxClient[Api].welcome("User X").call())(identity)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}




case class Messages(items: Seq[MessageItem]) {
  def updated(newItem: MessageItem) = {
    items.indexWhere(_.id == newItem.id) match {
      case -1 =>
        // add new
        Messages(items :+ newItem)
      case idx =>
        // replace old
        Messages(items.updated(idx, newItem))
    }
  }
  def remove(item: MessageItem) = Messages(items.filterNot(_ == item))
}

class MessageHandler[M](modelRW: ModelRW[M, Pot[Messages]]) extends ActionHandler(modelRW) {
  override def handle = {
    case RefreshMessages =>
      effectOnly(Effect(AjaxClient[Api].getMessages().call().map(UpdateAllMessages)))
    case UpdateAllMessages(messages) =>
      // got new messages, update model
      updated(Ready(Messages(messages)))
    case UpdateMessage(item) =>
      // make a local update and inform server
      updated(value.map(_.updated(item)), Effect(AjaxClient[Api].updateMessage(item).call().map(UpdateAllMessages)))
    case DeleteMessage(item) =>
      // make a local update and inform server
      updated(value.map(_.remove(item)), Effect(AjaxClient[Api].deleteMessage(item.id).call().map(UpdateAllMessages)))
  }
}

case class Events(items: Seq[EventItem]) {
  def updated(newItem: EventItem) = {
    items.indexWhere(_.id == newItem.id) match {
      case -1 =>
        // add new
        Events(items :+ newItem)
      case idx =>
        // replace old
        Events(items.updated(idx, newItem))
    }
  }
  def remove(item: MessageItem) = Events(items.filterNot(_ == item))
}

class EventHandler[M](modelRW: ModelRW[M, Pot[Events]]) extends ActionHandler(modelRW) {
  override def handle = {
    case RefreshEvents =>
      effectOnly(Effect(AjaxClient[Api].getEvents().call().map(UpdateAllEvents)))
    case UpdateAllEvents(events) =>
      // got new messages, update model
      updated(Ready(Events(events)))
    case UpdateEvent(item) =>
      // make a local update and inform server
      updated(value.map(_.updated(item)), Effect(AjaxClient[Api].updateEvent(item).call().map(UpdateAllEvents)))
    case DeleteMessage(item) =>
      // make a local update and inform server
      updated(value.map(_.remove(item)), Effect(AjaxClient[Api].deleteEvent(item.id).call().map(UpdateAllEvents)))
  }
}



// Application circuit
object SPACircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  // initial application model
  override protected def initialModel = RootModel(Empty, Empty, Empty, Empty)
  // combine all handlers into one
  override protected val actionHandler = combineHandlers(
    new TodoHandler(zoomRW(_.todos)((m, v) => m.copy(todos = v))),
    new MotdHandler(zoomRW(_.motd)((m, v) => m.copy(motd = v))),
    new MessageHandler(zoomRW(_.messages)((m, v) => m.copy(messages = v))),
    new EventHandler(zoomRW(_.events)((m, v) => m.copy(events = v)))
  )
}