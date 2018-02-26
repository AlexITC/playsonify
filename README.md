# playsonify
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


## Usage
At the moment the documentation is incomplete, there are several examples in the [tests](playsonify/test/src/com/alexitc/playsonify/controllers) and in the controllers from the [Crypto Coin Alerts project](https://github.com/AlexITC/crypto-coin-alerts/tree/master/alerts-server/app/controllers).


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
class DummyAuthenticatorService extends AbstractAuthenticatorService {
  override def authenticate[A](request: Request[A]): FutureApplicationResult[Int] = {
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
class MyController @Inject() (components: MyJsonControllerComponents) extends MyJsonController(components) {
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
    val msg = s"Hello ${context.model.name}, you are ${context.person.age} years old"
    HelloMessage(msg)
  }
}
```

What about authenticating the request?
```scala
...
def helloAuth = authenticatedNoInput { context: AuthenticatedContext =>
  val msg = s"Hello user with id ${context.auth}"
  HelloMessage(msg)
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
