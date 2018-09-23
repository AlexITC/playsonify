package com.alexitc.playsonify.controllers

import javax.inject.Inject

import com.alexitc.playsonify.common._
import com.alexitc.playsonify.models.PublicContext
import org.scalactic.{Bad, Good, Many}
import play.api.i18n.Lang

import scala.concurrent.Future

class PublicNoInputController @Inject() (cc: CustomControllerComponents) extends CustomJsonController(cc) {

  def getModel(int: Int, string: String) = publicNoInput { context =>
    val result = CustomModel(int, string)
    Future.successful(Good(result))
  }

  def getCustomStatus() = publicNoInput(Created) { context =>
    val result = CustomModel(0, "no")
    Future.successful(Good(result))
  }

  def getErrors() = publicNoInput[CustomUser] { context: PublicContext[Lang] =>
    val result = Bad(Many(CustomError.InputError, CustomError.DuplicateError))
    Future.successful(result)
  }

  def getException(exception: Exception) = publicNoInput[CustomUser] { context: PublicContext[Lang] =>
    Future.failed(exception)
  }
}
