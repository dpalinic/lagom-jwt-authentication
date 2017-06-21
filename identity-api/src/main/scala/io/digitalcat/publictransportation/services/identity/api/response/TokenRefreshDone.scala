package io.digitalcat.publictransportation.services.identity.api.response

import play.api.libs.json.{Format, Json}

case class TokenRefreshDone(authToken: String)

object TokenRefreshDone {
  implicit val format: Format[TokenRefreshDone] = Json.format
}