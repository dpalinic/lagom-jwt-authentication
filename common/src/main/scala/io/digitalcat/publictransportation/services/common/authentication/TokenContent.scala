package io.digitalcat.publictransportation.services.common.authentication

import play.api.libs.json.{Format, Json}

case class TokenContent(clientId: String, userId: String, username: String)
object TokenContent {
  implicit val format: Format[TokenContent] = Json.format
}