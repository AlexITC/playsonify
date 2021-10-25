package com.alexitc.playsonify.play

import javax.inject.Inject

import com.alexitc.playsonify.core.I18nService
import play.api.i18n.{Lang, MessagesApi}

class I18nPlayService @Inject() (messagesApi: MessagesApi) extends I18nService[Lang] {

  override def render(key: String, args: Any*)(implicit lang: Lang): String = {
    messagesApi.apply(key, args)
  }
}
