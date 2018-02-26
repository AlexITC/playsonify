package controllers.common

import javax.inject.Inject

import com.alexitc.example.{DummyAuthenticatorService, MyApplicationErrorMapper}
import com.alexitc.playsonify.{JsonControllerComponents, PublicErrorRenderer}
import play.api.mvc.MessagesControllerComponents

import scala.concurrent.ExecutionContext

class MyJsonControllerComponents @Inject() (
    override val messagesControllerComponents: MessagesControllerComponents,
    override val executionContext: ExecutionContext,
    override val publicErrorRenderer: PublicErrorRenderer,
    override val applicationErrorMapper: MyApplicationErrorMapper,
    override val authenticatorService: DummyAuthenticatorService)
    extends JsonControllerComponents[Int]
