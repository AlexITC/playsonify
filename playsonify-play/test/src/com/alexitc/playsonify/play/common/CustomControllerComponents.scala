package com.alexitc.playsonify.play.common

import javax.inject.Inject

import com.alexitc.playsonify.play.{I18nPlayService, JsonControllerComponents, PublicErrorRenderer}
import play.api.i18n.Lang
import play.api.mvc.MessagesControllerComponents

import scala.concurrent.ExecutionContext

class CustomControllerComponents @Inject()(
    override val messagesControllerComponents: MessagesControllerComponents,
    override val i18nService: I18nPlayService,
    override val executionContext: ExecutionContext,
    override val publicErrorRenderer: PublicErrorRenderer,
    override val authenticatorService: CustomAuthenticator)
    extends JsonControllerComponents[CustomUser, Lang]
