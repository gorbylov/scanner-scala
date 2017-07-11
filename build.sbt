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

// Dependencies
// akka
val akkaVersion = "2.4.17"
val akkaHttpVersion = "10.0.4"
val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion
val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
val akkaCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.12.0"
//http
val scalajHttp = "org.scalaj" %% "scalaj-http" % "2.3.0"
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
  .aggregate(message, core, api, currency)
  .settings(name := """scanner-server""")
  .settings(baseSettings)

//protocol
lazy val message = module(
  name = "message",
  location = "protocol/message"
)

//cluster
lazy val clusterSeed = service(
  name = "cluster-seed",
  location = "cluster-seed",
  dependencies = Seq(message),
  libs = Seq(akkaActor, akkaCluster)
)

//services
lazy val core = module(
  name = "core",
  location = "service/core",
  dependencies = Seq(message),
  libs = Seq(akkaActor, akkaCluster, akkaHttp, akkaCirce, akkaSlf4j, logback, typeSafeLogs, scalaTest, akkaTest)
)
lazy val currency = service(
  name = "currency",
  location = "service/currency",
  dependencies = Seq(core, message),
  libs = Seq(scalaTest, akkaTest) ++ circeSuite
)
lazy val api = service(
  name = "api",
  location = "service/api",
  dependencies = Seq(core, message),
  libs = Seq(scalaTest, akkaTest, akkaHttpTest) ++ circeSuite
)
lazy val wizzair = service(
  name = "wizzair",
  location = "service/wizzair",
  dependencies = Seq(core, message),
  libs = Seq(scalaTest, akkaTest, akkaHttpTest) ++ circeSuite
)