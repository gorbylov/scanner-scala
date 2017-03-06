import sbt.Keys._
import sbt._

val baseSettings = Seq(
  organization := "com.scanner",
  version := "1.0",
  scalaVersion := "2.12.1"
)

def module(name: String, location: String, dependencies: Seq[ClasspathDep[ProjectReference]] = Nil, libs: Seq[ModuleID] = Nil) = {
  Project(name, file(location), dependencies = dependencies) //TODO find out project creation
    .settings(baseSettings)
    .settings(libraryDependencies ++= libs)
}

// Dependencies
// akka
val akkaSuite = Seq(
  "com.typesafe.akka" %% "akka-actor",
  "com.typesafe.akka" %% "akka-slf4j"
).map(_ % "2.4.17")
val akkaKryo = "com.github.romix.akka" %% "akka-kryo-serialization" % "0.5.1" //TODO find out akka kryo
//json
val circeSuite = Seq(
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.7.0")
// logging
val logback = "ch.qos.logback" % "logback-classic" % "1.1.6"
val loggly = "org.logback-extensions" % "logback-ext-loggly" % "0.1.2"
val typeSafeLogs = "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2"

// Project
lazy val root = (project in file(".")) //TODO find out lazy val and project
  .aggregate(query, currency)
  .settings(name := """scanner-server""")
  .settings(baseSettings)

lazy val query = module(name = "query", location = "protocol/query", libs = Seq(akkaKryo))
//services
lazy val core = module(name = "service-core", location = "service/core",
  dependencies = Seq(query), libs = akkaSuite ++ Seq(logback, typeSafeLogs))
lazy val currency = module(name = "currency", location = "service/currency",
  dependencies = Seq(core, query), libs = akkaSuite ++ circeSuite)
