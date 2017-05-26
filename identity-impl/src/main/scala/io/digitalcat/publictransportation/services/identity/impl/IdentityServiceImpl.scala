package io.digitalcat.publictransportation.services.identity.impl

import java.util.UUID
import javax.inject.Inject

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import io.digitalcat.publictransportation.services.common.TokenContent
import io.digitalcat.publictransportation.services.identity.api.{ClientRegistrationDone, IdentityService, UserLogin, UserLoginDone}
import io.digitalcat.publictransportation.services.identity.impl.util.{JwtTokenGenerator, SecurePasswordHashing}
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

class IdentityServiceImpl @Inject()(
 persistentRegistry: PersistentEntityRegistry,
 readSide: CassandraReadSide,
 db: CassandraSession
)(implicit ec: ExecutionContext) extends IdentityService
{
  override def registerClient() = ServiceCall { request =>
    val ref = persistentRegistry.refFor[IdentityEntity](UUID.randomUUID().toString)

    ref.ask(RegisterClient(request.company, request.firstName, request.lastName, request.email, request.username, request.password))
  }

  override def getRegisteredClient(id: String) = ServiceCall { _ =>
    val ref = persistentRegistry.refFor[IdentityEntity](id)

    ref.ask(GetRegisteredClient())
  }

  override def loginUser() = ServiceCall { request =>
    val result: Future[UserLoginDone] = for {
      client <- findClientByCredentials(request.username, request.password)
      token = client.map(client => {
        JwtTokenGenerator.tokenize(client)
      }).orElse(throw new IllegalStateException("User and password combination not found"))
    } yield {
        UserLoginDone(token.get.authToken, token.get.refreshToken)
    }

    result
  }

  private def findClientByCredentials(username: String, password: String): Future[Option[TokenContent]] = {
    val result = db.selectOne("SELECT id, company_id, username, hashed_password FROM users WHERE username = ? ALLOW FILTERING", username).map {
      case Some(row) => {
        if (SecurePasswordHashing.validatePassword(password, row.getString("hashed_password")))
          Option(TokenContent(row.getUUID("id").toString, row.getUUID("company_id").toString, row.getString("username")))
        else
          Option.empty
      }
      case None => Option.empty
    }
    // TODO: remove allow filtering and design schema better
    result
  }

  override def createUser(clientId: String) = ServiceCall { request =>
    val ref = persistentRegistry.refFor[IdentityEntity](clientId)

    ref.ask(CreateUser(request.firstName, request.lastName, request.email, request.username, request.password))
  }

}
