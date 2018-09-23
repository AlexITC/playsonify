package com.alexitc.playsonify.models

import com.alexitc.playsonify.core.I18nService
import play.api.libs.json.JsPath

sealed trait JsonControllerErrors
case class JsonFieldValidationError(path: JsPath, errors: List[MessageKey])
    extends JsonControllerErrors
    with InputValidationError {

  override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
    val field = path.path.map(_.toJsonString.replace(".", "")).mkString(".")
    errors.map { messageKey =>
      val message = i18nService.render(messageKey.string)

      FieldValidationError(field, message)
    }
  }
}
