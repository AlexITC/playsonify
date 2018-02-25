package com.alexitc.playsonify.models

import play.api.libs.json.JsPath

sealed trait JsonControllerErrors
case class JsonFieldValidationError(path: JsPath, errors: Seq[MessageKey])
    extends JsonControllerErrors
    with InputValidationError
