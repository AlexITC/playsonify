package com.alexitc.playsonify.models

import play.api.i18n.{Lang, MessagesApi}

/**
 * This is not a sealed trait to allow customizing the top-level errors.
 */
trait ApplicationError {

  /**
   * Map to a list of [[PublicError]].
   *
   * @param messagesApi the api used to render messages in different languages.
   * @param lang the user preferred language.
   * @return the list of [[PublicError]]
   */
  def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError]
}

trait ServerError extends ApplicationError {
  /**
   * No all server errors could have a related exception.
   */
  def cause: Option[Throwable]

  /**
   * A server error is private, hence, an empty list is returned.
   */
  override def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError] = List.empty
}

trait InputValidationError extends ApplicationError
trait ConflictError extends ApplicationError
trait NotFoundError extends ApplicationError
trait AuthenticationError extends ApplicationError

case class WrappedExceptionError(exception: Throwable) extends ServerError {
  override def cause: Option[Throwable] = Option(exception)
}
