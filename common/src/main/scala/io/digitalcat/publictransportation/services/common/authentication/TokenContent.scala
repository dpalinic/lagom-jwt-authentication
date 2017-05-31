package io.digitalcat.publictransportation.services.common.authentication

import java.util.UUID

import play.api.libs.json.{Format, Json}

case class TokenContent(clientId: UUID, userId: UUID, username: String)
object TokenContent {
  implicit val format: Format[TokenContent] = Json.format
}