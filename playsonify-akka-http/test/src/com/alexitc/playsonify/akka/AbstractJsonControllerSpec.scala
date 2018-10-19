package com.alexitc.playsonify.akka

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.alexitc.playsonify.akka.controllers.TestController
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.JsValue

class AbstractJsonControllerSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with PlayJsonSupport {

  val controller = new TestController
  val routes = controller.routes

  "general pieces" should {
    "return json on unknown route" in {
      pending

      val request = Get("/unknown")
      request ~> routes ~> check {
        println(response)
        status should ===(StatusCodes.NotFound)

        val json = unmarshal[JsValue](response.entity).get
        val errors = (json \ "errors").as[List[JsValue]]
        errors.size should be(1)

        val error = errors.head
        (error \ "type").as[String] should be(PublicErrorRenderer.GenericErrorType)
        (error \ "message").as[String].nonEmpty should be(true)
      }
    }
  }

  "publicNoInput" should {

    "serialize a result as json" in {
      val request = Get("/no-input/model")
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        val json = unmarshal[JsValue](response.entity).get
        (json \ "int").as[Int] should be(0)
        (json \ "string").as[String] should be("none")
      }
    }

    "allows to override successful result status" in {
      val request = Get("/no-input/model-custom-status")
      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        val json = unmarshal[JsValue](response.entity).get
        (json \ "int").as[Int] should be(0)
        (json \ "string").as[String] should be("none")
      }
    }

    "serialize an error list as json" in {
      val request = Get("/no-input/errors")
      request ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)

        val json = unmarshal[JsValue](response.entity).get
        testForErrorList(json)
      }
    }

    "serialize exceptions as json with an error id" in {
      val request = Get("/no-input/exception")
      request ~> routes ~> check {
        status should ===(StatusCodes.InternalServerError)

        val json = unmarshal[JsValue](response.entity).get
        testForException(json)
      }
    }
  }

  "publicWithInput" should {
    val defaultBody =
      """
        | {
        |   "int": 0,
        |   "string": "none"
        | }
      """.stripMargin

    "serialize a result as json" in {
      val request = Post("/input/model", HttpEntity(ContentTypes.`application/json`, defaultBody))
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        val json = unmarshal[JsValue](response.entity).get
        (json \ "int").as[Int] should be(0)
        (json \ "string").as[String] should be("none")
      }
    }

    "reject invalid json body" in {
      val body =
        """
          | {
          |   int: 0
          |   string: "none"
          | }
        """.stripMargin

      val request = Post("/input/model", HttpEntity(ContentTypes.`application/json`, body))
      request ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)

        val json = unmarshal[JsValue](response.entity).get
        val errors = (json \ "errors").as[List[JsValue]]
        errors.size should be(1)

        val error = errors.head
        (error \ "type").as[String] should be(PublicErrorRenderer.GenericErrorType)
        (error \ "message").as[String].nonEmpty should be(true)
      }
    }

    "reject json with missing field" in {
      val body =
        """
          | {
          |   "string": "none"
          | }
        """.stripMargin

      val request = Post("/input/model", HttpEntity(ContentTypes.`application/json`, body))
      request ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)

        val json = unmarshal[JsValue](response.entity).get
        val errors = (json \ "errors").as[List[JsValue]]
        errors.size should be(1)

        val error = errors.head
        (error \ "type").as[String] should be(PublicErrorRenderer.FieldValidationErrorType)
        (error \ "field").as[String] should be("int")
        (error \ "message").as[String].nonEmpty should be(true)
      }
    }

    "reject json with wrong types" in {
      val body =
        """
          | {
          |   "int": "1",
          |   "string": "none"
          | }
        """.stripMargin

      val request = Post("/input/model", HttpEntity(ContentTypes.`application/json`, body))
      request ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)

        val json = unmarshal[JsValue](response.entity).get
        val errors = (json \ "errors").as[List[JsValue]]
        errors.size should be(1)

        val error = errors.head
        (error \ "type").as[String] should be(PublicErrorRenderer.FieldValidationErrorType)
        (error \ "field").as[String] should be("int")
        (error \ "message").as[String].nonEmpty should be(true)
      }
    }

    "reject empty body" in {
      val request = Post("/input/model", HttpEntity(ContentTypes.`application/json`, ""))
      request ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)

        val json = unmarshal[JsValue](response.entity).get
        val errors = (json \ "errors").as[List[JsValue]]
        errors.size should be(1)

        val error = errors.head
        (error \ "type").as[String] should be(PublicErrorRenderer.GenericErrorType)
        (error \ "message").as[String].nonEmpty should be(true)
      }
    }

    "allows to override successful result status" in {
      val request = Post("/input/model-custom-status", HttpEntity(ContentTypes.`application/json`, defaultBody))
      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        val json = unmarshal[JsValue](response.entity).get
        (json \ "int").as[Int] should be(0)
        (json \ "string").as[String] should be("none")
      }
    }

    "serialize an error list as json" in {
      val request = Post("/input/errors", HttpEntity(ContentTypes.`application/json`, defaultBody))
      request ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)

        val json = unmarshal[JsValue](response.entity).get
        testForErrorList(json)
      }
    }

    "serialize exceptions as json with an error id" in {
      val request = Post("/input/exception", HttpEntity(ContentTypes.`application/json`, defaultBody))
      request ~> routes ~> check {
        status should ===(StatusCodes.InternalServerError)

        val json = unmarshal[JsValue](response.entity).get
        testForException(json)
      }
    }
  }

  "authenticatedNoInput" should {

    import akka.http.scaladsl.model.headers._

    "serialize a result as json" in {
      val id = "playsonify"
      val request = Get("/authenticated/model")
          .withHeaders(Authorization(OAuth2BearerToken(id)))

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        val json = unmarshal[JsValue](response.entity).get
        (json \ "int").as[Int] should be(0)
        (json \ "string").as[String] should be(s"Bearer $id")
      }
    }

    "allows to override successful result status" in {
      val id = "playsonify"
      val request = Get("/authenticated/model-custom-status")
          .withHeaders(Authorization(OAuth2BearerToken(id)))

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        val json = unmarshal[JsValue](response.entity).get
        (json \ "int").as[Int] should be(0)
        (json \ "string").as[String] should be(s"Bearer $id")
      }
    }

    "return UNAUTHORIZED when no AUTHORIZATION header is present" in {
      val request = Get("/authenticated/model")

      request ~> routes ~> check {
        status should ===(StatusCodes.Unauthorized)

        val json = unmarshal[JsValue](response.entity).get
        val errorList = (json \ "errors").as[List[JsValue]]
        errorList.size should be(1)

        val firstError = errorList.head
        (firstError \ "type").as[String] should be(PublicErrorRenderer.HeaderValidationErrorType)
        (firstError \ "header").as[String] should be("Authorization")
        (firstError \ "message").as[String].nonEmpty should be(true)
      }
    }
  }

  "authenticatedWithInput" should {

    import akka.http.scaladsl.model.headers._

    val defaultBody =
      """
        | {
        |   "int": 0,
        |   "string": "none"
        | }
      """.stripMargin

    "serialize a result as json" in {
      val id = "playsonify"
      val request = Post("/authenticated-input/model", HttpEntity(ContentTypes.`application/json`, defaultBody))
          .withHeaders(Authorization(OAuth2BearerToken(id)))

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        val json = unmarshal[JsValue](response.entity).get
        (json \ "int").as[Int] should be(0)
        (json \ "string").as[String] should be(s"none")
      }
    }

    "allows to override successful result status" in {
      val id = "playsonify"
      val request = Post("/authenticated-input/model-custom-status", HttpEntity(ContentTypes.`application/json`, defaultBody))
          .withHeaders(Authorization(OAuth2BearerToken(id)))

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        val json = unmarshal[JsValue](response.entity).get
        (json \ "int").as[Int] should be(0)
        (json \ "string").as[String] should be(s"none")
      }
    }

    "return UNAUTHORIZED when no AUTHORIZATION header is present" in {
      val request = Post("/authenticated-input/model-custom-status", HttpEntity(ContentTypes.`application/json`, defaultBody))

      request ~> routes ~> check {
        status should ===(StatusCodes.Unauthorized)

        val json = unmarshal[JsValue](response.entity).get
        val errorList = (json \ "errors").as[List[JsValue]]
        errorList.size should be(1)

        val firstError = errorList.head
        (firstError \ "type").as[String] should be(PublicErrorRenderer.HeaderValidationErrorType)
        (firstError \ "header").as[String] should be("Authorization")
        (firstError \ "message").as[String].nonEmpty should be(true)
      }
    }
  }

  def testForErrorList(json: JsValue) = {
    val errorList = (json \ "errors").as[List[JsValue]]
    errorList.size should be(2)

    val firstError = errorList.head
    (firstError \ "type").as[String] should be(PublicErrorRenderer.FieldValidationErrorType)
    (firstError \ "field").as[String] should be("field")
    (firstError \ "message").as[String].nonEmpty should be(true)

    val secondError = errorList.lift(1).get
    (secondError \ "type").as[String] should be(PublicErrorRenderer.FieldValidationErrorType)
    (secondError \ "field").as[String] should be("anotherField")
    (secondError \ "message").as[String].nonEmpty should be(true)
  }

  def testForException(json: JsValue) = {
    val errorList = (json \ "errors").as[List[JsValue]]
    errorList.size should be(1)

    val error = errorList.head
    (error \ "type").as[String] should be(PublicErrorRenderer.ServerErrorType)
    (error \ "errorId").as[String].nonEmpty should be(true)
    (error \ "message").as[String].nonEmpty should be(true)
  }
}
