package com.alexitc.example

import javax.inject.Inject

import com.alexitc.playsonify.ApplicationErrorMapper
import com.alexitc.playsonify.models._
import play.api.i18n.{Lang, MessagesApi}

class MyApplicationErrorMapper @Inject() (messagesApi: MessagesApi) extends ApplicationErrorMapper {

  override def toPublicErrorList(error: ApplicationError)(implicit lang: Lang): Seq[PublicError] = error match {
    // server errors are not supposed to be mapped to PublicError
    case _: ServerError => List.empty

    // at the moment, you are required to catch this error,
    // it happens when there are errors while mapping the request body to JSON.
    case JsonFieldValidationError(path, errors) =>
      val field = path.path.map(_.toJsonString.replace(".", "")).mkString(".")
      errors.map { messageKey =>
        val message = messagesApi(messageKey.string)
        FieldValidationError(field, message)
      }

    // having a top-level error allow us to delegate the mapping task to specific functions
    case e: UserError => List(renderUserError(e))

    // it is better avoid a catch-all here in order to get a RuntimeException when an error is not mapped,
    // I know, this is ugly, hopefully we will improve this in the future, so, please, ensure you have covered
    // all possible errors in your tests.
  }

  // we use message keys to support multiple languages, store these keys in the messages file.
  private def renderUserError(error: UserError)(implicit lang: Lang) = error match {
    case UserAlreadyExistError =>
      val message = messagesApi("user.error.alreadyExist")
      FieldValidationError("email", message)
      
    case UserNotFoundError =>
      val message = messagesApi("user.error.notFound")
      FieldValidationError("userId", message)

    case UserEmailIncorrectError =>
      val message = messagesApi("user.error.incorrectEmail")
      FieldValidationError("email", message)
  }
}

