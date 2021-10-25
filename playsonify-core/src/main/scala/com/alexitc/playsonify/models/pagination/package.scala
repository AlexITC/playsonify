package com.alexitc.playsonify.models

package object pagination {

  case class Count(int: Int) extends AnyVal with WrappedInt
  case class Offset(int: Int) extends AnyVal with WrappedInt
  case class Limit(int: Int) extends AnyVal with WrappedInt

  case class PaginatedQuery(offset: Offset, limit: Limit)
  case class PaginatedResult[+T](offset: Offset, limit: Limit, total: Count, data: List[T])
}
