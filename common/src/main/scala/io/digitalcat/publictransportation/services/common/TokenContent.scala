package io.digitalcat.publictransportation.services.common

import play.api.libs.json.{Format, Json}

case class TokenContent(companyId: String, userId: String, username: String)
object TokenContent
{
  implicit val format: Format[TokenContent] = Json.format
}