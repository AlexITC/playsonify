package com.alexitc.playsonify.play.controllers

import javax.inject.Inject

import com.alexitc.playsonify.play.common.{CustomControllerComponents, CustomError, CustomJsonController, CustomModel}
import org.scalactic.{Bad, Good, Many}

import scala.concurrent.Future

class PublicWithInputController @Inject() (cc: CustomControllerComponents) extends CustomJsonController(cc) {

  import Context._

  def getModel() = publicInput { context: HasModel[CustomModel] =>
    Future.successful(Good(context.model))
  }

  def getCustomStatus() = publicInput(Created) { context: HasModel[CustomModel] =>
    Future.successful(Good(context.model))
  }

  def getErrors() = publicInput[CustomModel, CustomModel] { _: HasModel[CustomModel] =>
    val result = Bad(Many(CustomError.InputError, CustomError.DuplicateError))
    Future.successful(result)
  }

  def getException(exception: Exception) = publicInput[CustomModel, CustomModel] { _: HasModel[CustomModel] =>
    Future.failed(exception)
  }
}
