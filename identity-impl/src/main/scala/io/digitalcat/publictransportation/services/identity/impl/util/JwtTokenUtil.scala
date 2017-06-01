package io.digitalcat.publictransportation.services.identity.impl.util

import com.typesafe.config.ConfigFactory
import io.digitalcat.publictransportation.services.common.authentication.TokenContent
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import play.api.libs.json.{Format, Json}

object JwtTokenUtil {
  val secret = ConfigFactory.load().getString("jwt.secret")
  val authExpiration = ConfigFactory.load().getInt("jwt.token.auth.expirationInSeconds")
  val refreshExpiration = ConfigFactory.load().getInt("jwt.token.refresh.expirationInSeconds")
  val algorithm = JwtAlgorithm.HS512

  def generateTokens(content: TokenContent)(implicit format: Format[TokenContent]): Token = {
    val authClaim = JwtClaim(Json.toJson(content).toString())
      .expiresIn(authExpiration)
      .issuedNow

    val refreshClaim = JwtClaim(Json.toJson(content.copy(isRefreshToken = true)).toString())
      .expiresIn(refreshExpiration)
      .issuedNow

    val authToken = JwtJson.encode(authClaim, secret, algorithm)
    val refreshToken = JwtJson.encode(refreshClaim, secret, algorithm)

    Token(
      authToken = authToken,
      refreshToken = Some(refreshToken)
    )
  }

  def generateAuthTokenOnly(content: TokenContent)(implicit format: Format[TokenContent]): Token = {
    val authClaim = JwtClaim(Json.toJson(content).toString())
      .expiresIn(authExpiration)
      .issuedNow

    val authToken = JwtJson.encode(authClaim, secret, algorithm)

    Token(
      authToken = authToken,
      None
    )
  }
}

case class Token(authToken: String, refreshToken: Option[String])
object Token {
  implicit val format: Format[Token] = Json.format
}
