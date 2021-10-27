package com.alexitc.playsonify.akka.controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.alexitc.playsonify.akka.common.{CustomError, CustomJsonController, CustomModel}
import com.alexitc.playsonify.core.FutureApplicationResult
import org.scalactic.{Bad, Good, Many}

import scala.concurrent.Future

class TestController(implicit mat: Materializer) extends CustomJsonController {

  import Context._

  def routes: Route = withGlobalHandler {
    publicWithInputRoutes ~ publicNoInputRoutes ~ authenticatedNoInputRoutes ~ authenticatedWithInputRoutes
  }

  def authenticatedNoInputRoutes = {
    pathPrefix("authenticated") {
      path("model") {
        get {
          authenticated[CustomModel] { ctx: Authenticated =>
            val result = CustomModel(0, ctx.auth.id)
            Future.successful(Good(result))
          }
        }
      } ~
      path("model-custom-status") {
        get {
          authenticated[CustomModel](StatusCodes.Created) { ctx: Authenticated =>
            val result = CustomModel(0, ctx.auth.id)
            Future.successful(Good(result))
          }
        }
      }
    }
  }

  def authenticatedWithInputRoutes = {
    pathPrefix("authenticated-input") {
      path("model") {
        post {
          authenticatedInput { ctx: HasModel[CustomModel] with Authenticated =>
            Future.successful(Good(ctx.model))
          }
        }
      } ~
      path("model-custom-status") {
        post {
          authenticatedInput(StatusCodes.Created) { ctx: HasModel[CustomModel] with Authenticated =>
            Future.successful(Good(ctx.model))
          }
        }
      }
    }
  }

  def publicNoInputRoutes = {
    pathPrefix("no-input") {
      path("model") {
        get {
          public { ctx =>
            val result = CustomModel(0, "none")
            Future.successful(Good(result))
          }
        }
      } ~
      path("model-custom-status") {
        get {
          public(StatusCodes.Created) { ctx =>
            val result = CustomModel(0, "none")
            Future.successful(Good(result))
          }
        }
      } ~
      path("errors") {
        get {
          public { ctx =>
            errorsResponse
          }
        }
      } ~
      path("exception") {
        get {
          public { ctx =>
            exceptionResponse
          }
        }
      }
    }
  }

  def publicWithInputRoutes = {
    pathPrefix("input") {
      path("model") {
        post {
          publicInput { ctx: HasModel[CustomModel] =>
            Future.successful(Good(ctx.model))
          }
        }
      } ~
      path("model-custom-status") {
        post {
          publicInput(StatusCodes.Created) { ctx: HasModel[CustomModel] =>
            Future.successful(Good(ctx.model))
          }
        }
      } ~
      path("errors") {
        post {
          publicInput { ctx: HasModel[CustomModel] =>
            errorsResponse
          }
        }
      } ~
      path("exception") {
        post {
          publicInput { ctx: HasModel[CustomModel] =>
            exceptionResponse
          }
        }
      }
    }
  }

  def errorsResponse: FutureApplicationResult[CustomModel] = {
    val badResult = Bad(Many(CustomError.InputError, CustomError.DuplicateError))
    Future.successful(badResult)
  }

  def exceptionResponse: FutureApplicationResult[CustomModel] = {
    Future.failed(new RuntimeException("Unknown failure"))
  }
}
