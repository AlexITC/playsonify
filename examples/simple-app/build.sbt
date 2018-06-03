name := "playsonify-simple-app"
organization := "com.alexitc"
scalaVersion := "2.12.2"

lazy val root = (project in file("."))
    .enablePlugins(PlayScala)

libraryDependencies ++= Seq(guice)

libraryDependencies += "com.alexitc" %% "playsonify" % "1.3.0-SNAPSHOT"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"
libraryDependencies += "ch.qos.logback" % "logback-core" % "1.2.3"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.alexitc" %% "playsonifytest" % "1.1.0" % Test

