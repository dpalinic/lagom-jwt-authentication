package io.digitalcat.publictransportation.services.identity.impl

import java.sql.Timestamp
import java.util.UUID

import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import io.digitalcat.publictransportation.services.common.date.DateUtcUtil

import scala.concurrent.{ExecutionContext, Future}

class IdentityRepository(db: CassandraSession)(implicit ec: ExecutionContext) {

  def findUserByUsername(username: String): Future[Option[UserByUsername]] = {
    val result = db.selectOne("SELECT id, client_id, username, hashed_password FROM users_by_username WHERE username = ?", username).map {
      case Some(row) => Option(
        UserByUsername(
          username = row.getString("username"),
          id = row.getUUID("id"),
          clientId = row.getUUID("client_id"),
          hashedPassword = row.getString("hashed_password")
        )
      )
      case None => Option.empty
    }

    result
  }

  def reserveUsername(username: String): Future[Boolean] = {
    val createdOn = new Timestamp(DateUtcUtil.now().getMillis)

    db.selectOne("INSERT INTO reserved_usernames (username, created_on) VALUES (?, ?) IF NOT EXISTS", username, createdOn).map {
      case Some(row) => row.getBool("[applied]")
      case None => false
    }
  }

  def unreserveUsername(username: String) = {
    db.executeWrite("DELETE FROM reserved_usernames WHERE username = ?", username)
  }

  def reserveEmail(email: String): Future[Boolean] = {
    val createdOn = new Timestamp(DateUtcUtil.now().getMillis)

    db.selectOne("INSERT INTO reserved_emails (email, created_on) VALUES (?, ?) IF NOT EXISTS", email, createdOn).map {
      case Some(row) => row.getBool("[applied]")
      case None => false
    }
  }

  def unreserveEmail(email: String) = {
    db.executeWrite("DELETE FROM reserved_emails WHERE email = ?", email)
  }
}

case class UserByUsername(username: String, id: UUID, clientId: UUID, hashedPassword: String)
