package com.alexitc.example

import com.alexitc.playsonify.core.I18nService
import com.alexitc.playsonify.models._

sealed trait UserError

object UserError {

  case object UserAlreadyExist extends UserError with ConflictError {
    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val message = i18nService.render("user.error.alreadyExist")
      val error = FieldValidationError("email", message)
      List(error)
    }
  }

  case object UserNotFound extends UserError with NotFoundError {
    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val message = i18nService.render("user.error.notFound")
      val error = FieldValidationError("userId", message)
      List(error)
    }
  }

  case object UserEmailIncorrect extends UserError with InputValidationError {
    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val message = i18nService.render("user.error.incorrectEmail")
      val error = FieldValidationError("email", message)
      List(error)
    }
  }
}
