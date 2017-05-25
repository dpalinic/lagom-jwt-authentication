package io.digitalcat.publictransportation.services.identity.impl

import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import io.digitalcat.publictransportation.services.common.GeneratedIdDone
import io.digitalcat.publictransportation.services.identity.api.{RegisteredClientDone, UserCreationDone}
import play.api.libs.json.{Format, Json}

sealed trait IdentityCommand

case class RegisterClient(
 company: String,
 firstName: String,
 lastName: String,
 email: String,
 username: String,
 password: String
) extends PersistentEntity.ReplyType[GeneratedIdDone] with IdentityCommand
object RegisterClient
{
  implicit val format: Format[RegisterClient] = Json.format
}

case class CreateUser(
 firstName: String,
 lastName: String,
 email: String,
 username: String,
 password: String
) extends PersistentEntity.ReplyType[UserCreationDone] with IdentityCommand
object CreateUser
{
  implicit val format: Format[CreateUser] = Json.format
}

case class GetRegisteredClient() extends PersistentEntity.ReplyType[RegisteredClientDone] with IdentityCommand