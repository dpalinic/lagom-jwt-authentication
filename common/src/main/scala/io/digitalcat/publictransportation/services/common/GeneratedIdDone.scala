package io.digitalcat.publictransportation.services.common

import play.api.libs.json.{Format, Json}

case class GeneratedIdDone(id: String)
object GeneratedIdDone
{
  implicit val format: Format[GeneratedIdDone] = Json.format[GeneratedIdDone]
}
