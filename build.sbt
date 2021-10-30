ThisBuild / organization := "com.alexitc"
ThisBuild / version := "2.2.0"
ThisBuild / scalaVersion := "2.12.10"

inThisBuild(
  List(
    name := "playsonify",
    description := "An opinionated library to help you build JSON APIs in a practical way using Play Framework ",
    organization := "com.alexitc",
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

val scalacticVersion = "3.1.2"

lazy val baseLibSettings: Project => Project = _.settings(
  scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-unchecked" // Enable additional warnings where generated code depends on assumptions.
  ),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )
)

lazy val playSettings: Project => Project = _.settings(
  Compile / doc / scalacOptions ++= Seq(
    "-no-link-warnings"
  ),
  libraryDependencies ++= Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"
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
      "com.typesafe.play" %% "play" % "2.8.0",
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
  .configure(baseLibSettings)
  .dependsOn(`playsonify-core`)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % scalacticVersion
    )
  )
