package com.alexitc.example

import com.alexitc.playsonify.core.I18nService
import com.alexitc.playsonify.models.{AuthenticationError, HeaderValidationError, PublicError}

sealed trait SimpleAuthError

object SimpleAuthError {

  case object InvalidAuthorizationHeader extends SimpleAuthError with AuthenticationError {

    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val message = i18nService.render("auth.error.invalidToken")
      val error = HeaderValidationError("Authorization", message)
      List(error)
    }
  }
}
