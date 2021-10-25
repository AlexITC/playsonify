package com.alexitc.playsonify.models

package object ordering {

  case class FieldOrdering[+A](field: A, orderingCondition: OrderingCondition)

  case class OrderingQuery(string: String) extends AnyVal with WrappedString
}
