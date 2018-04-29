package com.alexitc.playsonify.models

import play.api.libs.json.{JsNumber, Writes}

trait WrappedLong extends Any {

  def long: Long

  override def toString: String = long.toString
}

object WrappedLong {

  implicit val writes: Writes[WrappedLong] = {
    Writes[WrappedLong] { wrapped => JsNumber(wrapped.long) }
  }
}
