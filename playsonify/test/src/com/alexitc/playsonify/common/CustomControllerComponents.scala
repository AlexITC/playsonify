package com.alexitc.playsonify.common

import javax.inject.Inject

import com.alexitc.playsonify.{JsonControllerComponents, PublicErrorRenderer}
import play.api.mvc.MessagesControllerComponents

import scala.concurrent.ExecutionContext

class CustomControllerComponents @Inject()(
    override val messagesControllerComponents: MessagesControllerComponents,
    override val executionContext: ExecutionContext,
    override val publicErrorRenderer: PublicErrorRenderer,
    override val authenticatorService: CustomAuthenticator)
    extends JsonControllerComponents[CustomUser]
