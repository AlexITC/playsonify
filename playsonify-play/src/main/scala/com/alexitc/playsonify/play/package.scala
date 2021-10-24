package com.alexitc.playsonify

import _root_.play.api.libs.json.JsPath
import com.alexitc.playsonify.core.I18nService
import com.alexitc.playsonify.models.{FieldValidationError, InputValidationError, MessageKey, PublicError}

package object play {

  case class JsonFieldValidationError(path: JsPath, errors: List[MessageKey]) extends InputValidationError {

    override def toPublicErrorList[L](i18nService: I18nService[L])(implicit lang: L): List[PublicError] = {
      val field = path.path.map(_.toJsonString.replace(".", "")).mkString(".")
      errors.map { messageKey =>
        val message = i18nService.render(messageKey.string)

        FieldValidationError(field, message)
      }
    }
  }
}
