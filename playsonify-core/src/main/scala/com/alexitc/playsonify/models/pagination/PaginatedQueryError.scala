package com.alexitc.playsonify.models.pagination

import com.alexitc.playsonify.core.I18nService
import com.alexitc.playsonify.models.{FieldValidationError, InputValidationError, PublicError}

sealed trait PaginatedQueryError

object PaginatedQueryError {

  case object InvalidOffset extends PaginatedQueryError with InputValidationError {
    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val message = i18nService.render("error.paginatedQuery.offset.invalid")
      val error = FieldValidationError("offset", message)
      List(error)
    }
  }

  case class InvalidLimit(maxValue: Int) extends PaginatedQueryError with InputValidationError {
    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val message = i18nService.render("error.paginatedQuery.limit.invalid", maxValue)
      val error = FieldValidationError("limit", message)
      List(error)
    }
  }
}
