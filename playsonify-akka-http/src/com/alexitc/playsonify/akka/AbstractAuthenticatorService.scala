package com.alexitc.playsonify.akka

import akka.http.scaladsl.model.HttpRequest
import com.alexitc.playsonify.core.FutureApplicationResult

trait AbstractAuthenticatorService[+A] {

  def authenticate(request: HttpRequest): FutureApplicationResult[A]

}
