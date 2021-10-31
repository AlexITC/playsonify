package com.alexitc.playsonify.sql

import com.alexitc.playsonify.models.ordering.{FieldOrdering, OrderingCondition}
import org.scalatest.matchers.must.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class FieldOrderingSQLInterpreterSpec extends AnyWordSpec {

  import FieldOrderingSQLInterpreterSpec._

  val interpreter = new FieldOrderingSQLInterpreter

  "toOrderByClause" should {
    "set the ascending order" in {
      val ordering = FieldOrdering[PersonField](PersonField.Id, OrderingCondition.AscendingOrder)
      val result = interpreter.toOrderByClause(ordering)

      result must be("ORDER BY Id ASC")
    }

    "set the descending order" in {
      val ordering = FieldOrdering[PersonField](PersonField.Id, OrderingCondition.DescendingOrder)
      val result = interpreter.toOrderByClause(ordering)

      result must be("ORDER BY Id DESC")
    }

    "break ties" in {
      val ordering = FieldOrdering[PersonField](PersonField.Country, OrderingCondition.AscendingOrder)
      val result = interpreter.toOrderByClause(ordering)

      result must be("ORDER BY Country ASC, Id")
    }
  }
}

object FieldOrderingSQLInterpreterSpec {
  sealed trait PersonField
  object PersonField {
    final case object Id extends PersonField
    final case object Country extends PersonField
  }

  implicit val personColumnNameResolver: ColumnNameResolver[PersonField] = new ColumnNameResolver[PersonField] {
    override def getColumnName(field: PersonField): String = field.toString

    override def getUniqueColumnName: String = PersonField.Id.toString
  }
}
