package com.alexitc.playsonify.controllers

import javax.inject.Inject

import com.alexitc.playsonify.common._
import com.alexitc.playsonify.models.PublicRequestContext
import org.scalactic.{Bad, Good, Many}

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

  def getErrors() = publicNoInput[CustomUser] { context: PublicRequestContext =>
    val result = Bad(Many(CustomErrorMapper.InputError, CustomErrorMapper.DuplicateError))
    Future.successful(result)
  }

  def getException(exception: Exception) = publicNoInput[CustomUser] { context: PublicRequestContext =>
    Future.failed(exception)
  }
}
