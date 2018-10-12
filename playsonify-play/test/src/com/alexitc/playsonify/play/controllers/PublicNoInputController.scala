package com.alexitc.playsonify.play.controllers

import javax.inject.Inject

import com.alexitc.playsonify.play.RequestContext
import com.alexitc.playsonify.play.common._
import org.scalactic.{Bad, Good, Many}

import scala.concurrent.Future

class PublicNoInputController @Inject() (cc: CustomControllerComponents) extends CustomJsonController(cc) {

  import RequestContext._

  def getModel(int: Int, string: String) = publicNoInput { context =>
    val result = CustomModel(int, string)
    Future.successful(Good(result))
  }

  def getCustomStatus() = publicNoInput(Created) { context =>
    val result = CustomModel(0, "no")
    Future.successful(Good(result))
  }

  def getErrors() = publicNoInput[CustomUser] { context: PublicContext =>
    val result = Bad(Many(CustomError.InputError, CustomError.DuplicateError))
    Future.successful(result)
  }

  def getException(exception: Exception) = publicNoInput[CustomUser] { context: PublicContext =>
    Future.failed(exception)
  }
}
