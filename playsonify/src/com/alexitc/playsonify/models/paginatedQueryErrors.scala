package com.alexitc.playsonify.models

import com.alexitc.playsonify.core.I18nService

sealed trait PaginatedQueryError

case object PaginatedQueryOffsetError extends PaginatedQueryError with InputValidationError {
  override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
    val message = i18nService.render("error.paginatedQuery.offset.invalid")
    val error = FieldValidationError("offset", message)
    List(error)
  }
}

case class PaginatedQueryLimitError(maxValue: Int) extends PaginatedQueryError with InputValidationError {
  override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
    val message = i18nService.render("error.paginatedQuery.limit.invalid", maxValue)
    val error = FieldValidationError("limit", message)
    List(error)
  }
}
