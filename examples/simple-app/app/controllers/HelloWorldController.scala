package controllers

import javax.inject.Inject

import com.alexitc.example.UserError
import com.alexitc.playsonify.models.{AuthenticatedContext, PublicContext, PublicContextWithModel}
import controllers.common.{MyJsonController, MyJsonControllerComponents}
import org.scalactic.{Bad, Every, Good}
import play.api.i18n.Lang
import play.api.libs.json.{Json, Reads, Writes}

import scala.concurrent.Future

class HelloWorldController @Inject() (components: MyJsonControllerComponents)
    extends MyJsonController(components) {

  def hello = publicWithInput { context: PublicContextWithModel[Person, Lang] =>
    val msg = s"Hello ${context.model.name}, you are ${context.model.age} years old"
    val helloMessage = HelloMessage(msg)
    val goodResult = Good(helloMessage)

    Future.successful(goodResult)
  }

  def authenticatedHello = authenticatedNoInput { context: AuthenticatedContext[Int, Lang] =>
    val msg = s"Hello user with id ${context.auth}"
    val helloMessage = HelloMessage(msg)
    val goodResult = Good(helloMessage)

    Future.successful(goodResult)
  }

  def failedHello = publicNoInput[HelloMessage] { context: PublicContext[Lang] =>
    val errors = Every(
      UserError.UserEmailIncorrect,
      UserError.UserAlreadyExist,
      UserError.UserNotFound)

    val badResult = Bad(errors)
    Future.successful(badResult)
  }

  def exceptionHello = publicNoInput[HelloMessage] { context: PublicContext[Lang] =>
    Future.failed(new RuntimeException("database unavailable"))
  }
}

case class Person(name: String, age: Int)

object Person {
  implicit val reads: Reads[Person] = Json.reads[Person]
}


case class HelloMessage(message: String)

object HelloMessage {
  implicit val writes: Writes[HelloMessage] = Json.writes[HelloMessage]
}
