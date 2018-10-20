package com.alexitc.playsonify.akka

import com.alexitc.playsonify.core.I18nService
import com.alexitc.playsonify.models.ServerError

class SingleLangService extends I18nService[String] {

  import SingleLangService._

  override def render(key: String, args: Any*)(implicit lang: String = ""): String = {
    KnownTranslations.getOrElse(key, key)
  }
}

object SingleLangService {

  private val KnownTranslations = Map(
    "error.path.missing" -> "Field required",
    "error.expected.jsstring" -> "String expected",
    "error.expected.jsnumber" -> "Number expecteds",
    ServerError.messageKey.string -> "Internal server error"
  )
}
