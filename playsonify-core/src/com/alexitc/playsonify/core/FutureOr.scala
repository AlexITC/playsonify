package com.alexitc.playsonify.core

import org.scalactic.{Bad, Good, One, Or}

import scala.concurrent.{ExecutionContext, Future}

import com.alexitc.playsonify.models.ApplicationError

/**
 * Monad transformer for composing values with [[FutureApplicationResult]] type.
 */
class FutureOr[E, +A](val future: FutureApplicationResult[E, A]) extends AnyVal {

  def toFuture: Future[ApplicationResult[E, A]] = future

  def flatMap[B](f: A => FutureOr[E, B])(implicit ec: ExecutionContext): FutureOr[E, B] = {
    val newFuture = future.flatMap {
      case Good(a) => f(a).toFuture
      case Bad(error) => Future.successful(Bad(error))
    }

    new FutureOr(newFuture)
  }

  def map[B](f: A => B)(implicit ec: ExecutionContext): FutureOr[E, B] = {
    val newFuture = future.map { _.map(f) }
    new FutureOr(newFuture)
  }

  def mapWithError[B](f: A => B Or ApplicationErrors[E])(implicit ec: ExecutionContext): FutureOr[E, B] = {
    val newFuture = future.map {
      case Good(x) => f(x)
      case Bad(e) => Bad(e)
    }

    new FutureOr(newFuture)
  }
}

object FutureOr {

  object Implicits {
    implicit class FutureOps[E, +A](val future: FutureApplicationResult[E, A]) extends AnyVal {

      def toFutureOr: FutureOr[E, A] = {
        new FutureOr(future)
      }
    }

    implicit class FutureListOps[E, +A](val inner: List[FutureApplicationResult[E, A]]) extends AnyVal {

      def toFutureOr(implicit ec: ExecutionContext): FutureOr[E, List[A]] = {
        val futureList = Future.sequence(inner)

        val future = futureList.map { resultList =>
          val errorsMaybe = resultList
            .flatMap(_.swap.toOption)
            .reduceLeftOption(_ ++ _)
            .map(_.distinct)

          errorsMaybe
            .map(Bad(_))
            .getOrElse {
              val valueList = resultList.flatMap(_.toOption)
              Good(valueList)
            }
        }

        new FutureOr(future)
      }
    }

    implicit class OrOps[E, +A](val or: ApplicationResult[E, A]) extends AnyVal {

      def toFutureOr: FutureOr[E, A] = {
        val future = Future.successful(or)
        new FutureOr(future)
      }
    }

    implicit class OptionOps[E, +A](val option: Option[A]) extends AnyVal {

      def toFutureOr(error: E): FutureOr[E, A] = {
        val or = Or.from(option, One(error))
        val future = Future.successful(or)
        new FutureOr(future)
      }
    }
  }
}
