import sbt.Keys._
import sbt._

val baseSettings = Seq(
  organization := "com.scanner",
  version := "1.0",
  scalaVersion := "2.12.1"
)

def module(
  name: String,
  location: String,
  dependencies: Seq[ClasspathDep[ProjectReference]] = Nil,
  libs: Seq[ModuleID] = Nil
) = Project(name, file(location), dependencies = dependencies)
  .settings(baseSettings)
  .settings(libraryDependencies ++= libs)

def service(
  name: String, location: String,
  dependencies: Seq[ClasspathDep[ProjectReference]] = Nil,
  libs: Seq[ModuleID] = Nil
) = module(name, location, dependencies, libs)
  .settings(Seq(fork in run := false))
  //.settings(assemblySettings)

// Dependencies
// akka
val akkaVersion = "2.4.17"
val akkaHttpVersion = "10.0.4"
val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
val akkaCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.12.0"
//json
val circeSuite = Seq(
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.7.0")
// logging
val logback = "ch.qos.logback" % "logback-classic" % "1.1.6"
val typeSafeLogs = "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2"
//test
val akkaTest = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
val akkaHttpTest = "com.typesafe.akka" %% "akka-http-testkit" % "10.0.4" % "test"

// Project
lazy val root = (project in file("."))
  .aggregate(query, core, currency)
  .settings(name := """scanner-server""")
  .settings(baseSettings)
//protocol
lazy val query = module(name = "query", location = "protocol/query")
//services
lazy val core = module(name = "service-core", location = "service/core",
  dependencies = Seq(query), libs = Seq(akkaActor, akkaHttpCore, akkaCirce, akkaSlf4j,
    logback, typeSafeLogs, scalaTest))
lazy val graphs = module(name = "graphs", location = "service/graphs", libs = Seq(scalaTest))
lazy val currency = service(name = "currency", location = "service/currency",
  dependencies = Seq(core, query), libs = Seq(scalaTest, akkaTest) ++ circeSuite)
lazy val wizzair = service(name = "wizzair", location = "service/wizzair",
  dependencies = Seq(core, query, graphs), libs = Seq(akkaTest, akkaHttpTest) ++
    circeSuite)