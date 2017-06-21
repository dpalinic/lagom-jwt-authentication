package io.digitalcat.publictransportation.services.identity.impl

import java.util.UUID

import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

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
    db.selectOne("INSERT INTO reserved_usernames (username) VALUES (?) IF NOT EXISTS", username).map {
      case Some(row) => row.getBool("[applied]")
      case None => false
    }
  }

  def reserveEmail(email: String): Future[Boolean] = {
    db.selectOne("INSERT INTO reserved_emails (email) VALUES (?) IF NOT EXISTS", email).map {
      case Some(row) => row.getBool("[applied]")
      case None => false
    }
  }
}

case class UserByUsername(username: String, id: UUID, clientId: UUID, hashedPassword: String)
