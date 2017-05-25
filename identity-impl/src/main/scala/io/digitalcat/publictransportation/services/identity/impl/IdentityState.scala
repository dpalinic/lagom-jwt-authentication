package io.digitalcat.publictransportation.services.identity.impl

import play.api.libs.json.{Format, Json}


case class IdentityState(client: Option[Client]) {
  def addUser(user: User): IdentityState = client match {
    case None => throw new IllegalStateException("User can't be added before client is created")
    case Some(client) =>
      val newUsers =  client.users :+ user
      IdentityState(Some(client.copy(users = newUsers)))
  }
}
object IdentityState {
  implicit val format: Format[IdentityState] = Json.format
}

case class Client(
 name: String,
 users: Seq[User] = Seq.empty
)
object Client {
  implicit val format: Format[Client] = Json.format
}

case class User(
  userId: String,
  firstName: String,
  lastName: String,
  email: String,
  username: String,
  password: String
)
object User {
  implicit val format: Format[User] = Json.format
}