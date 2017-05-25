package io.digitalcat.publictransportation.services.identity.impl

import java.util.UUID

import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, NotFound, TransportErrorCode}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import io.digitalcat.publictransportation.services.common.GeneratedIdDone
import io.digitalcat.publictransportation.services.identity.api.{RegisteredClientDone, UserCreationDone}
import io.digitalcat.publictransportation.services.identity.impl.util.SecurePasswordHashing

import scala.collection.immutable.Seq


class IdentityEntity extends PersistentEntity {
  override type Command = IdentityCommand
  override type Event = IdentityEvent
  override type State = IdentityState

  override def behavior: Behavior = {
    Actions().onCommand[RegisterClient, GeneratedIdDone] {
      case (RegisterClient(company, firstName, lastName, username, email, password), ctx, state) =>
        state.client match {
          case Some(_) =>
            ctx.invalidCommand(s"User ${entityId} is already registered")
            ctx.done
          case None =>
            val hashedPassword = SecurePasswordHashing.hashPassword(password)
            val userId = UUID.randomUUID().toString

            ctx.thenPersistAll(
              ClientCreated(company),
              UserCreated(userId, firstName, lastName, username, email, hashedPassword)
            ) { () =>
              ctx.reply(GeneratedIdDone(entityId))
            }
        }
    }
    .onReadOnlyCommand[GetRegisteredClient, RegisteredClientDone] {
      case (GetRegisteredClient(), ctx, state) =>
        state.client match {
          case None =>
            throw new NotFound(TransportErrorCode.BadRequest, new ExceptionMessage(s"Client registered with ${entityId} can't be found", ""))
          case Some(client: Client) =>
            ctx.reply(RegisteredClientDone(
                entityId,
                client.name,
                client.users.map(user => UserCreationDone(
                  user.userId,
                  user.firstName,
                  user.lastName,
                  user.username,
                  user.email
                ))
              )
            )
        }
    }
    .onEvent {
      case (ClientCreated(company), _) => IdentityState(Some(Client(company)))
      case (UserCreated(userId, firstName, lastName, username, email, password), state) => state.addUser(User(userId, firstName, lastName, username, email, password))
    }
  }

  override def initialState: IdentityState = IdentityState(None)
}

object IdentitySerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[GeneratedIdDone],
    JsonSerializer[CreateUser],
    JsonSerializer[RegisterClient],
    JsonSerializer[ClientCreated],
    JsonSerializer[UserCreated],
    JsonSerializer[IdentityState]
  )
}

