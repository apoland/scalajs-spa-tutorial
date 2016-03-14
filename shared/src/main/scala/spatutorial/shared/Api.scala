package spatutorial.shared

trait Api {
  // message of the day
  def welcome(name: String): String

  // get Todo items
  def getTodos(): Seq[MemberItem]

  // update a Todo
  def updateTodo(item: MemberItem): Seq[MemberItem]

  // delete a Todo
  def deleteTodo(itemId: String): Seq[MemberItem]

  def getMessages(): Seq[MessageItem]
  def updateMessage(item: MessageItem): Seq[MessageItem]
  def deleteMessage(itemId: String): Seq[MessageItem]

  def getEvents(): Seq[EventItem]
  def updateEvent(item: EventItem): Seq[EventItem]
  def deleteEvent(itemId: String): Seq[EventItem]

}
