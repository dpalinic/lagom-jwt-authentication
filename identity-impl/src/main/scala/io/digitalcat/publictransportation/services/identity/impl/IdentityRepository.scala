package io.digitalcat.publictransportation.services.identity.impl

import javax.inject.Inject
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import scala.concurrent.{ExecutionContext, Future}

class IdentityRepository @Inject()(db: CassandraSession)(implicit ec: ExecutionContext) {
  def findUserByCredentials(username: String): Future[Option[UserByUsername]] = {
    val result = db.selectOne("SELECT id, client_id, username, hashed_password FROM users_by_username WHERE username = ?", username).map {
      case Some(row) => Option(
        UserByUsername(
          username = row.getString("username"),
          id = row.getUUID("id").toString,
          clientId = row.getUUID("client_id").toString,
          hashedPassword = row.getString("hashed_password")
        )
      )
      case None => Option.empty
    }

    result
  }
}

case class UserByUsername(username: String, id: String, clientId: String, hashedPassword: String)
