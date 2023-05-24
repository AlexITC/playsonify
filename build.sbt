ThisBuild / organization := "com.alexitc"
ThisBuild / scalaVersion := "2.13.8"

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

val playVersion = "2.8.8"
val scalacticVersion = "3.1.2"
val scalatestVersion = "3.2.16"

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
    "org.scalatest" %% "scalatest" % scalatestVersion % Test
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
      "com.typesafe.play" %% "play" % playVersion,
      "org.scalactic" %% "scalactic" % scalacticVersion
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
