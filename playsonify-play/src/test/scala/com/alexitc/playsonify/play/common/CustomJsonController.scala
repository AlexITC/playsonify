package com.alexitc.playsonify.play.common

import com.alexitc.playsonify.models.ServerError
import com.alexitc.playsonify.play.AbstractJsonController
import org.slf4j.LoggerFactory

class CustomJsonController (components: CustomControllerComponents) extends AbstractJsonController(components) {

  protected val logger = LoggerFactory.getLogger(this.getClass)

  override protected def onServerError(error: ServerError): Unit = {
    if (error.cause.isEmpty) {
      logger.error(s"Unexpected internal error = ${error.id.string}")
    }

    error.cause.foreach { cause =>
      logger.error(s"Unexpected internal error = ${error.id.string}", cause)
    }
  }
}
