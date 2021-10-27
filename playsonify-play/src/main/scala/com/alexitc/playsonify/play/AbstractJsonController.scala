package com.alexitc.playsonify.play

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
abstract class AbstractJsonController[+A] (
    components: JsonControllerComponents[A])
    extends MessagesBaseController {

  class Context(val request: MessagesRequest[JsValue], val lang: Lang)

  object Context {

    trait HasModel[+T] { def model: T }

    trait Authenticated { def auth: A }
  }

  import Context._

  override protected val controllerComponents: MessagesControllerComponents = components.messagesControllerComponents

  protected implicit val ec = components.executionContext

  /**
   * Override this and decide what to do in case of server errors.
   *
   * For example, log the error with the id, handle metrics, etc.
   *
   * @param error the error that occurred.
   */
  protected def onServerError(error: ServerError): Unit

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
   * The model [[R]] is wrapped in a [[Context]].
   *
   * Note: The request will not be authenticated.
   *
   * Example:
   * {{{
   *   import Context._
   *
   *   def createUser = publicInput(Created) { context: HasModel[CreateUserModel] =>
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
  def publicInput[R: Reads, M](
      successStatus: Status)(
      block: Context with HasModel[R] => FutureApplicationResult[M])(
      implicit tjs: Writes[M]): Action[JsValue] = Action.async(parse.json) { request =>

    val result = for {
      input <- validate[R](request.body).toFutureOr
      lang = messagesApi.preferred(request).lang
      context = new Context(request, lang) with HasModel[R] {
        override def model: R = input
      }
      output <- block(context).toFutureOr
    } yield output

    val lang = messagesApi.preferred(request).lang
    renderResult(successStatus, result.toFuture)(lang, tjs)
  }

  /**
   * Sets Ok as the default successStatus.
   *
   * Example:
   * {{{
   *   import Context._
   *
   *   def login = publicInput { context: HasModel[LoginModel] =>
   *     ...
   *   }
   * }}}
   *
   * Where there is an implicit deserializer for the LoginModel class, in case of a successful result,
   * the HTTP status Ok (200) will be returned.
   */
  def publicInput[R: Reads, M](
      block: Context with HasModel[R] => FutureApplicationResult[M])(
      implicit tjs: Writes[M]) = {

    publicInput[R, M](Ok)(block)
  }

  /**
   * Execute an asynchronous action that doesn't need an input model
   * and returns the model [[M]] on success.
   *
   * Note: The request will not be authenticated.
   *
   * Example:
   * {{{
   *   def verifyEmail(token: String) = public { context: Context =>
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
  def public[M](
      successStatus: Status)(
      block: Context => FutureApplicationResult[M])(
      implicit tjs: Writes[M]) = Action.async(EmptyJsonParser) { request =>

    val lang = messagesApi.preferred(request).lang
    val context = new Context(request, lang)
    val result = block(context)
    renderResult(successStatus, result)(lang, tjs)
  }

  /**
   * Sets a default successStatus.
   *
   * Example:
   * {{{
   *   def verifyEmail(token: String) = public(Created) { context: Context =>
   *     ...
   *   }
   * }}}
   *
   * In case of a successful result, the HTTP status Created (201) will be returned.
   */
  def public[M](
      block: Context => FutureApplicationResult[M])(
      implicit tjs: Writes[M]): Action[JsValue] = {

    public[M](Ok)(block)
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
   *   import Context._
   *
   *   def setPreferences = authenticatedInput(Ok) { context: HasModel[SetUserPreferencesModel] with Authenticated =>
   *     ...
   *   }
   * }}}
   *
   * Where there is an implicit deserializer for the SetUserPreferencesModel class.
   *
   * @param successStatus the http status for a successful response
   * @param block the block to execute
   * @param tjs the serializer for [[M]]
   * @tparam R the input model type
   * @tparam M the output model type
   */
  def authenticatedInput[R: Reads, M](
      successStatus: Status)(
      block: Context with Authenticated with HasModel[R] => FutureApplicationResult[M])(
      implicit tjs: Writes[M]) = Action.async(parse.json) { request =>

    val lang = messagesApi.preferred(request).lang
    val result = for {
      input <- validate[R](request.body).toFutureOr
      authValue <- components.authenticatorService.authenticate(request).toFutureOr
      lang = messagesApi.preferred(request).lang
      context = new Context(request, lang) with HasModel[R] with Authenticated {
        override def model: R = input

        override def auth: A = authValue
      }
      output <- block(context).toFutureOr
    } yield output

    renderResult(successStatus, result.toFuture)(lang, tjs)
  }

  /**
   * Sets Ok as the default successStatus.
   *
   * Note: The request will be authenticated using your custom [[AbstractAuthenticatorService]].
   *
   * Example:
   * {{{
   *   import Context._
   *
   *   def setPreferences = authenticatedInput { context: HasModel[SetUserPreferencesModel] with Authenticated =>
   *     ...
   *   }
   * }}}
   *
   * Where there is an implicit deserializer for the SetUserPreferencesModel class.
   */
  def authenticatedInput[R: Reads, M](
      block: Context with Authenticated with HasModel[R] => FutureApplicationResult[M])(
      implicit tjs: Writes[M]): Action[JsValue] = {

    authenticatedInput[R, M](Ok)(block)
  }

  /**
   * Execute an asynchronous action that doesn't need an input model
   * and returns the model [[M]] on success.
   *
   * Note: The request will be authenticated using your custom [[AbstractAuthenticatorService]].
   *
   * Example:
   * {{{
   *   import Context._
   *
   *   def whoAmI() = authenticated { context: Authenticated =>
   *     ...
   *   }
   * }}}
   *
   * @param successStatus the http status for a successful response
   * @param block the block to execute
   * @param tjs the serializer for [[M]]
   * @tparam M the output model type
   */
  def authenticated[M](
      successStatus: Status)(
      block: Context with Authenticated => FutureApplicationResult[M])(
      implicit tjs: Writes[M]): Action[JsValue] = Action.async(EmptyJsonParser) { request =>

    val lang = messagesApi.preferred(request).lang
    val result = for {
      authValue <- components.authenticatorService.authenticate(request).toFutureOr
      lang = messagesApi.preferred(request).lang
      context = new Context(request, lang) with Authenticated {
        override def auth: A = authValue
      }
      output <- block(context).toFutureOr
    } yield output

    renderResult(successStatus, result.toFuture)(lang, tjs)
  }

  /**
   * Sets Ok as the default successStatus.
   *
   * Note: The request will be authenticated using your custom [[AbstractAuthenticatorService]].
   *
   * Example:
   * {{{
   *   def whoAmI() = authenticated { context: Authenticated =>
   *     ...
   *   }
   * }}}
   */
  def authenticated[M](
      block: Context with Authenticated => FutureApplicationResult[M])(
      implicit tjs: Writes[M]) = {

    authenticated[M](Ok)(block)
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

  private def renderResult[M](
      successStatus: Status,
      response: FutureApplicationResult[M])(
      implicit lang: Lang,
      tjs: Writes[M]): Future[Result] = {

    response.map {
      case Good(value) =>
        renderSuccessfulResult(successStatus, value)(tjs)

      case Bad(errors) =>
        val errorId = ErrorId.create
        val status = getResultStatus(errors)
        val json = renderErrors(errors)

        logServerErrors(errorId, errors)
        status(json)
    }.recover {
      case NonFatal(ex) =>
        val errorId = ErrorId.create
        val error = WrappedExceptionError(errorId, ex)
        val errors = Every(error)
        val json = renderErrors(errors)
        val status = getResultStatus(errors)

        logServerErrors(errorId, errors)
        status(json)
    }
  }

  private def renderSuccessfulResult[M](successStatus: Status, model: M)(implicit tjs: Writes[M]) = {
    val json = Json.toJson(model)
    successStatus.apply(json)
  }

  private def logServerErrors(errorId: ErrorId, errors: ApplicationErrors): Unit = {
    errors
        .collect { case e: ServerError => e }
        .foreach(onServerError)
  }

  // detect response status based on the first error
  private def getResultStatus(errors: ApplicationErrors): Results.Status = errors.head match {
    case _: InputValidationError => Results.BadRequest
    case _: ConflictError => Results.Conflict
    case _: NotFoundError => Results.NotFound
    case _: AuthenticationError => Results.Unauthorized
    case _: ServerError => Results.InternalServerError
  }

  private def renderErrors(errors: ApplicationErrors)(implicit lang: Lang) = {
    val jsonErrorList = errors.toList
        .flatMap { error =>
          error.toPublicErrorList(components.i18nService)
        }
        .map(components.publicErrorRenderer.renderPublicError)

    Json.obj("errors" -> jsonErrorList)
  }
}
