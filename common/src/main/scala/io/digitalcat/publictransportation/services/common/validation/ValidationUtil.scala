package io.digitalcat.publictransportation.services.common.validation

import com.wix.accord.transform.ValidationTransform
import com.wix.accord.{Descriptions, Failure, Violation, validate => accordValidate}
import io.digitalcat.publictransportation.services.common.exception.{ValidationError, ValidationException}

object ValidationUtil {
  def validate[A](underValidation: A)(implicit validator: ValidationTransform.TransformedValidator[A]) = {
    val validationResult = accordValidate(underValidation)

    validationResult match {
      case failure: Failure =>  throw ValidationException(underValidation, "Object failed validation", extractValidationErrors(failure.violations))
      case _ =>
    }
  }
  private def extractValidationErrors(violations: Set[Violation]) = {
    violations.map(violation => {
      ValidationError(
        key = Descriptions.render(violation.description),
        message = violation.constraint
      )
    })
  }
}


