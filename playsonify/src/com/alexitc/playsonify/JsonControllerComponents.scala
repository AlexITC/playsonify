package com.alexitc.playsonify

import play.api.mvc.MessagesControllerComponents

import scala.concurrent.ExecutionContext

/**
 * The components that define the behavior of your controllers.
 *
 * @tparam A the type used on successful authenticated requests.
 */
trait JsonControllerComponents[A] {

  /**
   * This is internally used by play, you probably want to inject it.
   */
  def messagesControllerComponents: MessagesControllerComponents

  /**
   * The execution context where all light-weight operations are going to run,
   * you probably want to inject the default [[ExecutionContext]].
   */
  def executionContext: ExecutionContext

  /**
   * This component maps a [[com.alexitc.playsonify.models.PublicError]] to a
   * [[play.api.libs.json.JsValue]], this is where the errors are serialized to
   * JSON, you can implement a custom [[PublicErrorRenderer]] to override the default
   * behavior or to define a new error response format.
   */
  def publicErrorRenderer: PublicErrorRenderer

  /**
   * You are required to implement this component.
   *
   * This is where you need to map your
   * application specific errors to a general [[com.alexitc.playsonify.models.PublicError]].
   */
  def applicationErrorMapper: ApplicationErrorMapper

  /**
   * You are required to implement this component.
   *
   * This is where you decide how to authenticate a request and what is the result of a
   * successful authentication, typically it would be something like a User or UserId.
   */
  def authenticatorService: AbstractAuthenticatorService[A]
}
