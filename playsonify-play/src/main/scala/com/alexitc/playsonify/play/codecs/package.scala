package com.alexitc.playsonify.play

import com.alexitc.playsonify.models.pagination._
import com.alexitc.playsonify.models.{WrappedInt, WrappedLong, WrappedString}
import play.api.libs.json._

package object codecs {

  implicit val countWrites: Writes[Count] = Writes[Count] { count => JsNumber(count.int) }

  implicit val limitWrites: Writes[Limit] = Writes[Limit] { limit => JsNumber(limit.int) }

  implicit val offsetWrites: Writes[Offset] = Writes[Offset] { offset => JsNumber(offset.int) }

  implicit def writes[T](implicit writesT: Writes[T]): Writes[PaginatedResult[T]] = OWrites[PaginatedResult[T]] {
    result =>
      Json.obj(
        "offset" -> result.offset,
        "limit" -> result.limit,
        "total" -> result.total,
        "data" -> result.data
      )
  }

  implicit val wrappedIntWrites: Writes[WrappedInt] = {
    Writes[WrappedInt] { wrapped => JsNumber(wrapped.int) }
  }

  implicit val wrapperLongWrites: Writes[WrappedLong] = {
    Writes[WrappedLong] { wrapped => JsNumber(wrapped.long) }
  }

  implicit val wrappedStringWrites: Writes[WrappedString] = {
    Writes[WrappedString] { wrapped => JsString(wrapped.string) }
  }
}
