package io.digitalcat.publictransportation.services.identity.impl

import java.util.UUID

import akka.Done
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor.ReadSideHandler
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}

import scala.concurrent.{ExecutionContext, Future}

class IdentityEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext) extends ReadSideProcessor[IdentityEvent] {
  private var insertUserStatement: PreparedStatement = _
  private var reportIdToReservedUsernamesStatement: PreparedStatement = _
  private var reportIdToReservedEmailsStatement: PreparedStatement = _

  override def buildHandler(): ReadSideHandler[IdentityEvent] = {
    readSide.builder[IdentityEvent]("identityEventOffset")
      .setPrepare { tag =>
        prepareStatements()
      }.setEventHandler[UserCreated](insertUser)
      .build()
  }

  override def aggregateTags: Set[AggregateEventTag[IdentityEvent]] = {
    IdentityEvent.Tag.allTags
  }

  private def prepareStatements(): Future[Done] = {
    for {
      insertUser <- session.prepare("INSERT INTO users(id, client_id, username, email, first_name, last_name, hashed_password) VALUES (?, ?, ?, ?, ?, ?, ?)")
      reportIdToReservedUsernames <- session.prepare("UPDATE reserved_usernames SET user_id = ? WHERE username = ?")
      reportIdToReservedEmails <- session.prepare("UPDATE reserved_emails SET user_id = ? WHERE email = ?")
    } yield {
      insertUserStatement = insertUser
      reportIdToReservedUsernamesStatement = reportIdToReservedUsernames
      reportIdToReservedEmailsStatement = reportIdToReservedEmails
      Done
    }
  }

  private def insertUser(user: EventStreamElement[UserCreated]) = {
    Future.successful(
      List(
        insertUserStatement.bind(
          user.event.userId,
          UUID.fromString(user.entityId),
          user.event.username,
          user.event.email,
          user.event.firstName,
          user.event.lastName,
          user.event.hashedPassword
        ),
        reportIdToReservedUsernamesStatement.bind(user.event.userId, user.event.username),
        reportIdToReservedEmailsStatement.bind(user.event.userId, user.event.email)
      )
    )
  }
}
