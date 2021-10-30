package com.alexitc.playsonify.sql

import com.alexitc.playsonify.models.ordering.{FieldOrdering, OrderingCondition}

class FieldOrderingSQLInterpreter {

  def toOrderByClause[A](
      fieldOrdering: FieldOrdering[A]
  )(implicit columnNameResolver: ColumnNameResolver[A]): String = {
    val field = columnNameResolver.getColumnName(fieldOrdering.field)
    val condition = getCondition(fieldOrdering.orderingCondition)
    val uniqueField = columnNameResolver.getUniqueColumnName

    if (field == uniqueField) {
      s"ORDER BY $field $condition"
    } else {
      s"ORDER BY $field $condition, $uniqueField"
    }
  }

  private def getCondition(ordering: OrderingCondition) = ordering match {
    case OrderingCondition.AscendingOrder => "ASC"
    case OrderingCondition.DescendingOrder => "DESC"
  }
}
