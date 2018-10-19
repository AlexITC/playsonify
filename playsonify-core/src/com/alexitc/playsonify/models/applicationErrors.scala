package com.alexitc.playsonify.models

import com.alexitc.playsonify.core.I18nService

/**
 * This is not a sealed trait to allow customizing the top-level errors.
 */
trait ApplicationError {

  /**
   * Map to a list of [[PublicError]].
   *
   * @param i18nService the api used to render messages in different languages.
   * @param lang the user preferred language.
   * @tparam L the type of the language required by the [[I18nService]]
   * @return the list of [[PublicError]]
   */
  def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError]
}

trait ServerError extends ApplicationError {

  import ServerError._

  def id: ErrorId

  /**
   * No all server errors could have a related exception.
   */
  def cause: Option[Throwable]

  /**
   * A server error is private, hence, an empty list is returned.
   */
  override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
    val message = i18nService.render(messageKey.string)
    val error = InternalError(id, message)
    List(error)
  }
}

object ServerError {
  val messageKey: MessageKey = MessageKey("error.internal")
}

trait InputValidationError extends ApplicationError
trait ConflictError extends ApplicationError
trait NotFoundError extends ApplicationError
trait AuthenticationError extends ApplicationError

case class WrappedExceptionError(id: ErrorId, exception: Throwable) extends ServerError {
  override def cause: Option[Throwable] = Option(exception)
}
