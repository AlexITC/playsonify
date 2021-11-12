package com.alexitc.playsonify.play.common

import com.alexitc.playsonify.core.FutureApplicationResult
import com.alexitc.playsonify.play.AbstractAuthenticatorService
import org.scalactic.{One, Or}
import play.api.libs.json.JsValue
import play.api.mvc.Request
import play.api.test.Helpers.AUTHORIZATION

import scala.concurrent.Future

class CustomAuthenticator extends AbstractAuthenticatorService[CustomUser] {

  override def authenticate(request: Request[JsValue]): FutureApplicationResult[CustomUser] = {
    val header = request.headers
      .get(AUTHORIZATION)
      .map(CustomUser.apply)

    val result = Or.from(header, One(CustomError.FailedAuthError))
    Future.successful(result)
  }
}
