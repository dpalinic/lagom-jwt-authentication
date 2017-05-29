package io.digitalcat.publictransportation.services.common.response

import play.api.libs.json.{Format, Json}

case class GeneratedIdDone(id: String)
object GeneratedIdDone {
  implicit val format: Format[GeneratedIdDone] = Json.format
}
