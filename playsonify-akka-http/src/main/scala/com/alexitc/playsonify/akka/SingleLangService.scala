package com.alexitc.playsonify.akka

import com.alexitc.playsonify.core.I18nService
import com.alexitc.playsonify.models.ServerError

class SingleLangService(knownTranslations: Map[String, String]) extends I18nService[String] {

  override def render(key: String, args: Any*)(implicit lang: String = ""): String = {
    knownTranslations.getOrElse(key, key)
  }
}

object SingleLangService {

  val DefaultTranslations = Map(
    "error.path.missing" -> "Field required",
    "error.expected.jsstring" -> "String expected",
    "error.expected.jsnumber" -> "Number expecteds",
    ServerError.messageKey.string -> "Internal server error"
  )

  val Default = new SingleLangService(DefaultTranslations)
}
