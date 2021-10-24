ThisBuild / organization := "com.alexitc"
ThisBuild / version := "2.0.0"
ThisBuild / scalaVersion := "2.12.7"

inThisBuild(
  List(
    name := "playsonify",
    description := "An opinionated library to help you build JSON APIs in a practical way using Play Framework ",
    organization := "com.alexit",
    homepage := Some(
      url("https://github.com/AlexITC/playsonify")
    ),
    licenses := List(
      "MIT" -> url("https://www.opensource.org/licenses/mit-license.html")
    ),
    developers := List(
      Developer(
        "AlexITC",
        "Alexis Hernandez",
        "alexis22229@gmail.com",
        url("https://github.com/AlexITC")
      )
    )
  )
)

val scalacticVersion = "3.0.5"

lazy val baseLibSettings: Project => Project = _.settings(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )
)

lazy val playSettings: Project => Project = _.settings(
  libraryDependencies ++= Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
  )
)

lazy val `playsonify-core` = (project in file("playsonify-core"))
  .configure(baseLibSettings, playSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % scalacticVersion
    )
  )

lazy val `playsonify-play` = (project in file("playsonify-play"))
  .configure(baseLibSettings, playSettings)
  .dependsOn(`playsonify-core`)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % "2.6.20",
      "org.scalactic" %% "scalactic" % scalacticVersion
    )
  )

lazy val `playsonify-akka-http` = (project in file("playsonify-akka-http"))
  .configure(baseLibSettings)
  .dependsOn(`playsonify-core`)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.5",
      "com.typesafe.akka" %% "akka-stream" % "2.5.12",
      "de.heikoseeberger" %% "akka-http-play-json" % "1.22.0",
      "com.typesafe.play" %% "play-json" % "2.6.10",
      "org.scalactic" %% "scalactic" % scalacticVersion
    ),
    // test
    libraryDependencies ++= Seq(
      "org.mockito" % "mockito-core" % "2.15.0" % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % "10.1.4" % Test
    )
  )

lazy val `playsonify-play-test` = (project in file("playsonify-play-test"))
  .configure(playSettings)

lazy val `playsonify-sql` = (project in file("playsonify-sql"))
  .dependsOn(`playsonify-core`)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % scalacticVersion
    )
  )
