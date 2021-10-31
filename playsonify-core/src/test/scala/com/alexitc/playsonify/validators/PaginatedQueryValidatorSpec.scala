package com.alexitc.playsonify.validators

import com.alexitc.playsonify.models.pagination.{Limit, Offset, PaginatedQuery, PaginatedQueryError}
import org.scalactic.{Bad, Every, Good}
import org.scalatest.matchers.must.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class PaginatedQueryValidatorSpec extends AnyWordSpec {

  val validator = new PaginatedQueryValidator

  "validate" should {
    "succeed on valid query" in {
      val query = PaginatedQuery(Offset(0), Limit(100))
      val maxLimit = 100
      val expected = Good(query)
      val result = validator.validate(query, maxLimit)

      result mustEqual expected
    }

    "fail on offset < 0" in {
      val query = PaginatedQuery(Offset(-1), Limit(1))
      val maxLimit = 100
      val expected = Bad(PaginatedQueryError.InvalidOffset).accumulating
      val result = validator.validate(query, maxLimit)

      result mustEqual expected
    }

    "fail on limit = 0" in {
      val query = PaginatedQuery(Offset(0), Limit(0))
      val maxLimit = 100
      val expected = Bad(PaginatedQueryError.InvalidLimit(maxLimit)).accumulating
      val result = validator.validate(query, maxLimit)

      result mustEqual expected
    }

    "fail on limit > maxLimit" in {
      val query = PaginatedQuery(Offset(0), Limit(101))
      val maxLimit = 100
      val expected = Bad(PaginatedQueryError.InvalidLimit(maxLimit)).accumulating
      val result = validator.validate(query, maxLimit)

      result mustEqual expected
    }

    "accumulate errors when offset and limit are invalid" in {
      val query = PaginatedQuery(Offset(-1), Limit(101))
      val maxLimit = 100
      val expected = Bad(Every(PaginatedQueryError.InvalidOffset, PaginatedQueryError.InvalidLimit(maxLimit)))
      val result = validator.validate(query, maxLimit)

      result mustEqual expected
    }
  }
}
