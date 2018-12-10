package controllers.common

import com.alexitc.playsonify.models.ServerError
import com.alexitc.playsonify.play.AbstractJsonController
import org.slf4j.LoggerFactory

abstract class MyJsonController(components: MyJsonControllerComponents) extends AbstractJsonController(components) {

  protected val logger = LoggerFactory.getLogger(this.getClass)

  override protected def onServerError(error: ServerError): Unit = {
    error.cause match {
      case Some(cause) =>
        logger.error(s"Unexpected internal error, id = ${error.id.string}, error = $error", cause)

      case None =>
        logger.error(s"Unexpected internal error, id = ${error.id.string}, error = $error}")
    }
  }
}
