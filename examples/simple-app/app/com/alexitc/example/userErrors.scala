package com.alexitc.example

import com.alexitc.playsonify.models.{ConflictError, InputValidationError, NotFoundError}

sealed trait UserError
case object UserAlreadyExistError extends UserError with ConflictError
case object UserNotFoundError extends UserError with NotFoundError
case object UserEmailIncorrectError extends UserError with InputValidationError
