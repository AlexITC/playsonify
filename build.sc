import mill._
import mill.scalalib._

object playsonify extends ScalaModule {
  def scalaVersion = "2.12.2"

  def ivyDeps = Agg(
    ivy"com.typesafe.play::play:2.6.11",
    ivy"org.scalactic::scalactic:3.0.4"
  )

  object test extends Tests{
    def ivyDeps = Agg(
      ivy"org.scalatestplus.play::scalatestplus-play:3.1.0"
    )

    def testFramework = "org.scalatest.tools.Framework"
  }
}
