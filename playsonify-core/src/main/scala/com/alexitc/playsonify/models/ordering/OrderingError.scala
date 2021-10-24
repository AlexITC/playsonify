package com.alexitc.playsonify.models.ordering

import com.alexitc.playsonify.core.I18nService
import com.alexitc.playsonify.models.{FieldValidationError, InputValidationError, PublicError}

sealed trait OrderingError

object OrderingError {

  case object UnknownField extends OrderingError with InputValidationError {

    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val message = i18nService.render("error.ordering.unknownField")
      val error = FieldValidationError("orderBy", message)

      List(error)
    }
  }

  case object InvalidCondition extends OrderingError with InputValidationError {

    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val message = i18nService.render("error.ordering.unknownCondition")
      val error = FieldValidationError("orderBy", message)

      List(error)
    }
  }

  case object InvalidFormat extends OrderingError with InputValidationError {

    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val message = i18nService.render("error.ordering.invalidFormat")
      val error = FieldValidationError("orderBy", message)

      List(error)
    }
  }
}
