package io.digitalcat.publictransportation.services.identity.impl

import java.util.UUID

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, Forbidden}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import io.digitalcat.publictransportation.services.common.authentication.AuthenticationServiceComposition._
import io.digitalcat.publictransportation.services.common.authentication.TokenContent
import io.digitalcat.publictransportation.services.identity.api.IdentityService
import io.digitalcat.publictransportation.services.identity.impl.util.{JwtTokenUtil, SecurePasswordHashing}

import scala.concurrent.{ExecutionContext, Future}
import io.digitalcat.publictransportation.services.common.validation.ValidationUtil._
import io.digitalcat.publictransportation.services.identity.api.request.WithUserCreationFields
import io.digitalcat.publictransportation.services.identity.api.response.{TokenRefreshDone, UserLoginDone}

class IdentityServiceImpl(
  persistentRegistry: PersistentEntityRegistry,
  identityRepository: IdentityRepository
)(implicit ec: ExecutionContext) extends IdentityService
{
  override def registerClient() = ServiceCall { request =>
    validate(request)

    val canProceed = tryReserveUsernameAndEmail(request)

    canProceed.flatMap(canProceed => {
      canProceed match {
        case true => {
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
        case false => throw BadRequest("Either username or email is already taken.")
      }
    })
  }

  override def getIdentityState() = authenticated { tokenContent =>
    ServerServiceCall { _ =>
      val ref = persistentRegistry.refFor[IdentityEntity](tokenContent.clientId.toString)

      ref.ask(GetIdentityState())
    }
  }

  override def loginUser() = ServiceCall { request =>
    def passwordMatches(providedPassword: String, storedHashedPassword: String) = SecurePasswordHashing.validatePassword(providedPassword, storedHashedPassword)

    validate(request)

    for {
      maybeUser <- identityRepository.findUserByUsername(request.username)

      token = maybeUser.filter(user => passwordMatches(request.password, user.hashedPassword))
        .map(user =>
          TokenContent(
            clientId = user.clientId,
            userId = user.id,
            username = user.username
          )
        )
        .map(tokenContent => JwtTokenUtil.generateTokens(tokenContent))
        .getOrElse(throw Forbidden("Username and password combination not found"))
    }
    yield {
      UserLoginDone(token.authToken, token.refreshToken.getOrElse(throw new IllegalStateException("Refresh token missing")))
    }
  }

  override def refreshToken() = authenticatedWithRefreshToken { tokenContent =>
    ServerServiceCall { _ =>
      val token = JwtTokenUtil.generateAuthTokenOnly(tokenContent)

      Future.successful(TokenRefreshDone(token.authToken))
    }
  }

  override def createUser() = authenticated { tokenContent =>
    ServerServiceCall { request =>
      validate(request)

      val canProceed = tryReserveUsernameAndEmail(request)

      canProceed.flatMap(canProceed => {
        canProceed match {
          case true => {
            val ref = persistentRegistry.refFor[IdentityEntity](tokenContent.clientId.toString)

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
          case false => throw BadRequest("Either username or email is already taken.")
        }
      })
    }
  }

  private def tryReserveUsernameAndEmail(userCreation: WithUserCreationFields) = {
    for {
      userReserved <- identityRepository.reserveUsername(userCreation.username)
      emailReserved <- identityRepository.reserveEmail(userCreation.email)
    }
    yield userReserved && emailReserved
  }
}
