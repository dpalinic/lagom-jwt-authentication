package io.digitalcat.publictransportation.services.identity.impl

import java.util.{UUID}

import akka.Done
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor.ReadSideHandler
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}

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
      CREATE TABLE IF NOT EXISTS users (
        id              uuid,
        company_id      uuid,
        first_name      TEXT,
        last_name       TEXT,
        email           TEXT,
        username        TEXT,
        hashed_password TEXT, PRIMARY KEY (id)
      )
    """)
  }


  private def prepareStatements(): Future[Done] = {
    for {
      insert <- session.prepare("INSERT INTO users(id, company_id, first_name, last_name, email, username, hashed_password) VALUES (?, ?, ?, ?, ?, ?, ?)")
    } yield {
      insertAuctionStatement = insert
      Done
    }
  }

  private def insertUser(user: EventStreamElement[UserCreated]) = {
    Future.successful(
      List(
        insertAuctionStatement.bind(
          UUID.fromString(user.event.userId),
          UUID.fromString(user.entityId),
          user.event.firstName,
          user.event.lastName,
          user.event.email,
          user.event.username,
          user.event.hashedPassword
        )
      )
    )
  }
}
