package io.digitalcat.publictransportation.services.common.validation

case class ValidationException[A](
  validatedObject: A,
  message: String,
  errors: Set[ValidationError]
)
extends RuntimeException(ValidationException.generateMessage(message, errors), None.orNull)
object ValidationException {
  def generateMessage(message: String, errors: Set[ValidationError]) = {
    s"$message\n" + generateErrors(errors)
  }
  private def generateErrors(errors: Set[ValidationError]) = {
    errors.map(error => s"${error.key}: ${error.message}\n").mkString("- ", "- ", "")
  }
}

case class ValidationError(key: String, message: String)