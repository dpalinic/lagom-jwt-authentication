package io.digitalcat.publictransportation.services.identity.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import io.digitalcat.publictransportation.services.common.response.GeneratedIdDone
import play.api.libs.json.{Format, Json}

trait IdentityService extends Service {
  def getIdentityState(entityId: String): ServiceCall[NotUsed, IdentityStateDone]
  def registerClient(): ServiceCall[ClientRegistration, GeneratedIdDone]
  def loginUser(): ServiceCall[UserLogin, UserLoginDone]
  def createUser(): ServiceCall[UserCreation, GeneratedIdDone]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("identity-service").withCalls(
      restCall(Method.GET, "/api/state/identity/:entityId", getIdentityState _),
      restCall(Method.POST, "/api/client/registration", registerClient _),
      restCall(Method.POST, "/api/user/login", loginUser _),
      restCall(Method.POST, "/api/user", createUser _)
    ).withAutoAcl(true)
    // @formatter:on
  }
}

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

case class ClientRegistration(
  company: String,
  firstName: String,
  lastName: String,
  email: String,
  username: String,
  password: String
)
object ClientRegistration {
  implicit val format: Format[ClientRegistration] = Json.format
}

case class UserLogin (
  username: String,
  password: String
)
object UserLogin {
  implicit val format: Format[UserLogin] = Json.format
}

case class UserLoginDone (
  authToken: String,
  refreshToken: String
)
object UserLoginDone {
  implicit val format: Format[UserLoginDone] = Json.format
}

case class UserCreation(
  firstName: String,
  lastName: String,
  email: String,
  username: String,
  password: String
)
object UserCreation {
  implicit val format: Format[UserCreation] = Json.format
}

