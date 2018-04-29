package com.alexitc.playsonify.validators

import com.alexitc.playsonify.core.ApplicationResult
import com.alexitc.playsonify.models._
import org.scalactic.{Accumulation, Bad, Good}

class PaginatedQueryValidator {

  import PaginatedQueryValidator._

  def validate(query: PaginatedQuery, maxLimit: Int): ApplicationResult[PaginatedQuery] = {
    Accumulation.withGood(
      validateOffset(query.offset),
      validateLimit(query.limit, maxLimit)) {

      PaginatedQuery.apply
    }
  }

  private def validateOffset(offset: Offset): ApplicationResult[Offset] = {
    if (offset.int >= MinOffset) {
      Good(offset)
    } else {
      Bad(PaginatedQueryOffsetError).accumulating
    }
  }

  private def validateLimit(limit: Limit, maxLimit: Int): ApplicationResult[Limit] = {
    if (MinLimit to maxLimit contains limit.int) {
      Good(limit)
    } else {
      Bad(PaginatedQueryLimitError(maxLimit)).accumulating
    }
  }
}

object PaginatedQueryValidator {

  private val MinOffset = 0
  private val MinLimit = 1

}
