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

  override def buildHandler(): ReadSideHandler[IdentityEvent] = {
    readSide.builder[IdentityEvent]("identityEventOffset")
      .setGlobalPrepare(createTable)
      .setPrepare { tag =>
        prepareStatements()
      }.setEventHandler[UserCreated](insertUser)
      .build()
  }

  override def aggregateTags: Set[AggregateEventTag[IdentityEvent]] = {
    IdentityEvent.Tag.allTags
  }

  private def createTable(): Future[Done] = {
    session.executeCreateTable("""
      CREATE TABLE IF NOT EXISTS users (
        id              uuid,
        client_id       uuid,
        username        varchar,
        email           varchar,
        first_name      varchar,
        last_name       varchar,
        hashed_password varchar, PRIMARY KEY (id)
      );
    """)
    session.executeCreateTable("""
      CREATE MATERIALIZED VIEW IF NOT EXISTS users_by_username AS
       SELECT * FROM users
       WHERE username IS NOT NULL
       PRIMARY KEY (username, id)
    """)
  }


  private def prepareStatements(): Future[Done] = {
    for {
      insert <- session.prepare("INSERT INTO users(id, client_id, username, email, first_name, last_name, hashed_password) VALUES (?, ?, ?, ?, ?, ?, ?)")
    } yield {
      insertUserStatement = insert
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
        )
      )
    )
  }
}
