package services

import java.util.{UUID, Date}

import spatutorial.shared._

class ApiService extends Api {
  var members = Seq(
    MemberItem("41424344-4546-4748-494a-4b4c4d4e4f50", 0x61626364, "Andrew Poland", "317-270-0251", false),
    MemberItem("41424344-4546-4748-494a-4b4c4d4e4f50", 0x61626364, "Bob Dole", "317-222-2222", false),
  MemberItem("41424344-4546-4748-494a-4b4c4d4e4f50", 0x61626364, "Teeve Torbes", "317-333-3333", false),
  MemberItem("41424344-4546-4748-494a-4b4c4d4e4f50", 0x61626364, "Turd Furgeson", "317-555-1212", false)

  )

  override def welcome(name: String): String =
    s"Welcome to Red Card Robot, $name! Time is now ${new Date}"

  var messages = Seq(
    MessageItem("41424344-4546-4748-494a-4b4c4d4e4f50", 0x61626364, "Reminder: RCR Rehearsal 3 is in three days! - Sent to ALL", MessageLow, false),
    MessageItem("2", 0x61626364, "Reminder: RCR Rehearsal 4 is in three days! - Sent to ALL", MessageNormal, false),
    MessageItem("3", 0x61626364, "Are you still coming to Game 1 on 3/19?  Reply YES or NO.  - Sent to ALL", MessageHigh, false),
    MessageItem("4", 0x61626364, "Are you still coming to Game 2 on 4/2?  Reply YES or NO.  - Sent to ALL", MessageNormal, true)
  )

  var events = Seq(
    EventItem("41424344-4546-4748-494a-4b4c4d4e4f50", 0x61626364, "Rehearsal 1", EventLow, false),
    EventItem("2", 0x61626364, "Rehearsal 2", EventNormal, false),
    EventItem("5", 0x61626364, "Rehearsal 3", EventLow, false),
    EventItem("6", 0x61626364, "Rehearsal 4", EventNormal, false),
    EventItem("3", 0x61626364, "Game 1", EventHigh, false),
    EventItem("4", 0x61626364, "Game 2", EventNormal, true)
  )

  override def getTodos(): Seq[MemberItem] = {
    // provide some fake Todos
    Thread.sleep(300)
    println(s"Sending ${members.size} Todo items")
    members
  }

  // update a Todo
  override def updateTodo(item: MemberItem): Seq[MemberItem] = {
    // TODO, update database etc :)
    if(members.exists(_.id == item.id)) {
      members = members.collect {
        case i if i.id == item.id => item
        case i => i
      }
      println(s"Todo item was updated: $item")
    } else {
      // add a new item
      val newItem = item.copy(id = UUID.randomUUID().toString)
      members :+= newItem
      println(s"Todo item was added: $newItem")
    }
    Thread.sleep(300)
    members
  }

  // delete a Todo
  override def deleteTodo(itemId: String): Seq[MemberItem] = {
    println(s"Deleting item with id = $itemId")
    Thread.sleep(300)
    members = members.filterNot(_.id == itemId)
    members
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


  override def getEvents(): Seq[EventItem] = {
    Thread.sleep(300)
    println(s"Sending ${events.size} Event items")
    events
  }



  // delete a Event
  override def deleteEvent(itemId: String): Seq[EventItem] = {
    println(s"Deleting event with id = $itemId")
    Thread.sleep(300)
    events = events.filterNot(_.id == itemId)
    events
  }

  // update a Event
  override def updateEvent(item: EventItem): Seq[EventItem] = {
    if(events.exists(_.id == item.id)) {
      events = events.collect {
        case i if i.id == item.id => item
        case i => i
      }
      println(s"Event item was updated: $item")
    } else {
      // add a new item
      val newItem = item.copy(id = UUID.randomUUID().toString)
      events :+= newItem
      println(s"Event item was added: $newItem")
    }
    Thread.sleep(300)
    events
  }



}
