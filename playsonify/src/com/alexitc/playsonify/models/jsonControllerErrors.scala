package com.alexitc.playsonify.models

import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.JsPath

sealed trait JsonControllerErrors
case class JsonFieldValidationError(path: JsPath, errors: List[MessageKey])
    extends JsonControllerErrors
    with InputValidationError {

  override def toPublicErrorList(messagesApi: MessagesApi)(implicit lang: Lang): List[PublicError] = {
    val field = path.path.map(_.toJsonString.replace(".", "")).mkString(".")
    errors.map { messageKey =>
      val message = messagesApi(messageKey.string)

      FieldValidationError(field, message)
    }
  }
}
