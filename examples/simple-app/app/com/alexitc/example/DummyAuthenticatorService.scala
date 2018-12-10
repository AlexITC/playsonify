package com.alexitc.example

import com.alexitc.playsonify.core.FutureApplicationResult
import com.alexitc.playsonify.play.AbstractAuthenticatorService
import org.scalactic.{One, Or}
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.mvc.Request

import scala.concurrent.Future
import scala.util.Try

class DummyAuthenticatorService extends AbstractAuthenticatorService[Int] {

  override def authenticate(request: Request[JsValue]): FutureApplicationResult[Int] = {
    val userIdMaybe = request
      .headers
      .get(HeaderNames.AUTHORIZATION)
      .flatMap { header => Try(header.toInt).toOption }

    val result = Or.from(userIdMaybe, One(SimpleAuthError.InvalidAuthorizationHeader))
    Future.successful(result)
  }
}
