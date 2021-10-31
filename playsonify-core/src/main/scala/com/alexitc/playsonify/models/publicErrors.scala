package com.alexitc.playsonify.models

/** A PublicError could be displayed to anyone.
  */
sealed trait PublicError
case class GenericPublicError(message: String) extends PublicError
case class FieldValidationError(field: String, message: String) extends PublicError
case class HeaderValidationError(header: String, message: String) extends PublicError
case class InternalError(id: ErrorId, message: String) extends PublicError

object PublicError {

  def genericError(message: String): PublicError = {
    GenericPublicError(message)
  }
}
