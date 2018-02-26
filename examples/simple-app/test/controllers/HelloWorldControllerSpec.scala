package controllers

import controllers.common.MyPlayAPISpec
import play.api.test.Helpers._

class HelloWorldControllerSpec extends MyPlayAPISpec {

  override val application = guiceApplicationBuilder.build()

  "POST /hello" should {
    "succeed" in {
      val name = "Alex"
      val age = 18
      val body =
        s"""
           |{
           |  "name": "$name",
           |  "age": $age
           |}
         """.stripMargin

      val response = POST("/hello", Some(body))
      status(response) mustEqual OK

      val json = contentAsJson(response)
      (json \ "message").as[String] mustEqual "Hello Alex, you are 18 years old"
    }
  }
}
