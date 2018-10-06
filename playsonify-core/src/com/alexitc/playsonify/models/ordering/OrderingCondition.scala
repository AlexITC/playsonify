package com.alexitc.playsonify.models.ordering

sealed trait OrderingCondition

object OrderingCondition {

  case object AscendingOrder extends OrderingCondition
  case object DescendingOrder extends OrderingCondition
}
