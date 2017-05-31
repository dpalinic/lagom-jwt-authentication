package io.digitalcat.publictransportation.services.identity.impl

import java.util.UUID

import akka.Done
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor.ReadSideHandler
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}
import play.libs.Json

import scala.concurrent.{ExecutionContext, Future}

class IdentityEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext) extends ReadSideProcessor[IdentityEvent] {
  private var insertAuctionStatement: PreparedStatement = _

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
      CREATE TABLE IF NOT EXISTS users_by_username (
        username        varchar,
        id              uuid,
        client_id       uuid,
        first_name      varchar,
        last_name       varchar,
        email           varchar,
        hashed_password varchar, PRIMARY KEY (username)
      )
    """)
  }


  private def prepareStatements(): Future[Done] = {
    for {
      insert <- session.prepare("INSERT INTO users_by_username(username, id, client_id, first_name, last_name, email, hashed_password) VALUES (?, ?, ?, ?, ?, ?, ?)")
    } yield {
      insertAuctionStatement = insert
      Done
    }
  }

  private def insertUser(user: EventStreamElement[UserCreated]) = {
    Future.successful(
      List(
        insertAuctionStatement.bind(
          user.event.username,
          UUID.fromString(user.event.userId),
          UUID.fromString(user.entityId),
          user.event.firstName,
          user.event.lastName,
          user.event.email,
          user.event.hashedPassword
        )
      )
    )
  }
}
