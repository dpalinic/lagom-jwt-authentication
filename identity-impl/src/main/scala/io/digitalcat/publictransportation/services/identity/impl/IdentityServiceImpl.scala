package io.digitalcat.publictransportation.services.identity.impl

import java.util.UUID
import javax.inject.Inject

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import io.digitalcat.publictransportation.services.identity.api.{IdentityService, UserLogin, UserLoginDone}

import scala.concurrent.ExecutionContext

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

  override def createUser(clientId: String) = ServiceCall { request =>
    val ref = persistentRegistry.refFor[IdentityEntity](clientId)

    ref.ask(CreateUser(request.firstName, request.lastName, request.email, request.username, request.password))
  }

  override def loginUser() = ???
}
