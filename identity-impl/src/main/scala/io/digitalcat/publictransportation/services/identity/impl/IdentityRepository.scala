package io.digitalcat.publictransportation.services.identity.impl

import java.util.UUID
import javax.inject.Inject

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
}

case class UserByUsername(username: String, id: UUID, clientId: UUID, hashedPassword: String)
