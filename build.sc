import mill._
import mill.scalalib._
import mill.scalalib.publish._

object playsonify extends PlaysonifyModule {

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

object `playsonify-core` extends PlaysonifyModule {
  def ivyDeps = Agg(
    scalactic
  )

  object test extends Tests{

    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}

object playsonifytest extends PlaysonifyModule {

  def ivyDeps = Agg(
    scalatestPlusPlay
  )
}

trait PlaysonifyModule extends ScalaModule with PublishModule {
  def scalaVersion = "2.12.2"
  def publishVersion = "1.3.0-SNAPSHOT"

  val playframework = ivy"com.typesafe.play::play:2.6.15"
  val scalactic = ivy"org.scalactic::scalactic:3.0.5"
  val scalatestPlusPlay = ivy"org.scalatestplus.play::scalatestplus-play:3.1.2"

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
