package com.alexitc.example

import com.alexitc.playsonify.models.{AuthenticationError, HeaderValidationError, PublicError}
import play.api.i18n.{Lang, MessagesApi}

sealed trait SimpleAuthError
case object InvalidAuthorizationHeaderError extends SimpleAuthError with AuthenticationError {

  override def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError] = {
    val message = messagesApi("auth.error.invalidToken")
    val error = HeaderValidationError("Authorization", message)
    List(error)
  }
}

