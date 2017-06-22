package io.digitalcat.publictransportation.services.common.exception.handling

import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.{DefaultExceptionSerializer, RawExceptionMessage}
import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, MessageProtocol}
import io.digitalcat.publictransportation.services.common.exception.ValidationException
import play.api.Environment
import play.api.libs.json.Json

import scala.collection.immutable.Seq
import scala.util.control.NonFatal

class CustomExceptionSerializer(environment: Environment) extends DefaultExceptionSerializer(environment = environment) {
  override def serialize(exception: Throwable, accept: Seq[MessageProtocol]): RawExceptionMessage = {
    val rawExceptionMessage = super.serialize(exception, accept)

    val extendedMessageBytes = exception match {
      case validationException: ValidationException[_] => constructMessageForValidationException(rawExceptionMessage, validationException)
      case _ => rawExceptionMessage.message
    }

    RawExceptionMessage(rawExceptionMessage.errorCode, rawExceptionMessage.protocol, extendedMessageBytes)
  }

  private def decodeToExtendedMessage(message: ByteString) = {
    val messageJson = try {
      Json.parse(message.iterator.asInputStream)
    } catch {
      case NonFatal(_) =>
        Json.obj()
    }

    val jsonParseResult = for {
      name <- (messageJson \ "name").validate[String]
      detail <- (messageJson \ "detail").validate[String]
    } yield new ExceptionMessage(name, detail)

    jsonParseResult.asOpt.getOrElse(throw new IllegalStateException("Can't decode ExtendedMessage"))
  }

  private def constructMessageForValidationException(rawExceptionMessage: RawExceptionMessage, exception: ValidationException[_]) = {
    val extendedMessage = decodeToExtendedMessage(rawExceptionMessage.message)

    ByteString.fromString(Json.stringify(Json.obj(
      "name" -> extendedMessage.name,
      "detail" -> extendedMessage.detail,
      "errors" -> Json.toJson(exception.errors.map(error => Json.obj("key" -> error.key, "message" -> error.message)))
    )))
  }

}




