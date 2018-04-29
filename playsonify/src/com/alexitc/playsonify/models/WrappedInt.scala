package com.alexitc.playsonify.models

import play.api.libs.json.{JsNumber, Writes}

trait WrappedInt extends Any {

  def int: Int

  override def toString: String = int.toString
}

object WrappedInt {

  implicit val writes: Writes[WrappedInt] = {
    Writes[WrappedInt] { wrapped => JsNumber(wrapped.int) }
  }
}
