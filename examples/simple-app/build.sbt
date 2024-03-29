name := "playsonify-simple-app"
organization := "com.alexitc"
scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

val playsonifyVersion = "2.3.0"

libraryDependencies ++= Seq(guice)

libraryDependencies ++= Seq(
  "com.alexitc" %% "playsonify-core" % playsonifyVersion,
  "com.alexitc" %% "playsonify-play" % playsonifyVersion,
  "com.alexitc" %% "playsonify-sql" % playsonifyVersion,
  "com.alexitc" %% "playsonify-play-test" % playsonifyVersion % Test
)

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.11"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.36"
libraryDependencies += "ch.qos.logback" % "logback-core" % "1.2.11"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.11"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
