package io.digitalcat.publictransportation.services.common.exception

import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, TransportErrorCode, TransportException}

case class ValidationException[A](
  validatedObject: A,
  message: String,
  errors: Set[ValidationError]
)
extends TransportException(TransportErrorCode.BadRequest, ValidationException.generateMessage(message, errors), null)
object ValidationException {
  def generateMessage(message: String, errors: Set[ValidationError]) = {
    val details = s"$message\n" + generateErrors(errors)

    new ExceptionMessage("ValidationException", details)
  }
  private def generateErrors(errors: Set[ValidationError]) = {
    errors.map(error => s"${error.key}: ${error.message}\n").mkString("- ", "- ", "")
  }
}

case class ValidationError(key: String, message: String)