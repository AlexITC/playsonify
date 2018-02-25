# playsonify
An opinionated library to help you build JSON APIs in a practical way using Play Framework

## State
while there are not too many commits in the project, the library has been used for some months on the [Crypto Coin Alerts project](https://github.com/AlexITC/crypto-coin-alerts) and it's in a stable state.


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

## Usage
At the moment the documentation is incomplete, there are several examples in the [tests](playsonify/test/src/com/alexitc/playsonify/controllers) and in the controllers from the [Crypto Coin Alerts project](https://github.com/AlexITC/crypto-coin-alerts/tree/master/alerts-server/app/controllers).


## Development
The project is built using the [mill](https://github.com/lihaoyi/mill) build tool instead of `sbt`, hence, you need to install `mill` in order to build the project.

### Compile
`mill playsonify.compile`

### Test
`mill playsonify.test`

