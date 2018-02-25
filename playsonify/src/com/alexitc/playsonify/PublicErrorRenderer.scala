package com.alexitc.playsonify

import com.alexitc.playsonify.models._
import play.api.libs.json.{JsValue, Json}

class PublicErrorRenderer {

  import PublicErrorRenderer._

  /**
   * Converts a [[PublicError]] to a [[JsValue]].
   */
  def renderPublicError(publicError: PublicError): JsValue = publicError match {
    case e: GenericPublicError =>
      val obj = Json.obj(
        "type" -> GenericErrorType,
        "message" -> e.message
      )
      Json.toJson(obj)

    case e: FieldValidationError =>
      val obj = Json.obj(
        "type" -> FieldValidationErrorType,
        "field" -> e.field,
        "message" -> e.message
      )
      Json.toJson(obj)

    case e: HeaderValidationError =>
      val obj = Json.obj(
        "type" -> HeaderValidationErrorType,
        "header" -> e.header,
        "message" -> e.message
      )
      Json.toJson(obj)
  }

  def renderPrivateError(errorId: ErrorId) = {
    Json.obj(
      "type" -> ServerErrorType,
      "errorId" -> errorId.string
    )
  }
}

object PublicErrorRenderer {
  val GenericErrorType = "generic-error"
  val FieldValidationErrorType = "field-validation-error"
  val HeaderValidationErrorType = "header-validation-error"
  val ServerErrorType = "server-error"
}
