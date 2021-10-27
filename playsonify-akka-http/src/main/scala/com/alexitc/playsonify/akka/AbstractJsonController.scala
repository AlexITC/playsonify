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
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future
import scala.util.{Failure, Success}

abstract class AbstractJsonController[+A](
    components: JsonControllerComponents[A])
    extends Directives
    with PlayJsonSupport {

  class Context(val request: HttpRequest)

  object Context {

    trait HasModel[+T] { def model: T }

    trait Authenticated { def auth: A }
  }

  import Context._

  // Required to complete the calls to render messages
  private implicit val dummyLang: String = ""

  protected def onServerError(error: ServerError, id: ErrorId): Unit

  protected def withGlobalHandler: Directive0 = handleRejections(rejectionHandler)

  protected val rejectionHandler = {
    def notFoundHandler = {
      val error = RouteNotFoundError
      val badResult = Bad(error).accumulating
      val result: FutureApplicationResult[String] = Future.successful(badResult)
      renderResult(StatusCodes.NotFound, result)
    }

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
        }.handle { case MethodRejection(_) =>
          notFoundHandler
        }
        .handleNotFound {
          notFoundHandler
        }
        .result()
  }

  def publicInput[I, O](successCode: StatusCode)(
      f: Context with HasModel[I] => FutureApplicationResult[O])(
      implicit um: FromRequestUnmarshaller[I],
      rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    handleRejections(rejectionHandler) {
      val directive = entity(as(requestContextWithModelUnmarshaller))
      val g = renderResult(successCode, _: FutureApplicationResult[O])(rm)
      directive.apply(f andThen g)
    }
  }

  def publicInput[I, O](
      f: Context with HasModel[I] => FutureApplicationResult[O])(
      implicit um: FromRequestUnmarshaller[I],
      rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    publicInput[I, O](StatusCodes.OK)(f)
  }

  def public[O](
      successCode: StatusCode)(
      f: Context => FutureApplicationResult[O])(
      implicit rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    handleRejections(rejectionHandler) {
      val directive = entity(as(requestContextUnmarshaller))
      val g = renderResult(successCode, _: FutureApplicationResult[O])(rm)
      directive.apply(f andThen g)
    }
  }

  def public[O](
      f: Context => FutureApplicationResult[O])(
      implicit rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    public[O](StatusCodes.OK)(f)
  }

  def authenticated[O](
      successCode: StatusCode)(
      f: Context with Authenticated => FutureApplicationResult[O])(
      implicit rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    handleRejections(rejectionHandler) {
      // TODO: unmarshall request after authentication
      val directive = entity(as(requestContextUnmarshaller))
      directive { ctx =>
        extractExecutionContext { implicit ec =>
          val result = for {
            authObj <- components.authenticatorService.authenticate(ctx.request).toFutureOr
            authCtx = new Context(ctx.request) with Authenticated {

              override val auth: A = authObj
            }
            output <- f(authCtx).toFutureOr
          } yield output

          renderResult(successCode, result.toFuture)
      }
    }
  }
  }

  def authenticated[O](
      f: Context with Authenticated => FutureApplicationResult[O])(
      implicit rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    authenticated[O](StatusCodes.OK)(f)
  }

  def authenticatedInput[I, O](successCode: StatusCode)(
      f: Context with Authenticated with HasModel[I] => FutureApplicationResult[O])(
      implicit um: FromRequestUnmarshaller[I],
      rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    handleRejections(rejectionHandler) {
      // TODO: unmarshall request after authentication
      val directive = entity(as(requestContextWithModelUnmarshaller))
      directive { ctx =>
        extractExecutionContext { implicit ec =>
          val result = for {
            authObj <- components.authenticatorService.authenticate(ctx.request).toFutureOr
            authCtx = new Context(ctx.request) with Authenticated with HasModel[I] {

              override def auth: A = authObj

              override def model: I = ctx.model
            }
            output <- f(authCtx).toFutureOr
          } yield output

          renderResult(successCode, result.toFuture)
        }
      }
    }
  }

  def authenticatedInput[I, O](
      f: Context with Authenticated with HasModel[I] => FutureApplicationResult[O])(
      implicit um: FromRequestUnmarshaller[I],
      rm: ToResponseMarshaller[O],
      mat: Materializer): Route = {

    authenticatedInput[I, O](StatusCodes.OK)(f)
  }

  private def logServerErrors(errorId: ErrorId, errors: ApplicationErrors): Unit = {
    errors
        .collect { case e: ServerError => e }
        .foreach { onServerError(_, errorId) }
  }

  private def renderResult[T](
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
  private def getResultStatus(errors: ApplicationErrors): StatusCode = errors.head match {
    case _: InputValidationError => StatusCodes.BadRequest
    case _: ConflictError => StatusCodes.Conflict
    case _: NotFoundError => StatusCodes.NotFound
    case _: AuthenticationError => StatusCodes.Unauthorized
    case _: ServerError => StatusCodes.InternalServerError
  }

  private def renderErrors(errors: ApplicationErrors): JsValue = {
    val jsonErrorList = errors.toList
        .flatMap { error =>
          error.toPublicErrorList(components.i18nService)
        }
        .map(components.publicErrorRenderer.renderPublicError)

    Json.obj("errors" -> jsonErrorList)
  }

  private def requestContextWithModelUnmarshaller[T](
      implicit um: FromRequestUnmarshaller[T],
      mat: Materializer): FromRequestUnmarshaller[Context with HasModel[T]] = {

    Unmarshaller.apply[HttpRequest, Context with HasModel[T]] { implicit ec => request =>
      um.apply(request)(ec, mat)
          .map { x =>
            new Context(request) with HasModel[T] {
              override def model: T = x
            }
          }
    }
  }

  private def requestContextUnmarshaller(implicit mat: Materializer) = {
    Unmarshaller.strict[HttpRequest, Context] { request =>
      new Context(request)
    }
  }
}
