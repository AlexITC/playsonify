package com.alexitc.playsonify.play

import play.api.i18n.Lang

sealed trait RequestContext {
  def lang: Lang
}

object RequestContext {

  final case class PublicContext(lang: Lang) extends RequestContext

  final case class PublicContextWithModel[+T](
      model: T,
      lang: Lang)
      extends RequestContext

  final case class AuthenticatedContext[+A](
      auth: A,
      lang: Lang)
      extends RequestContext

  final case class AuthenticatedContextWithModel[+A, +T](
      auth: A,
      model: T,
      lang: Lang)
      extends RequestContext
}
