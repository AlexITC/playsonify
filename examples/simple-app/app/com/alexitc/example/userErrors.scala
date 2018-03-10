package com.alexitc.example

import com.alexitc.playsonify.models._
import play.api.i18n.{Lang, MessagesApi}

sealed trait UserError

case object UserAlreadyExistError extends UserError with ConflictError {
  override def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError] = {
    val message = messagesApi("user.error.alreadyExist")
    val error = FieldValidationError("email", message)
    List(error)
  }
}

case object UserNotFoundError extends UserError with NotFoundError {
  override def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError] = {
    val message = messagesApi("user.error.notFound")
    val error = FieldValidationError("userId", message)
    List(error)
  }
}

case object UserEmailIncorrectError extends UserError with InputValidationError {
  override def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError] = {
    val message = messagesApi("user.error.incorrectEmail")
    val error = FieldValidationError("email", message)
    List(error)
  }
}
