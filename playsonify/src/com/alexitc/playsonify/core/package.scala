package com.alexitc.playsonify

import org.scalactic.{Every, Or}

import scala.concurrent.Future

import com.alexitc.playsonify.models.ApplicationError

package object core {

  type ApplicationErrors = Every[ApplicationError]
  type ApplicationResult[+A] = A Or ApplicationErrors
  type FutureApplicationResult[+A] = Future[ApplicationResult[A]]
}
