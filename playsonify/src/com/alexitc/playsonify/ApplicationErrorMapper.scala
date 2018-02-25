package com.alexitc.playsonify

import com.alexitc.playsonify.models.{ApplicationError, PublicError}
import play.api.i18n.Lang

/**
 * Maps an [[ApplicationError]] to a list of [[PublicError]].
 */
trait ApplicationErrorMapper {

  def toPublicErrorList(error: ApplicationError)(implicit lang: Lang): Seq[PublicError]
}
