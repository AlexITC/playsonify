package com.alexitc.playsonify.controllers

import javax.inject.Inject

import com.alexitc.playsonify.common.{CustomControllerComponents, CustomErrorMapper, CustomJsonController, CustomModel}
import com.alexitc.playsonify.models.PublicContextWithModel
import org.scalactic.{Bad, Good, Many}

import scala.concurrent.Future

class PublicWithInputController @Inject() (cc: CustomControllerComponents) extends CustomJsonController(cc) {
  def getModel() = publicWithInput { context: PublicContextWithModel[CustomModel] =>
    Future.successful(Good(context.model))
  }

  def getCustomStatus() = publicWithInput(Created) { context: PublicContextWithModel[CustomModel] =>
    Future.successful(Good(context.model))
  }

  def getErrors() = publicWithInput[CustomModel, CustomModel] { context: PublicContextWithModel[CustomModel] =>
    val result = Bad(Many(CustomErrorMapper.InputError, CustomErrorMapper.DuplicateError))
    Future.successful(result)
  }

  def getException(exception: Exception) = publicWithInput[CustomModel, CustomModel] { context: PublicContextWithModel[CustomModel] =>
    Future.failed(exception)
  }
}
