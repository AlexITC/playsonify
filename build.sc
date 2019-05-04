import mill._
import mill.scalalib._
import mill.scalalib.publish._

object `playsonify-core` extends PlaysonifyModule {
  def ivyDeps = Agg(
    scalactic
  )

  object test extends Tests{
    def ivyDeps = Agg(
      scalatestPlusPlay
    )

    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}

object `playsonify-play` extends PlaysonifyModule {
  def moduleDeps = Seq(`playsonify-core`)

  def ivyDeps = Agg(
    playframework,
    scalactic
  )

  object test extends Tests{
    def ivyDeps = Agg(
      scalatestPlusPlay
    )

    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}

object `playsonify-play-test` extends PlaysonifyModule {

  def ivyDeps = Agg(
    scalatestPlusPlay
  )
}

object `playsonify-akka-http` extends PlaysonifyModule {
  def moduleDeps = Seq(`playsonify-core`)

  def ivyDeps = Agg(
    akkaHttp,
    akkaStreams,
    akkaHttpPlayJson,
    playJson,
    scalactic
  )

  object test extends Tests {

    def ivyDeps = Agg(
      scalatest,
      mockito,
      akkaHttpTestkit
    )

    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}

object `playsonify-sql` extends PlaysonifyModule {
  def moduleDeps = Seq(`playsonify-core`)

  def ivyDeps = Agg(
    scalactic
  )

  object test extends Tests {

    def ivyDeps = Agg(
      scalatest
    )

    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}

trait PlaysonifyModule extends ScalaModule with PublishModule {
  def scalaVersion = "2.12.7"
  def publishVersion = "2.0.1"

  val playframework = ivy"com.typesafe.play::play:2.6.20"
  val scalactic = ivy"org.scalactic::scalactic:3.0.5"
  val akkaStreams = ivy"com.typesafe.akka::akka-stream:2.5.12"
  val akkaHttp = ivy"com.typesafe.akka::akka-http:10.1.5"
  val akkaHttpTestkit = ivy"com.typesafe.akka::akka-http-testkit:10.1.4"
  val akkaHttpPlayJson = ivy"de.heikoseeberger::akka-http-play-json:1.22.0"
  val playJson = ivy"com.typesafe.play::play-json:2.6.10"
  val scalatest = ivy"org.scalatest::scalatest:3.0.5"
  val scalatestPlusPlay = ivy"org.scalatestplus.play::scalatestplus-play:3.1.2"
  val mockito = ivy"org.mockito:mockito-core:2.15.0"

  def pomSettings = PomSettings(
    description = "An opinionated library to help you build JSON APIs in a practical way using Play Framework ",
    organization = "com.alexitc",
    url = "https://github.com/AlexITC/playsonify",
    licenses = Seq(
      License("MIT license", "https://github.com/AlexITC/playsonify/blob/master/LICENSE")
    ),
    scm = SCM(
      "git://github.com/AlexITC/playsonify.git",
      "scm:git://github.com/AlexITC/playsonify.git"
    ),
    developers = Seq(
      Developer("AlexITC", "Alexis Hernandez","https://github.com/AlexITC")
    )
  )
}
