package com.alexitc.playsonify.akka.common

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.Authorization
import com.alexitc.playsonify.akka._
import com.alexitc.playsonify.core.FutureApplicationResult
import com.alexitc.playsonify.models.{ErrorId, ServerError}
import org.scalactic.{One, Or}

import scala.concurrent.Future

class CustomJsonController extends AbstractJsonController(new CustomJsonController.CustomJsonComponents) {

  override protected def onServerError(error: ServerError, id: ErrorId): Unit = {
    error
      .cause
      .orElse {
        println(s"Server error: $error, id = ${error.id}")
        None
      }
      .foreach { cause =>
        println(s"Server error: $error, id = $id")
        cause.printStackTrace()
      }
  }
}

object CustomJsonController {

  class CustomJsonComponents extends JsonControllerComponents[CustomUser] {

    override def i18nService: SingleLangService = SingleLangService.Default

    override def publicErrorRenderer = new PublicErrorRenderer

    override def authenticatorService = new CustomAuthenticator
  }

  class CustomAuthenticator extends AbstractAuthenticatorService[CustomUser] {

    override def authenticate(request: HttpRequest): FutureApplicationResult[CustomUser] = {

      val header = request
          .header[Authorization]
          .map(_.value())
          .map(CustomUser.apply)

      val result = Or.from(header, One(CustomError.FailedAuthError))
      Future.successful(result)
    }
  }
}
