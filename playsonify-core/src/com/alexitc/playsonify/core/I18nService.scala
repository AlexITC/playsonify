package com.alexitc.playsonify.core

trait I18nService[L] {

  def render(key: String, args: Any*)(implicit lang: L): String
}
