package com.alexitc.playsonify.akka

import akka.http.scaladsl.model.HttpRequest

sealed trait RequestContext {
  def request: HttpRequest
}

object RequestContext {

  final case class PublicContext(request: HttpRequest) extends RequestContext

  final case class PublicContextWithModel[+T](request: HttpRequest, model: T) extends RequestContext

  final case class AuthenticatedContext[+A](request: HttpRequest, auth: A) extends RequestContext

  final case class AuthenticatedContextWithModel[+A, +T](request: HttpRequest, auth: A, model: T) extends RequestContext
}
