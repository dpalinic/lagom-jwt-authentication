package io.digitalcat.publictransportation.services.identity.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.api.deser.DefaultExceptionSerializer
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import io.digitalcat.publictransportation.services.common.exception.handling.CustomExceptionSerializer
import io.digitalcat.publictransportation.services.identity.api.IdentityService
import play.api.libs.ws.ahc.AhcWSComponents

class IdentityLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new IdentityApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new IdentityApplication(context) with LagomDevModeComponents

  override def describeServices = List(
    readDescriptor[IdentityService]
  )
}

abstract class IdentityApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents
{

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[IdentityService](wire[IdentityServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = IdentitySerializerRegistry

  // Register dependencies
  lazy val identityRepository = wire[IdentityRepository]

  override lazy val defaultExceptionSerializer = new CustomExceptionSerializer(environment)

  // Register the public-transportation-services persistent entity
  persistentEntityRegistry.register(wire[IdentityEntity])

  // Register read side processors
  readSide.register(wire[IdentityEventProcessor])
}
