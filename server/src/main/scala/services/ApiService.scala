package services

import java.util.{UUID, Date}

import spatutorial.shared._

class ApiService extends Api {
  var todos = Seq(
    TodoItem("41424344-4546-4748-494a-4b4c4d4e4f50", 0x61626364, "Wear shirt that says “Life”. Hand out lemons on street corner.", TodoLow, false),
    TodoItem("2", 0x61626364, "Make vanilla pudding. Put in mayo jar. Eat in public.", TodoNormal, false),
    TodoItem("3", 0x61626364, "Walk away slowly from an explosion without looking back.", TodoHigh, false),
    TodoItem("4", 0x61626364, "Sneeze in front of the pope. Get blessed.", TodoNormal, true)
  )

  override def welcome(name: String): String =
    s"Welcome to Red Card Robot, $name! Time is now ${new Date}"

  var messages = Seq(
    MessageItem("41424344-4546-4748-494a-4b4c4d4e4f50", 0x61626364, "Message 1", MessageLow, false),
    MessageItem("2", 0x61626364, "Message 2", MessageNormal, false),
    MessageItem("3", 0x61626364, "Message 3", MessageHigh, false),
    MessageItem("4", 0x61626364, "Message 4.", MessageNormal, true)
  )

  override def getTodos(): Seq[TodoItem] = {
    // provide some fake Todos
    Thread.sleep(300)
    println(s"Sending ${todos.size} Todo items")
    todos
  }

  // update a Todo
  override def updateTodo(item: TodoItem): Seq[TodoItem] = {
    // TODO, update database etc :)
    if(todos.exists(_.id == item.id)) {
      todos = todos.collect {
        case i if i.id == item.id => item
        case i => i
      }
      println(s"Todo item was updated: $item")
    } else {
      // add a new item
      val newItem = item.copy(id = UUID.randomUUID().toString)
      todos :+= newItem
      println(s"Todo item was added: $newItem")
    }
    Thread.sleep(300)
    todos
  }

  // delete a Todo
  override def deleteTodo(itemId: String): Seq[TodoItem] = {
    println(s"Deleting item with id = $itemId")
    Thread.sleep(300)
    todos = todos.filterNot(_.id == itemId)
    todos
  }


  override def getMessages(): Seq[MessageItem] = {
    // provide some fake Messages
    Thread.sleep(300)
    println(s"Sending ${messages.size} Message items")
    messages
  }

  // update a Message
  override def updateMessage(item: MessageItem): Seq[MessageItem] = {
    // Message, update database etc :)
    if(messages.exists(_.id == item.id)) {
      messages = messages.collect {
        case i if i.id == item.id => item
        case i => i
      }
      println(s"Message item was updated: $item")
    } else {
      // add a new item
      val newItem = item.copy(id = UUID.randomUUID().toString)
      messages :+= newItem
      println(s"Message item was added: $newItem")
    }
    Thread.sleep(300)
    messages
  }

  // delete a Message
  override def deleteMessage(itemId: String): Seq[MessageItem] = {
    println(s"Deleting item with id = $itemId")
    Thread.sleep(300)
    messages = messages.filterNot(_.id == itemId)
    messages
  }

}
