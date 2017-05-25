package io.digitalcat.publictransportation.services.identity.api

import akka.{NotUsed}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}
import io.digitalcat.publictransportation.services.common.GeneratedIdDone

trait IdentityService extends Service
{
  def registerClient(): ServiceCall[ClientRegistration, GeneratedIdDone]
  def getRegisteredClient(id: String): ServiceCall[NotUsed, RegisteredClientDone]
  def loginUser(): ServiceCall[UserLogin, UserLoginDone]
  def createUser(clientId: String): ServiceCall[UserCreation, UserCreationDone]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("identity-service").withCalls(
      restCall(Method.POST, "/api/client/registration", registerClient _),
      restCall(Method.GET, "/api/client/registration/:id", getRegisteredClient _),
      restCall(Method.POST, "/api/user/login", loginUser _),
      restCall(Method.POST, "/api/client/:clientId/user", createUser _) // TODO: put clientId in token
    ).withAutoAcl(true)
    // @formatter:on
  }
}

case class ClientRegistration(
  company: String,
  firstName: String,
  lastName: String,
  email: String,
  username: String,
  password: String
)
object ClientRegistration
{
  implicit val format: Format[ClientRegistration] = Json.format[ClientRegistration]
}

case class RegisteredClientDone(
  id: String,
  company: String,
  users: Seq[UserCreationDone]
)
object RegisteredClientDone
{
  implicit val format: Format[RegisteredClientDone] = Json.format[RegisteredClientDone]
}

case class UserLogin
(
  user: String,
  password: String
)
object UserLogin
{
  implicit val format: Format[UserLogin] = Json.format[UserLogin]
}

case class UserLoginDone
(
  token: String
)
object UserLoginDone
{
  implicit val format: Format[UserLoginDone] = Json.format[UserLoginDone]
}

case class UserCreation(
  firstName: String,
  lastName: String,
  email: String,
  username: String,
  password: String
)
object UserCreation
{
  implicit val format: Format[UserCreation] = Json.format[UserCreation]
}

case class UserCreationDone(
  id: String,
  firstName: String,
  lastName: String,
  email: String,
  username: String
)
object UserCreationDone
{
  implicit val format: Format[UserCreationDone] = Json.format[UserCreationDone]
}
