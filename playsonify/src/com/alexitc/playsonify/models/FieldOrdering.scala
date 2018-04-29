package com.alexitc.playsonify.models

case class FieldOrdering[+A](field: A, orderingCondition: OrderingCondition)
