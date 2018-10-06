package com.alexitc.playsonify.models

sealed trait RequestContext[L] {
  def lang: L
}

sealed trait HasModel[T] {
  def model: T
}

final case class PublicContext[L](lang: L) extends RequestContext[L]
final case class PublicContextWithModel[T, L](model: T, lang: L)
    extends RequestContext[L] with HasModel[T]

final case class AuthenticatedContext[+A, L](auth: A, lang: L) extends RequestContext[L]
final case class AuthenticatedContextWithModel[+A, T, L](auth: A, model: T, lang: L)
    extends RequestContext[L] with HasModel[T]
