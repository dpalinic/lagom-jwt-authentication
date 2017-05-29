package io.digitalcat.publictransportation.services.common.authentication

import com.lightbend.lagom.scaladsl.api.transport.Forbidden
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.typesafe.config.ConfigFactory
import pdi.jwt.{Jwt, JwtAlgorithm, JwtJson}
import play.api.libs.json._

import scala.util.{Failure, Success}

object Authentication {
  val secret = ConfigFactory.load().getString("jwt.secret")
  val algorithm = JwtAlgorithm.HS512

  def authenticated[Request, Response](serviceCall: TokenContent => ServerServiceCall[Request, Response]) =
    ServerServiceCall.compose { requestHeader =>
      val tokenContent = requestHeader.getHeader("Authorization")
        .map(header => sanitizeToken(header))
        .filter(rawToken => validateToken(rawToken))
        .map(rawToken => decodeToken(rawToken))

      tokenContent match {
        case Some(tokenContent) => serviceCall(tokenContent)
        case _ => throw Forbidden("User must be authenticated to access this service call")
      }
    }

  private def sanitizeToken(header: String) = header.replaceFirst("Bearer ", "")

  private def validateToken(token: String) = Jwt.isValid(token, secret, Seq(algorithm))

  private def decodeToken(token: String) = {
    val jsonTokenContent = JwtJson.decode(token, secret, Seq(algorithm))

    jsonTokenContent match {
      case Success(json) => Json.parse(json.content).as[TokenContent]
      case Failure(_) => throw Forbidden(s"Unable to decode token")
    }
  }
}
