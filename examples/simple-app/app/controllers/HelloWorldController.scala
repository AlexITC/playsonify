package controllers

import javax.inject.Inject

import com.alexitc.example.UserError
import controllers.common.{MyJsonController, MyJsonControllerComponents}
import org.scalactic.{Bad, Every, Good}
import play.api.libs.json.{Json, Reads, Writes}

import scala.concurrent.Future

class HelloWorldController @Inject() (components: MyJsonControllerComponents) extends MyJsonController(components) {

  import Context._

  def hello = publicInput { context: HasModel[Person] =>
    val msg = s"Hello ${context.model.name}, you are ${context.model.age} years old"
    val helloMessage = HelloMessage(msg)
    val goodResult = Good(helloMessage)

    Future.successful(goodResult)
  }

  def authenticatedHello = authenticated { context: Authenticated =>
    val msg = s"Hello user with id ${context.auth}"
    val helloMessage = HelloMessage(msg)
    val goodResult = Good(helloMessage)

    Future.successful(goodResult)
  }

  def failedHello = public[HelloMessage] { context: Context =>
    val errors = Every(UserError.UserEmailIncorrect, UserError.UserAlreadyExist, UserError.UserNotFound)

    val badResult = Bad(errors)
    Future.successful(badResult)
  }

  def exceptionHello = public[HelloMessage] { context: Context =>
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
