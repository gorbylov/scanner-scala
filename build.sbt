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
val akka = Seq(
  "com.typesafe.akka" %% "akka-actor"
).map(_ % "2.4.17")
val akkaKryo = "com.github.romix.akka" %% "akka-kryo-serialization" % "0.5.1" //TODO find out akka kryo

// Project

lazy val root = (project in file(".")) //TODO find out lazy val and project
  .aggregate(query, currency)
  .settings(name := """scanner-server""")
  .settings(baseSettings)

lazy val query = module(name = "query", location = "protocol/query", libs = Seq(akkaKryo))
lazy val currency = module(name = "currency", location = "service/currency", dependencies = Seq(query), libs = akka)