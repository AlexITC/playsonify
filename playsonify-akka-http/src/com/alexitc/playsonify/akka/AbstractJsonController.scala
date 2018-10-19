package com.alexitc.playsonify.akka

import akka.http.scaladsl.marshalling.{ToResponseMarshallable, ToResponseMarshaller}
import akka.http.scaladsl.model.{HttpRequest, StatusCode, StatusCodes}
import akka.http.scaladsl.server.{RequestEntityExpectedRejection, _}
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import com.alexitc.playsonify.core.FutureOr.Implicits.FutureOps
import com.alexitc.playsonify.core.{ApplicationErrors, FutureApplicationResult}
import com.alexitc.playsonify.models._
import com.fasterxml.jackson.core.JsonParseException
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport.PlayJsonError
import org.scalactic.{Bad, Every, Good}
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.util.{Failure, Success}

abstract class AbstractJsonController[+A](
    components: JsonControllerComponents[A])
    extends Directives
    with PlayJsonSupport {

  import AbstractJsonController._
  import RequestContext._

  // Required to complete the calls to render messages
  private implicit val dummyLang: String = ""

  protected def onServerError(error: ServerError, id: ErrorId): Unit

  private val rejectionHandler = {
    RejectionHandler
        .newBuilder()
        .handle { case ValidationRejection(_, Some(e: PlayJsonError)) =>
          val errorList = e
              .error
              .errors
              .map { case (path, errors) =>
                val x = errors
                    .flatMap(_.messages)
                    .map(MessageKey.apply)
                    .toList

                JsonFieldValidationError(path, x)
              }

          // assume that errorList is non empty
          val badResult = Bad(Every(errorList.head, errorList.drop(1): _*))
          val result: FutureApplicationResult[String] = Future.successful(badResult)
          renderResult(StatusCodes.BadRequest, result)
        }
        .handle { case MalformedRequestContentRejection(_, _: JsonParseException) | RequestEntityExpectedRejection =>
          val error = MalformedJsonError
          val badResult = Bad(error).accumulating
          val result: FutureApplicationResult[String] = Future.successful(badResult)
          renderResult(StatusCodes.BadRequest, result)
        }
        .handleNotFound {
          // TODO: Fixme
          println("NOT FOUND")
          val error = RouteNotFoundError
          val badResult = Bad(error).accumulating
          val result: FutureApplicationResult[String] = Future.successful(badResult)
          renderResult(StatusCodes.NotFound, result)
        }
        .result()
  }

  def publicWithInput[I, O](successCode: StatusCode)(
      f: PublicContextWithModel[I] => FutureApplicationResult[O])(
      implicit um: FromRequestUnmarshaller[I],
      rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    handleRejections(rejectionHandler) {
      val directive = entity(as(requestContextWithModelUnmarshaller))
      val g = renderResult(successCode, _: FutureApplicationResult[O])(rm)
      directive.apply(f andThen g)
    }
  }

  def publicWithInput[I, O](
      f: PublicContextWithModel[I] => FutureApplicationResult[O])(
      implicit um: FromRequestUnmarshaller[I],
      rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    publicWithInput[I, O](StatusCodes.OK)(f)
  }

  def publicNoInput[O](
      successCode: StatusCode)(
      f: PublicContext => FutureApplicationResult[O])(
      implicit rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    handleRejections(rejectionHandler) {
      val directive = entity(as(requestContextUnmarshaller))
      val g = renderResult(successCode, _: FutureApplicationResult[O])(rm)
      directive.apply(f andThen g)
    }
  }

  def publicNoInput[O](
      f: PublicContext => FutureApplicationResult[O])(
      implicit rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    publicNoInput(StatusCodes.OK)(f)
  }

  def authenticatedNoInput[O](
      successCode: StatusCode)(
      f: AuthenticatedContext[A] => FutureApplicationResult[O])(
      implicit rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    handleRejections(rejectionHandler) {
      // TODO: unmarshall request after authentication
      val directive = entity(as(requestContextUnmarshaller))
      directive { ctx =>
        extractExecutionContext { implicit ec =>
          val result = for {
            auth <- components.authenticatorService.authenticate(ctx.request).toFutureOr
            authCtx = AuthenticatedContext(ctx.request, auth)
            output <- f(authCtx).toFutureOr
          } yield output

          renderResult(successCode, result.toFuture)
        }
      }
    }
  }

  def authenticatedNoInput[O](
      f: AuthenticatedContext[A] => FutureApplicationResult[O])(
      implicit rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    authenticatedNoInput(StatusCodes.OK)(f)
  }

  def authenticatedWithInput[I, O](successCode: StatusCode)(
      f: AuthenticatedContextWithModel[A, I] => FutureApplicationResult[O])(
      implicit um: FromRequestUnmarshaller[I],
      rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    handleRejections(rejectionHandler) {
      // TODO: unmarshall request after authentication
      val directive = entity(as(requestContextWithModelUnmarshaller))
      directive { ctx =>
        extractExecutionContext { implicit ec =>
          val result = for {
            auth <- components.authenticatorService.authenticate(ctx.request).toFutureOr
            authCtx = AuthenticatedContextWithModel(ctx.request, auth, ctx.model)
            output <- f(authCtx).toFutureOr
          } yield output

          renderResult(successCode, result.toFuture)
        }
      }
    }
  }

  def authenticatedWithInput[I, O](
      f: AuthenticatedContextWithModel[A, I] => FutureApplicationResult[O])(
      implicit um: FromRequestUnmarshaller[I],
      rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    authenticatedWithInput[I, O](StatusCodes.OK)(f)
  }

  def logServerErrors(errorId: ErrorId, errors: ApplicationErrors): Unit = {
    errors
        .collect { case e: ServerError => e }
        .foreach { onServerError(_, errorId) }
  }

  def renderResult[T](
      successCode: StatusCode,
      f: FutureApplicationResult[T])(
      implicit rm: ToResponseMarshaller[T]): Route = {

    onComplete(f) { r =>
      val (resultStatus, response): (StatusCode, ToResponseMarshallable) = r match {
        case Success(Good(x)) => (successCode, ToResponseMarshallable.apply(x))
        case Success(Bad(errors)) =>
          val errorId = ErrorId.create
          logServerErrors(errorId, errors)
          (getResultStatus(errors), renderErrors(errors))

        case Failure(ex) =>
          val errorId = ErrorId.create
          val error = WrappedExceptionError(errorId, ex)
          val errors = Every(error)

          logServerErrors(errorId, errors)
          (getResultStatus(errors), renderErrors(errors))
      }

      mapResponse(_.copy(status = resultStatus)) {
        complete(response)
      }
    }
  }

  // detect response status based on the first error
  def getResultStatus(errors: ApplicationErrors): StatusCode = errors.head match {
    case _: InputValidationError => StatusCodes.BadRequest
    case _: ConflictError => StatusCodes.Conflict
    case _: NotFoundError => StatusCodes.NotFound
    case _: AuthenticationError => StatusCodes.Unauthorized
    case _: ServerError => StatusCodes.InternalServerError
  }

  def renderErrors(errors: ApplicationErrors) = {
    val jsonErrorList = errors.toList
        .flatMap { error =>
          error.toPublicErrorList(components.i18nService)
        }
        .map(components.publicErrorRenderer.renderPublicError)

    Json.obj("errors" -> jsonErrorList)
  }
}

object AbstractJsonController {

  import RequestContext._

  def requestContextWithModelUnmarshaller[T](
      implicit um: FromRequestUnmarshaller[T],
      mat: Materializer): FromRequestUnmarshaller[PublicContextWithModel[T]] = {

    Unmarshaller.apply[HttpRequest, PublicContextWithModel[T]] { implicit ec => request =>
      um.apply(request)(ec, mat)
          .map { x =>
            PublicContextWithModel(request, x)
          }
    }
  }

  def requestContextUnmarshaller(implicit mat: Materializer): FromRequestUnmarshaller[PublicContext] = {
    Unmarshaller.strict[HttpRequest, PublicContext] { request =>
      PublicContext(request)
    }
  }
}
