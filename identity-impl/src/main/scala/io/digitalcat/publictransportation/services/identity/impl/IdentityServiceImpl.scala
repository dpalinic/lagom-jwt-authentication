package io.digitalcat.publictransportation.services.identity.impl

import java.util.UUID
import javax.inject.Inject

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.Forbidden
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import io.digitalcat.publictransportation.services.common.authentication.Authentication._
import io.digitalcat.publictransportation.services.common.authentication.TokenContent
import io.digitalcat.publictransportation.services.identity.api.{IdentityService, UserLoginDone}
import io.digitalcat.publictransportation.services.identity.impl.util.{JwtTokenUtil, SecurePasswordHashing}

import scala.concurrent.ExecutionContext

class IdentityServiceImpl @Inject()(
  persistentRegistry: PersistentEntityRegistry,
  identityRepository: IdentityRepository
)(implicit ec: ExecutionContext) extends IdentityService
{
  override def registerClient() = ServiceCall { request =>
    val ref = persistentRegistry.refFor[IdentityEntity](UUID.randomUUID().toString)

    ref.ask(
      RegisterClient(
        company = request.company,
        firstName = request.firstName,
        lastName = request.lastName,
        email = request.email,
        username = request.username,
        password = request.password
      )
    )
  }

  override def getIdentityState(id: String) = ServiceCall { _ =>
    val ref = persistentRegistry.refFor[IdentityEntity](id)

    ref.ask(GetIdentityState())
  }

  override def loginUser() = ServiceCall { request =>
    def passwordMatches(providedPassword: String, storedHashedPassword: String) = SecurePasswordHashing.validatePassword(providedPassword, storedHashedPassword)

    for {
      maybeUser <- identityRepository.findUserByCredentials(request.username)

      token = maybeUser.filter(user => passwordMatches(request.password, user.hashedPassword))
        .map(user =>
            TokenContent(
            clientId = user.clientId,
            userId = user.id,
            username = user.username
          )
        )
        .map(tokenContent => JwtTokenUtil.tokenize(tokenContent))
        .getOrElse(throw Forbidden("User and password combination not found"))
    }
    yield {
      UserLoginDone(token.authToken, token.refreshToken)
    }
  }

  override def createUser() = authenticated { tokenContent =>
    ServerServiceCall { request =>
      val ref = persistentRegistry.refFor[IdentityEntity](tokenContent.clientId)

      ref.ask(
        CreateUser(
          firstName = request.firstName,
          lastName = request.lastName,
          email = request.email,
          username = request.username,
          password = request.password
        )
      )
    }
  }
}
