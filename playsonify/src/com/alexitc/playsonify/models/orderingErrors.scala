package com.alexitc.playsonify.models

import play.api.i18n.{Lang, MessagesApi}

sealed trait OrderingError

case object UnknownOrderingFieldError extends OrderingError with InputValidationError {

  override def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError] = {
    val message = messagesApi("error.ordering.unknownField")
    val error = FieldValidationError("orderBy", message)

    List(error)
  }
}

case object InvalidOrderingConditionError extends OrderingError with InputValidationError {

  override def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError] = {
    val message = messagesApi("error.ordering.unknownCondition")
    val error = FieldValidationError("orderBy", message)

    List(error)
  }
}

case object InvalidOrderingFormatError extends OrderingError with InputValidationError {

  override def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError] = {
    val message = messagesApi("error.ordering.invalidFormat")
    val error = FieldValidationError("orderBy", message)

    List(error)
  }
}
