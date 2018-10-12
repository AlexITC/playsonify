package com.alexitc.playsonify.play.controllers

import javax.inject.Inject

import com.alexitc.playsonify.play.RequestContext
import com.alexitc.playsonify.play.common.{CustomControllerComponents, CustomError, CustomJsonController, CustomModel}
import org.scalactic.{Bad, Good, Many}

import scala.concurrent.Future

class PublicWithInputController @Inject() (cc: CustomControllerComponents) extends CustomJsonController(cc) {

  import RequestContext._

  def getModel() = publicWithInput { context: PublicContextWithModel[CustomModel] =>
    Future.successful(Good(context.model))
  }

  def getCustomStatus() = publicWithInput(Created) { context: PublicContextWithModel[CustomModel] =>
    Future.successful(Good(context.model))
  }

  def getErrors() = publicWithInput[CustomModel, CustomModel] { context: PublicContextWithModel[CustomModel] =>
    val result = Bad(Many(CustomError.InputError, CustomError.DuplicateError))
    Future.successful(result)
  }

  def getException(exception: Exception) = publicWithInput[CustomModel, CustomModel] { context: PublicContextWithModel[CustomModel] =>
    Future.failed(exception)
  }
}
