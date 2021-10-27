package com.alexitc.playsonify.akka.common

import com.alexitc.playsonify.core.I18nService
import com.alexitc.playsonify.models._

sealed trait CustomError

object CustomError {

  case object InputError extends CustomError with InputValidationError {
    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val publicError = FieldValidationError("field", "just an error")
      List(publicError)
    }
  }

  case object DuplicateError extends CustomError with ConflictError {
    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val publicError = FieldValidationError("anotherField", "just another error")
      List(publicError)
    }
  }

  case object FailedAuthError extends CustomError with AuthenticationError {
    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val publicError = HeaderValidationError("Authorization", "Invalid auth")
      List(publicError)
    }
  }
}
