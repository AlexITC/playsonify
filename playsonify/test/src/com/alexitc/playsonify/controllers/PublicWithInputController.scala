package com.alexitc.playsonify.controllers

import javax.inject.Inject

import com.alexitc.playsonify.common._
import com.alexitc.playsonify.models.PublicContextWithModel
import org.scalactic.{Bad, Good, Many}
import play.api.i18n.Lang

import scala.concurrent.Future

class PublicWithInputController @Inject() (cc: CustomControllerComponents) extends CustomJsonController(cc) {
  def getModel() = publicWithInput { context: PublicContextWithModel[CustomModel, Lang] =>
    Future.successful(Good(context.model))
  }

  def getCustomStatus() = publicWithInput(Created) { context: PublicContextWithModel[CustomModel, Lang] =>
    Future.successful(Good(context.model))
  }

  def getErrors() = publicWithInput[CustomModel, CustomModel] { context: PublicContextWithModel[CustomModel, Lang] =>
    val result = Bad(Many(CustomError.InputError, CustomError.DuplicateError))
    Future.successful(result)
  }

  def getException(exception: Exception) = publicWithInput[CustomModel, CustomModel] { context: PublicContextWithModel[CustomModel, Lang] =>
    Future.failed(exception)
  }
}
