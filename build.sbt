import Dependencies._
import Dsl._
import sbt._

lazy val baseSettings: BaseSettings = BaseSettings("com.scanner", "0.0.1", "2.12.6")

lazy val root = (project in file(".")).aggregate(protocol, core, api, currency, wizzair)

val protocol = module(
  name = "protocol",
  baseSettings = baseSettings
)

lazy val clusterSeed = module(
  name = "cluster-seed",
  baseSettings = baseSettings,
  dependsOn = Seq(protocol),
  compileLibs = Seq(akkaActor, akkaCluster)
)

lazy val core = module(
  name = "core",
  baseSettings = baseSettings,
  dependsOn = Seq(protocol),
  compileLibs = Seq(akkaActor, akkaCluster, akkaHttp, akkaCirce, scalaTest, akkaTest)
)

lazy val api = module(
  name = "api",
  baseSettings = baseSettings,
  dependsOn = Seq(protocol, core % "compile->compile;test->test"),
  compileLibs = Seq(circeGenerics, circeParcer),
  testLibs = Seq(scalaTest, akkaTest, akkaHttpTest)
)

lazy val currency = module(
  name = "currency",
  baseSettings = baseSettings,
  dependsOn = Seq(core, protocol),
  compileLibs = Seq(scalaTest, scalaMock, akkaTest, circeGenerics, circeParcer)
)

lazy val wizzair = module(
  name = "wizzair",
  baseSettings = baseSettings,
  dependsOn = Seq(core, protocol),
  compileLibs = Seq(scalajHttp, circeGenerics, circeParcer),
  testLibs = Seq(scalaTest, akkaTest, akkaHttpTest)
)