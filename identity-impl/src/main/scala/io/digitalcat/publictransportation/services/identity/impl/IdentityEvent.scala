package io.digitalcat.publictransportation.services.identity.impl

import java.util.UUID

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag}
import play.api.libs.json.{Format, Json}

object IdentityEvent {
  val NumShards = 5
  val Tag = AggregateEventTag.sharded[IdentityEvent]("IdentityEvent", NumShards)
}

sealed trait IdentityEvent extends AggregateEvent[IdentityEvent] {
  override def aggregateTag(): AggregateEventShards[IdentityEvent] = IdentityEvent.Tag
}

case class ClientCreated(company: String) extends IdentityEvent
object ClientCreated {
  implicit val format: Format[ClientCreated] = Json.format
}

case class UserCreated(userId: UUID, firstName: String, lastName: String, email: String, username: String, hashedPassword: String) extends IdentityEvent
object UserCreated {
  implicit val format: Format[UserCreated] = Json.format
}