package com.alexitc.playsonify.play.codecs

import com.alexitc.playsonify.models.pagination.{Count, Limit, Offset, PaginatedResult}
import com.alexitc.playsonify.models.{WrappedInt, WrappedLong, WrappedString}
import org.scalactic.Every
import play.api.libs.json._

trait DefaultCodecs {

  implicit val countWrites: Writes[Count] = Writes[Count] { count => JsNumber(count.int) }

  implicit val limitWrites: Writes[Limit] = Writes[Limit] { limit => JsNumber(limit.int) }

  implicit val offsetWrites: Writes[Offset] = Writes[Offset] { offset => JsNumber(offset.int) }

  implicit def paginatedResultWrites[T](implicit writesT: Writes[T]): Writes[PaginatedResult[T]] = OWrites[PaginatedResult[T]] { result =>
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

  implicit def everyReads[T](implicit readsT: Reads[T]): Reads[Every[T]] = Reads[Every[T]] { json =>
    json
        .validate[List[T]]
        .flatMap { list =>
          Every.from(list)
              .map(JsSuccess.apply(_))
              .getOrElse {
                JsError.apply("A non-empty list is expected")
              }
        }
  }

  implicit def everyWrites[T](implicit writesT: Writes[T]): Writes[Every[T]] = Writes[Every[T]] { o =>
    Json.toJson(o.toList)
  }
}

object DefaultCodecs extends DefaultCodecs
