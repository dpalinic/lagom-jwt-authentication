package io.digitalcat.publictransportation.services.identity.impl

import java.util.UUID
import javax.inject.Inject

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.Forbidden
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import io.digitalcat.publictransportation.services.common.authentication.TokenContent
import io.digitalcat.publictransportation.services.common.authentication.Authentication._
import io.digitalcat.publictransportation.services.identity.api.{IdentityService, UserLoginDone}
import io.digitalcat.publictransportation.services.identity.impl.util.{JwtTokenUtil, SecurePasswordHashing}

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

  override def getIdentityState(id: String) = ServiceCall { _ =>
    val ref = persistentRegistry.refFor[IdentityEntity](id)

    ref.ask(GetIdentityState())
  }

  override def loginUser() = ServiceCall { request =>
    val result: Future[UserLoginDone] = for {
      client <- findClientByCredentials(request.username, request.password)
      token = client.map(client => {
        JwtTokenUtil.tokenize(client)
      }).orElse(throw Forbidden("User and password combination not found"))
    }
    yield {
      UserLoginDone(token.get.authToken, token.get.refreshToken)
    }

    result
  }

  private def findClientByCredentials(username: String, password: String): Future[Option[TokenContent]] = {
    val result = db.selectOne("SELECT id, company_id, username, hashed_password FROM users WHERE username = ? ALLOW FILTERING", username).map {
      case Some(row) => {
        if (SecurePasswordHashing.validatePassword(password, row.getString("hashed_password")))
          Option(TokenContent(
            clientId = row.getUUID("company_id").toString,
            userId = row.getUUID("id").toString,
            username = row.getString("username")))
        else
          Option.empty
      }
      case None => Option.empty
    }
    // TODO: remove allow filtering and design schema better
    // TODO: move to repository and generate TokenContent outside this method
    result
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
