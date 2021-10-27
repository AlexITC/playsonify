package com.alexitc.playsonify.parsers

import com.alexitc.playsonify.core.{ApplicationErrors, ApplicationResult}
import com.alexitc.playsonify.models.ordering.{FieldOrdering, OrderingCondition, OrderingError, OrderingQuery}
import org.scalactic._

trait FieldOrderingParser[+A] {

  protected def parseField(unsafeField: String): Option[A]

  protected def defaultField: A

  protected def defaultOrderingCondition: OrderingCondition = OrderingCondition.AscendingOrder

  /**
   * Accepts values in the format field[:condition], being condition
   * an optional argument allowing the these values:
   * - asc: for ascending order.
   * - desc: for descending order.
   *
   * The empty string is also accepted returning a default ordering.
   */
  def from(orderByQuery: OrderingQuery): ApplicationResult[FieldOrdering[A]] = {
    Option(orderByQuery.string)
        .filter(_.nonEmpty)
        .map { string => from(string.split(":")) }
        .getOrElse {
          val ordering = FieldOrdering(defaultField, defaultOrderingCondition)
          Good(ordering)
        }
  }

  private def from(parts: Seq[String]): FieldOrdering[A] Or ApplicationErrors = parts match {
    case Seq(unsafeField) =>
      for {
        field <- getFieldResult(unsafeField)
      } yield FieldOrdering(field, defaultOrderingCondition)

    case Seq(unsafeField, unsafeOrderingCondition) =>
      Accumulation.withGood(
        getFieldResult(unsafeField),
        getOrderingConditionResult(unsafeOrderingCondition)) { FieldOrdering.apply }

    case _ =>
      Bad(OrderingError.InvalidFormat).accumulating
  }

  private def getFieldResult(unsafeField: String) = {
    val maybe = parseField(unsafeField)
    Or.from(maybe, One(OrderingError.UnknownField))
  }

  private def getOrderingConditionResult(unsafeOrderingCondition: String) = {
    val maybe = parseOrderingCondition(unsafeOrderingCondition)
    Or.from(maybe, One(OrderingError.InvalidCondition))
  }

  protected def parseOrderingCondition(unsafeOrderingCondition: String): Option[OrderingCondition] = unsafeOrderingCondition match {
      case "asc" => Some(OrderingCondition.AscendingOrder)
      case "desc" => Some(OrderingCondition.DescendingOrder)
      case _ => None
    }
}
