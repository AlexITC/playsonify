package com.alexitc.playsonify.play.controllers

import javax.inject.Inject

import com.alexitc.playsonify.play.common._
import org.scalactic.{Bad, Good, Many}

import scala.concurrent.Future

class PublicController @Inject() (cc: CustomControllerComponents) extends CustomJsonController(cc) {

  def getModel(int: Int, string: String) = public { context =>
    val result = CustomModel(int, string)
    Future.successful(Good(result))
  }

  def getCustomStatus() = public(Created) { context =>
    val result = CustomModel(0, "no")
    Future.successful(Good(result))
  }

  def getErrors() = public[CustomModel] { _: Context =>
    val result = Bad(Many(CustomError.InputError, CustomError.DuplicateError))
    Future.successful(result)
  }

  def getException(exception: Exception) = public[CustomModel] { _: Context =>
    Future.failed(exception)
  }
}
