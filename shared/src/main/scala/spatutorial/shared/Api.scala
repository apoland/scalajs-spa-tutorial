package spatutorial.shared

trait Api {
  // message of the day
  def welcome(name: String): String

  // get Todo items
  def getTodos(): Seq[TodoItem]

  // update a Todo
  def updateTodo(item: TodoItem): Seq[TodoItem]

  // delete a Todo
  def deleteTodo(itemId: String): Seq[TodoItem]

  def getMessages(): Seq[MessageItem]
  def updateMessage(item: MessageItem): Seq[MessageItem]
  def deleteMessage(itemId: String): Seq[MessageItem]

}
