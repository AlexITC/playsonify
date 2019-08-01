package com.alexitc.playsonify

import com.alexitc.playsonify.models.ApplicationError
import com.alexitc.playsonify.models.pagination.PaginatedResult
import org.scalactic.{Every, Or}

import scala.concurrent.Future

package object core {

  type ApplicationErrors[E] = Every[E]
  type ApplicationResult[E, +A] = A Or ApplicationErrors[E]
  type FutureApplicationResult[E, +A] = Future[ApplicationResult[E, A]]
  type FuturePaginatedResult[E, +A] = FutureApplicationResult[E, PaginatedResult[A]]
}
