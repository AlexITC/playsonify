package controllers.common

import com.alexitc.playsonify.test.PlayAPISpec
import org.slf4j.LoggerFactory
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

trait MyPlayAPISpec extends PlayAPISpec {

  protected val logger = LoggerFactory.getLogger(this.getClass)

  override def log[T](request: FakeRequest[T], response: Future[Result]): Unit = {
    logger.info(s"REQUEST > $request, headers = ${request.headers}; RESPONSE < status = ${status(response)}, body = ${contentAsString(response)}")
  }
}
