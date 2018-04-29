package com.alexitc.playsonify

import com.alexitc.playsonify.models.{ApplicationError, PaginatedResult}
import org.scalactic.{Every, Or}

import scala.concurrent.Future

package object core {

  type ApplicationErrors = Every[ApplicationError]
  type ApplicationResult[+A] = A Or ApplicationErrors
  type FutureApplicationResult[+A] = Future[ApplicationResult[A]]
  type FuturePaginatedResult[+A] = FutureApplicationResult[PaginatedResult[A]]
}
