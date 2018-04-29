package com.alexitc.playsonify.models

import play.api.libs.json.{JsString, Writes}

trait WrappedString extends Any {

  def string: String

  override def toString: String = string
}

object WrappedString {

  implicit val writes: Writes[WrappedString] = {
    Writes[WrappedString] { wrapped => JsString(wrapped.string) }
  }
}
