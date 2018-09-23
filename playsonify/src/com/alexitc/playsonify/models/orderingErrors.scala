package com.alexitc.playsonify.models

import com.alexitc.playsonify.core.I18nService

sealed trait OrderingError

case object UnknownOrderingFieldError extends OrderingError with InputValidationError {

  override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
    val message = i18nService.render("error.ordering.unknownField")
    val error = FieldValidationError("orderBy", message)

    List(error)
  }
}

case object InvalidOrderingConditionError extends OrderingError with InputValidationError {

  override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
    val message = i18nService.render("error.ordering.unknownCondition")
    val error = FieldValidationError("orderBy", message)

    List(error)
  }
}

case object InvalidOrderingFormatError extends OrderingError with InputValidationError {

  override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
    val message = i18nService.render("error.ordering.invalidFormat")
    val error = FieldValidationError("orderBy", message)

    List(error)
  }
}
