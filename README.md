# playsonify

[![Build Status](https://travis-ci.org/AlexITC/playsonify.svg?branch=master)](https://travis-ci.org/AlexITC/playsonify)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e09923e277df4191ab2a1c3a4953ce41)](https://www.codacy.com/app/AlexITC/playsonify?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=AlexITC/playsonify&amp;utm_campaign=Badge_Grade)
[![Join the chat at https://gitter.im/playsonify/Lobby](https://badges.gitter.im/playsonify/Lobby.svg)](https://gitter.im/playsonify/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven Central](https://img.shields.io/maven-central/v/com.alexitc/playsonify_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.alexitc/playsonify_2.12)

An opinionated library to help you build JSON APIs in a practical way using Play Framework

## State
while there are not too many commits in the project, the library has been used for some months on the [Crypto Coin Alerts project](https://github.com/AlexITC/crypto-coin-alerts) and it's in a stable state.

## Support
The library has been tested with the following versions, it might work with other versions while that is not official support for versions that are not in this list.
- Scala 2.12
- Play Framework 2.6

## Name
The name `playsonify` was inspired by mixing the `JSON.stringify` function from JavaScript and the Play Framework which is what it is built for.


## Features
- Validate, deserialize and map the incoming request body to a model class automatically.
- Serialize the result automatically.
- Automatically map errors to the proper HTTP status code (OK, BAD_REQUEST, etc).
- Support i18n easily.
- Render several errors instead of just the first one.
- Keeps error responses consistent.
- Authenticate requests easily.
- HTTP Idiomatic controller tests.



## What can playsonify do?

Try it by yourself with this [simple-app](examples/simple-app).

Let's define an input model:
```scala
case class Person(name: String, age: Int)

object Person {
  implicit val reads: Reads[Person] = Json.reads[Person]
}
```

Define an output model:
```scala
case class HelloMessage(message: String)

object HelloMessage {
  implicit val writes: Writes[HelloMessage] = Json.writes[HelloMessage]
}
```

Define a controller:
```scala
class HelloWorldController @Inject() (components: MyJsonControllerComponents)
    extends MyJsonController(components) {

  def hello = publicWithInput { context: PublicContextWithModel[Person] =>
    val msg = s"Hello ${context.model.name}, you are ${context.model.age} years old"
    val helloMessage = HelloMessage(msg)
    val goodResult = Good(helloMessage)

    Future.successful(goodResult)
  }

  def authenticatedHello = authenticatedNoInput { context: AuthenticatedContext[Int] =>
    val msg = s"Hello user with id ${context.auth}"
    val helloMessage = HelloMessage(msg)
    val goodResult = Good(helloMessage)

    Future.successful(goodResult)
  }

  def failedHello = publicNoInput[HelloMessage] { context: PublicContext =>
    val errors = Every(UserEmailIncorrectError, UserAlreadyExistError, UserNotFoundError)
    val badResult = Bad(errors)

    Future.successful(badResult)
  }

  def exceptionHello = publicNoInput[HelloMessage] { context: PublicContext =>
    Future.failed(new RuntimeException("database unavailable"))
  }
}
```

Last, define the routes file (conf/routes):
```
POST /hello         controllers.HelloWorldController.hello()
GET  /auth          controllers.HelloWorldController.authenticatedHello()
GET  /errors        controllers.HelloWorldController.failedHello()
GET  /exception     controllers.HelloWorldController.exceptionHello()
```

These are some of the features that you get automatically:

### Deserialization and serialization
Request:
```bash
curl -H "Content-Type: application/json" \
  -X POST -d \
  '{"name":"Alex","age":18}' \
   localhost:9000/hello
```

Response:
```
{"message":"Hello Alex, you are 18 years old"}
```


### Simple authentication
Request:
```bash
curl -H "Authorization: 13" localhost:9000/auth
```

Response:
```
{"message":"Hello user with id 13"}
```


### Automatic exception handling
Request:
```bash
curl -v localhost:9000/exception
```

Response:
```
< HTTP/1.1 500 Internal Server Error
{
   "errors":[
      {
         "type":"server-error",
         "errorId":"bc49d715fb8d4255a62c30d13322205b"
      }
   ]
}
```

### Simple error accumulation
Request:
```bash
curl -v localhost:9000/errors
```

Response:
```
< HTTP/1.1 400 Bad Request
{
   "errors":[
      {
         "type":"field-validation-error",
         "field":"email",
         "message":"The email format is incorrect"
      },
      {
         "type":"field-validation-error",
         "field":"email",
         "message":"The user already exist"
      },
      {
         "type":"field-validation-error",
         "field":"userId",
         "message":"The user was not found"
      }
   ]
}
```


### Controller testing is simple
```scala
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
```

## Usage
The documentation might be incomplete, you can take a look to these examples:
- The [tests](playsonify/test/src/com/alexitc/playsonify/controllers).
- The [example application](examples/simple-app).
- The controllers from the [Crypto Coin Alerts project](https://github.com/AlexITC/crypto-coin-alerts/tree/master/alerts-server/app/controllers).


### Add dependencies
Add these lines to your `build.sbt` file:
- `libraryDependencies += "com.alexitc" %% "playsonify" % "1.0.0"`
- `libraryDependencies += "com.alexitc" %% "playsonifytest" % "1.0.0" % Test` (optional, useful for testing).


#### Familiarize with scalactic Or and Every
Playsonify uses [scalactic Or and Every](http://www.scalactic.org/user_guide/OrAndEvery) a lot, in summary, we have replaced `Either[L, R]` with `Or[G, B]`, it allow us to construct value having a `Good` or a `Bad` result. Also, `Every` is a non-empty list which gives some compile time guarantees. 

As you might have noted, the use of scalactic could be easily replaced with `scalaz` or `cats`, in the future, we might add support to let you choose which library to use.


#### Familiarize with our type aliases
There are some type aliases that are helpful to not be nesting a lot of types on the method signatures, see the [core package](playsonify/src/com/alexitc/playsonify/core/package.scala), it looks like this:

```scala
type ApplicationErrors = Every[ApplicationError]
type ApplicationResult[+A] = A Or ApplicationErrors
type FutureApplicationResult[+A] = Future[ApplicationResult[A]]
```

- `ApplicationErrors` represents a non-empty list of errors.
- `ApplicationResult` represents a result or a non-empty list of errors.
- `FutureApplicationResult` represents a result or a non-empty list of error that will be available in the future (asynchronous result).

#### Create your application specific errors
We have already defined some top-level [application errors](playsonify/src/com/alexitc/playsonify/models/applicationErrors.scala), you are required to extend them in your error classes, this is crucial to get the correct mapping from an error to the HTTP status.
```scala
trait InputValidationError extends ApplicationError
trait ConflictError extends ApplicationError
trait NotFoundError extends ApplicationError
trait AuthenticationError extends ApplicationError
trait ServerError extends ApplicationError {
  // contains data private to the server
  def cause: Throwable
}
```

For example, let's say that we want to define the possible errors related to a user, we could define some errors:
```scala
sealed trait UserError
case object UserAlreadyExistError extends UserError with ConflictError
case object UserNotFoundError extends UserError with NotFoundError
case object UserEmailIncorrectError extends UserError with InputValidationError
```

Then, when playsonify detect a `Bad` result, it will map the error to an HTTP status code in the following way:
- InputValidationError -> 404 (BAD_REQUEST).
- ConflictError -> 409 (CONFLICT).
- NotFoundError -> 404 (NOT_FOUND).
- AuthenticationError -> 401 (UNAUTHORIZED).
- ServerError -> 500 (INTERNAL_SERVER_ERROR).

Hence, your task is to tag your error types with these top-level errors properly.

Here you have a real example: [errors package](https://github.com/AlexITC/crypto-coin-alerts/tree/master/alerts-server/app/com/alexitc/coinalerts/errors).



#### Create an error mapper
You are required to create your own [ApplicationErrorMapper](playsonify/src/com/alexitc/playsonify/ApplicationErrorMapper.scala), the task here is to map your application specific errors to our [PublicError](playsonify/src/com/alexitc/playsonify/models/publicErrors.scala), these errors should be safe to be displayed to clients.

When you implement this error mapper, you will notice that the `toPublicErrorList` method already receives a `Lang` which make it easy to support multiple languages.

For example:
```scala
class MyApplicationErrorMapper @Inject() (messagesApi: MessagesApi) extends ApplicationErrorMapper {

  override def toPublicErrorList(error: ApplicationError)(implicit lang: Lang): Seq[PublicError] = error match {
    // server errors are not supposed to be mapped to PublicError
    case _: ServerError => List.empty

    // at the moment, you are required to catch this error,
    // it happens when there are errors while mapping the request body to JSON.
    case JsonFieldValidationError(path, errors) =>
      val field = path.path.map(_.toJsonString.replace(".", "")).mkString(".")
      errors.map { messageKey =>
        val message = messagesApi(messageKey.string)
        FieldValidationError(field, message)
      }

    // having a top-level error allow us to delegate the mapping task to specific functions
    case e: UserError => List(renderUserError(e))

    // it is better avoid a catch-all here in order to get a RuntimeException when an error is not mapped,
    // I know, this is ugly, hopefully we will improve this in the future, so, please, ensure you have covered
    // all possible errors in your tests.
  }

  // we use message keys to support multiple languages, store these keys in the messages file.
  private def renderUserError(error: UserError)(implicit lang: Lang) = error match {
    case UserAlreadyExistError =>
      val message = messagesApi("user.error.alreadyExist")
      FieldValidationError("email", message)
      
    case UserNotFoundError =>
      val message = messagesApi("user.error.notFound")
      FieldValidationError("userId", message)

    case UserEmailIncorrectError =>
      val message = messagesApi("user.error.incorrectEmail")
      FieldValidationError("email", message)
  }
}

```

Here you have a real example: [MyApplicationErrorMapper](https://github.com/AlexITC/crypto-coin-alerts/blob/master/alerts-server/app/com/alexitc/coinalerts/errors/MyApplicationErrorMapper.scala).


#### Define your authenticataion mechanism
You are required to define your own [AbstractAuthenticatorService](playsonify/src/com/alexitc/playsonify/AbstractAuthenticatorService.scala), this service have the responsibility to decide which requests are aunthenticated and which ones are not, you first task is to define a model to represent an authenticated request, it is common to take the user or the user id for this, this model will be available in your controllers while dealing with authenticated requests.

For example, suppose that we'll use an `Int` to represent the id of the user performing the request, at first, define the errors that represents that a request wasn't authenticated, like this:

```scala
sealed trait SimpleAuthError
case object InvalidAuthorizationHeaderError extends SimpleAuthError with AuthenticationError
```

Note that while you could have defined the errors without the `SimpleAuthError`, it is recommended to do it in this way to simplify the code on your `ApplicationErrorMapper`.

Then, create your authenticator service, in this case, we'll a create a dummy authenticator which takes the value from the `Authorization` header and tries to convert it to an `Int` which we would be used as the user id (please, never use this unsecure approach):

```scala
class DummyAuthenticatorService extends AbstractAuthenticatorService[Int] {

  override def authenticate(request: Request[JsValue]): FutureApplicationResult[Int] = {
    val userIdMaybe = request
      .headers
      .get(HeaderNames.AUTHORIZATION)
      .flatMap { header => Try(header.toInt).toOption }

    val result = Or.from(userIdMaybe, One(InvalidAuthorizationHeaderError))
    Future.successful(result)
  }
}
```

Note that you might want to use a specific error when the header is not present, also, while the `Future` is not required in this specific case, it allow us to implement different approaches, like calling an external web service in this step.

Here you have a real example: [JWTAuthenticatorService](https://github.com/AlexITC/crypto-coin-alerts/blob/master/alerts-server/app/com/alexitc/coinalerts/services/JWTAuthenticatorService.scala).


#### Define your JsonControllerComponents
In order to provide your custom components, we'll create a custom [JsonControllerComponents](playsonify/src/com/alexitc/playsonify/JsonControllerComponents.scala), here you'll wire what you have just defined, for example:

```scala
class MyJsonControllerComponents @Inject() (
    override val messagesControllerComponents: MessagesControllerComponents,
    override val executionContext: ExecutionContext,
    override val publicErrorRenderer: PublicErrorRenderer,
    override val applicationErrorMapper: MyApplicationErrorMapper,
    override val authenticatorService: DummyAuthenticatorService)
    extends JsonControllerComponents[Int]
```

Here you have a real example: [MyJsonControllerComponents](https://github.com/AlexITC/crypto-coin-alerts/blob/master/alerts-server/app/controllers/MyJsonControllerComponents.scala).



#### Define your AbstractJsonController
Last, we need to define your customized [AbstractJsonController](playsonify/src/com/alexitc/playsonify/AbstractJsonController.scala), using guice dependency injection could lead us to this example:

```scala
abstract class MyJsonController(components: MyJsonControllerComponents) extends AbstractJsonController(components) {

  protected val logger = LoggerFactory.getLogger(this.getClass)

  override protected def onServerError(error: ServerError, errorId: ErrorId): Unit = {
    logger.error(s"Unexpected internal error = ${errorId.string}", error.cause)
  }
}
```

Here you have a real example: [MyJsonController](https://github.com/AlexITC/crypto-coin-alerts/blob/master/alerts-server/app/controllers/MyJsonController.scala).


#### Create your controllers
It is time to create your own controllers, let's define an input model for the request body:
```scala
case class Person(name: String, age: Int)

object Person {
  implicit val reads: Reads[Person] = Json.reads[Person]
}
```

Now, define the output response:
```scala
case class HelloMessage(message: String)

object HelloMessage {
  implicit val writes: Writes[HelloMessage] = Json.writes[HelloMessage]
}
```

And the controller:
```scala
class HelloWorldController @Inject() (components: MyJsonControllerComponents)
    extends MyJsonController(components) {

  def hello = publicWithInput { context: PublicContextWithModel[Person] =>
    val msg = s"Hello ${context.model.name}, you are ${context.model.age} years old"
    val helloMessage = HelloMessage(msg)
    val goodResult = Good(helloMessage)

    Future.successful(goodResult)
  }
}
```

What about authenticating the request?
```scala
...
  def authenticatedHello = authenticatedNoInput { context: AuthenticatedContext[Int] =>
    val msg = s"Hello user with id ${context.auth}"
    val helloMessage = HelloMessage(msg)
    val goodResult = Good(helloMessage)

    Future.successful(goodResult)
  }
...
```


Here you have a real example: [controllers package](https://github.com/AlexITC/crypto-coin-alerts/blob/master/alerts-server/app/controllers).


## Development
The project is built using the [mill](https://github.com/lihaoyi/mill) build tool instead of `sbt`, hence, you need to install `mill` in order to build the project.

The project has been built using `mill 0.1.1-36-7b7044`.

### Compile
`mill playsonify.compile`

### Test
`mill playsonify.test`

### Integrate with IntelliJ
This step should be run everytime `build.sc` is modified:
- `mill mill.scalalib.GenIdeaModule/idea`
