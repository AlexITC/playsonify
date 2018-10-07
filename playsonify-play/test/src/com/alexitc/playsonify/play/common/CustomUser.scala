package com.alexitc.playsonify.play.common

import play.api.libs.json.{Format, Json}

case class CustomUser(id: String)

object CustomUser {
  implicit val format: Format[CustomUser] = Json.format[CustomUser]
}
