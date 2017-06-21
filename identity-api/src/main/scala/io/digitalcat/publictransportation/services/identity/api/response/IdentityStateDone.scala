package io.digitalcat.publictransportation.services.identity.api.response

import play.api.libs.json.{Format, Json}

case class IdentityStateDone(
  id: String,
  company: String,
  users: Seq[User]
)

object IdentityStateDone {
  implicit val format: Format[IdentityStateDone] = Json.format
}

case class User(
  id: String,
  firstName: String,
  lastName: String,
  email: String,
  username: String
)
object User {
  implicit val format: Format[User] = Json.format
}