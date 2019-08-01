package com.alexitc.playsonify.validators

import com.alexitc.playsonify.core.ApplicationResult
import com.alexitc.playsonify.models.pagination.{Limit, Offset, PaginatedQuery, PaginatedQueryError}
import org.scalactic.{Accumulation, Bad, Good}

class PaginatedQueryValidator {

  import PaginatedQueryValidator._

  def validate(query: PaginatedQuery, maxLimit: Int): ApplicationResult[PaginatedQueryError, PaginatedQuery] = {
    Accumulation.withGood(validateOffset(query.offset), validateLimit(query.limit, maxLimit)) {

      PaginatedQuery.apply
    }
  }

  private def validateOffset(offset: Offset): ApplicationResult[PaginatedQueryError, Offset] = {
    if (offset.int >= MinOffset) {
      Good(offset)
    } else {
      Bad(PaginatedQueryError.InvalidOffset).accumulating
    }
  }

  private def validateLimit(limit: Limit, maxLimit: Int): ApplicationResult[PaginatedQueryError, Limit] = {
    if (MinLimit to maxLimit contains limit.int) {
      Good(limit)
    } else {
      Bad(PaginatedQueryError.InvalidLimit(maxLimit)).accumulating
    }
  }
}

object PaginatedQueryValidator {

  private val MinOffset = 0
  private val MinLimit = 1

}
