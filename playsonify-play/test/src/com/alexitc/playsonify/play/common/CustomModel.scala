package com.alexitc.playsonify.play.common

import play.api.libs.json.{Format, Json}

case class CustomModel(int: Int, string: String)

object CustomModel {
  implicit val format: Format[CustomModel] = Json.format[CustomModel]
}
