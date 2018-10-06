package com.alexitc.playsonify.parsers

import com.alexitc.playsonify.models.ordering.{FieldOrdering, OrderingCondition, OrderingError, OrderingQuery}
import org.scalactic.{Bad, Every, Good}
import org.scalatest.{MustMatchers, WordSpec}

class FieldOrderingParserSpec extends WordSpec with MustMatchers {

  import FieldOrderingParserSpec._

  val parser = new CustomFieldParser

  "from" should {
    "parse an empty query to default ordering" in {
      val query = OrderingQuery("")
      val expected = FieldOrdering(Id, OrderingCondition.AscendingOrder)
      val result = parser.from(query)

      result mustEqual Good(expected)
    }

    "parse a field without ordering condition" in {
      val query = OrderingQuery("id")
      val expected = FieldOrdering(Id, OrderingCondition.AscendingOrder)
      val result = parser.from(query)

      result mustEqual Good(expected)
    }

    "parse a field with ordering condition" in {
      val query = OrderingQuery("name:desc")
      val expected = FieldOrdering(Name, OrderingCondition.DescendingOrder)
      val result = parser.from(query)

      result mustEqual Good(expected)
    }

    "reject unknown field" in {
      val query = OrderingQuery("age:desc")
      val expected = Bad(OrderingError.UnknownField).accumulating
      val result = parser.from(query)

      result mustEqual expected
    }

    "reject unknown ordering condition" in {
      val query = OrderingQuery("id:descending")
      val expected = Bad(OrderingError.InvalidCondition).accumulating
      val result = parser.from(query)

      result mustEqual expected
    }

    "accumulate errors on unknown field and ordering condition" in {
      val query = OrderingQuery("age:descending")
      val expected = Bad(Every(OrderingError.UnknownField, OrderingError.InvalidCondition))
      val result = parser.from(query)

      result mustEqual expected
    }

    "reject bad ordering format" in {
      val query = OrderingQuery("id:desc:x")
      val expected = Bad(OrderingError.InvalidFormat).accumulating
      val result = parser.from(query)

      result mustEqual expected
    }
  }
}

object FieldOrderingParserSpec {

  sealed abstract class CustomField(val string: String)
  case object Id extends CustomField("id")
  case object Name extends CustomField("name")

  class CustomFieldParser extends FieldOrderingParser[CustomField] {
    override protected def defaultField: CustomField = Id

    override protected def parseField(unsafeField: String): Option[CustomField] = unsafeField match {
      case Id.string => Some(Id)
      case Name.string => Some(Name)
      case _ => None
    }
  }
}
