package com.alexitc.playsonify.common

import com.alexitc.playsonify.models._
import play.api.i18n.{Lang, MessagesApi}

sealed trait CustomError

object CustomError {

  case object InputError extends CustomError with InputValidationError {
    override def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError] = {
      val publicError = FieldValidationError("field", "just an error")
      List(publicError)
    }
  }

  case object DuplicateError extends CustomError with ConflictError {
    override def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError] = {
      val publicError = FieldValidationError("anotherField", "just another error")
      List(publicError)
    }
  }

  case object FailedAuthError extends CustomError with AuthenticationError {
    override def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError] = {
      val publicError = HeaderValidationError("Authorization", "Invalid auth")
      List(publicError)
    }
  }

}
