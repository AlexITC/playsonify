package com.alexitc.playsonify

import com.alexitc.playsonify.core.FutureOr.Implicits.{FutureOps, OrOps}
import com.alexitc.playsonify.core.{ApplicationErrors, ApplicationResult, FutureApplicationResult}
import com.alexitc.playsonify.models._
import org.scalactic.{Bad, Every, Good}
import play.api.i18n.Lang
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Abstract Controller designed to process actions that expect an input model
 * and computes an output model.
 *
 * The controller handles the json serialization and deserialization as well
 * as the error responses and http status codes.
 *
 * A common way for using this class is like:
 * {{{
 *   class MyController @Inject() (components: MyJsonControllerComponents)
 *       extends MyJsonController(components) {
 *
 *     ...
 *   }
 * }}}
 *
 * Where MyJsonControllerComponents is a custom implementation for [[JsonControllerComponents]]
 * and MyJsonController is the custom implementation for [[AbstractJsonController]].
 *
 * @param components the components used by the logic of this JsonController.
 * @tparam A the value type for an authenticated request, like User or UserId.
 */
abstract class AbstractJsonController[A] (
    components: JsonControllerComponents[A])
    extends MessagesBaseController {

  override protected val controllerComponents: MessagesControllerComponents = components.messagesControllerComponents

  protected implicit val ec = components.executionContext

  /**
   * Override this and decide what to do in case of server errors.
   *
   * For example, log the error with the id, handle metrics, etc.
   *
   * @param error the error that occurred.
   * @param errorId the unique identifier for the error.
   */
  protected def onServerError(error: ServerError, errorId: ErrorId): Unit

  /**
   * Ignores the body returning an empty json.
   *
   * Useful for using methods that doesn't require input.
   */
  private val EmptyJsonParser = parse.ignore(Json.toJson(JsObject.empty))

  /**
   * Execute an asynchronous action that receives the model [[R]]
   * and returns the model [[M]] on success.
   *
   * The model [[R]] is wrapped in a [[RequestContext]].
   *
   * Note: The request will not be authenticated.
   *
   * Example:
   * {{{
   *   def createUser = publicWithInput(Created) { context: PublicContextWithModel[CreateUserModel] =>
   *     ...
   *   }
   * }}}
   *
   * Where there is an implicit deserializer for the CreateUserModel class, in case of a successful result,
   * the HTTP status Created (201) will be returned.
   *
   * @param successStatus the http status for a successful response
   * @param block the block to execute
   * @param tjs the serializer for [[M]]
   * @tparam R the input model type
   * @tparam M the output model type
   */
  def publicWithInput[R: Reads, M](
      successStatus: Status)(
      block: PublicContextWithModel[R] => FutureApplicationResult[M])(
      implicit tjs: Writes[M]): Action[JsValue] = Action.async(parse.json) { request =>

    val result = for {
      input <- validate[R](request.body).toFutureOr
      context = PublicContextWithModel(input, messagesApi.preferred(request).lang)
      output <- block(context).toFutureOr
    } yield output

    val lang = messagesApi.preferred(request).lang
    toResult(successStatus, result.toFuture)(lang, tjs)
  }

  /**
   * Sets Ok as the default successStatus.
   *
   * Example:
   * {{{
   *   def login = publicWithInput { context: PublicContextWithModel[LoginModel] =>
   *     ...
   *   }
   * }}}
   *
   * Where there is an implicit deserializer for the LoginModel class, in case of a successful result,
   * the HTTP status Ok (200) will be returned.
   */
  def publicWithInput[R: Reads, M](
      block: PublicContextWithModel[R] => FutureApplicationResult[M])(
      implicit tjs: Writes[M]): Action[JsValue] = {

    publicWithInput[R, M](Ok)(block)
  }

  /**
   * Execute an asynchronous action that doesn't need an input model
   * and returns the model [[M]] on success.
   *
   * Note: The request will not be authenticated.
   *
   * Example:
   * {{{
   *   def verifyEmail(token: String) = publicNoInput { context: PublicContext =>
   *     ...
   *   }
   * }}}
   *
   * In case of a successful result, the HTTP status Ok (200) will be returned.
   *
   * @param successStatus the http status for a successful response
   * @param block the block to execute
   * @param tjs the serializer for [[M]]
   * @tparam M the output model type
   */
  def publicNoInput[M](
      successStatus: Status)(
      block: PublicContext => FutureApplicationResult[M])(
      implicit tjs: Writes[M]): Action[JsValue] = Action.async(EmptyJsonParser) { request =>

    val context = PublicContext(messagesApi.preferred(request).lang)
    val result = block(context)
    val lang = messagesApi.preferred(request).lang
    toResult(successStatus, result)(lang, tjs)
  }

  /**
   * Sets a default successStatus.
   *
   * Example:
   * {{{
   *   def verifyEmail(token: String) = publicNoInput(Created) { context: PublicContext =>
   *     ...
   *   }
   * }}}
   *
   * In case of a successful result, the HTTP status Created (201) will be returned.
   */
  def publicNoInput[M](
      block: PublicContext => FutureApplicationResult[M])(
      implicit tjs: Writes[M]): Action[JsValue] = {

    publicNoInput[M](Ok)(block)
  }

  /**
   * Execute an asynchronous action that receives the model [[R]]
   * and produces the model [[M]] on success, the http status in
   * case of a successful result will be taken from successStatus param.
   *
   * Note: The request will be authenticated using your custom [[AbstractAuthenticatorService]].
   *
   * Example:
   * {{{
   *   def setPreferences = authenticatedWithInput(Ok) { context: AuthenticatedContextWithModel[UserId, SetUserPreferencesModel] =>
   *     ...
   *   }
   * }}}
   *
   * Where UserId is what your custom [[AbstractAuthenticatorService]] returns on authenticated
   * requests, also, there is an implicit deserializer for the SetUserPreferencesModel class.
   *
   * @param successStatus the http status for a successful response
   * @param block the block to execute
   * @param tjs the serializer for [[M]]
   * @tparam R the input model type
   * @tparam M the output model type
   */
  def authenticatedWithInput[R: Reads, M](
      successStatus: Status)(
      block: AuthenticatedContextWithModel[A, R] => FutureApplicationResult[M])(
      implicit tjs: Writes[M]): Action[JsValue] = Action.async(parse.json) { request =>

    val lang = messagesApi.preferred(request).lang
    val result = for {
      input <- validate[R](request.body).toFutureOr
      authValue <- components.authenticatorService.authenticate(request).toFutureOr
      context = AuthenticatedContextWithModel(authValue, input, lang)
      output <- block(context).toFutureOr
    } yield output

    toResult(successStatus, result.toFuture)(lang, tjs)
  }

  /**
   * Sets Ok as the default successStatus.
   *
   * Note: The request will be authenticated using your custom [[AbstractAuthenticatorService]].
   *
   * Example:
   * {{{
   *   def setPreferences = authenticatedWithInput { context: AuthenticatedContextWithModel[UserId, SetUserPreferencesModel] =>
   *     ...
   *   }
   * }}}
   *
   * Where UserId is what your custom [[AbstractAuthenticatorService]] returns on authenticated
   * requests, also, there is an implicit deserializer for the SetUserPreferencesModel class.
   */
  def authenticatedWithInput[R: Reads, M](
      block: AuthenticatedContextWithModel[A, R] => FutureApplicationResult[M])(
      implicit tjs: Writes[M]): Action[JsValue] = {

    authenticatedWithInput[R, M](Ok)(block)
  }

  /**
   * Execute an asynchronous action that doesn't need an input model
   * and returns the model [[M]] on success.
   *
   * Note: The request will be authenticated using your custom [[AbstractAuthenticatorService]].
   *
   * Example:
   * {{{
   *   def whoAmI() = authenticatedNoInput { context: AuthenticatedContext[UserId] =>
   *     ...
   *   }
   * }}}
   *
   * Where UserId is what your custom [[AbstractAuthenticatorService]] returns on authenticated
   * requests.
   *
   * @param successStatus the http status for a successful response
   * @param block the block to execute
   * @param tjs the serializer for [[M]]
   * @tparam M the output model type
   */
  def authenticatedNoInput[M](
      successStatus: Status)(
      block: AuthenticatedContext[A] => FutureApplicationResult[M])(
      implicit tjs: Writes[M]): Action[JsValue] = Action.async(EmptyJsonParser) { request =>

    val lang = messagesApi.preferred(request).lang
    val result = for {
      authValue <- components.authenticatorService.authenticate(request).toFutureOr
      context = AuthenticatedContext(authValue, lang)
      output <- block(context).toFutureOr
    } yield output

    toResult(successStatus, result.toFuture)(lang, tjs)
  }

  /**
   * Sets Ok as the default successStatus.
   *
   * Note: The request will be authenticated using your custom [[AbstractAuthenticatorService]].
   *
   * Example:
   * {{{
   *   def whoAmI() = authenticatedNoInput { context: AuthenticatedContext[UserId] =>
   *     ...
   *   }
   * }}}
   *
   * Where UserId is what your custom [[AbstractAuthenticatorService]] returns on authenticated
   * requests.
   */
  def authenticatedNoInput[M](
      block: AuthenticatedContext[A] => FutureApplicationResult[M])(
      implicit tjs: Writes[M]): Action[JsValue] = {

    authenticatedNoInput[M](Ok)(block)
  }

  private def validate[R: Reads](json: JsValue): ApplicationResult[R] = {
    json.validate[R].fold(
      invalid => {
        val errorList = invalid.map { case (path, errors) =>
          JsonFieldValidationError(
            path,
            errors
                .flatMap(_.messages)
                .map(MessageKey.apply)
                .toList)
        }

        // assume that errorList is non empty
        Bad(Every(errorList.head, errorList.drop(1): _*))
      },
      valid => Good(valid)
    )
  }

  private def toResult[M](
      successStatus: Status,
      response: FutureApplicationResult[M])(
      implicit lang: Lang,
      tjs: Writes[M]): Future[Result] = {

    response.map {
      case Good(value) =>
        renderSuccessfulResult(successStatus, value)(tjs)

      case Bad(errors) =>
        renderErrors(errors)
    }.recover {
      case NonFatal(ex) =>
        val error = WrappedExceptionError(ex)
        renderErrors(Every(error))
    }
  }

  private def renderSuccessfulResult[M](successStatus: Status, model: M)(implicit tjs: Writes[M]) = {
    val json = Json.toJson(model)
    successStatus.apply(json)
  }

  private def renderErrors(errors: ApplicationErrors)(implicit lang: Lang): Result = {
    // detect response status based on the first error
    val status = errors.head match {
      case _: InputValidationError => Results.BadRequest
      case _: ConflictError => Results.Conflict
      case _: NotFoundError => Results.NotFound
      case _: AuthenticationError => Results.Unauthorized
      case _: ServerError => Results.InternalServerError
    }

    val json = errors.head match {
      case error: ServerError =>
        val errorId = ErrorId.create
        onServerError(error, errorId)
        renderPrivateError(errorId)

      case _ => renderPublicErrors(errors)
    }
    status(Json.toJson(json))
  }

  private def renderPublicErrors(errors: ApplicationErrors)(implicit lang: Lang) = {
    val jsonErrorList = errors
        .toList
        .flatMap { error => error.toPublicErrorList(components.messagesControllerComponents.messagesApi) }
        .map(components.publicErrorRenderer.renderPublicError)

    Json.obj("errors" -> jsonErrorList)
  }

  private def renderPrivateError(errorId: ErrorId) = {
    val jsonError = components.publicErrorRenderer.renderPrivateError(errorId)

    Json.obj("errors" -> List(jsonError))
  }
}
