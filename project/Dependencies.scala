import sbt._

object Dependencies {

  val akkaVersion     = "2.5.12"
  val akkaHttpVersion = "10.1.1"
  val circeVersion    = "0.9.3"

  // compile
  lazy val akkaActor      = "com.typesafe.akka" %% "akka-actor"         % akkaVersion
  lazy val akkaCluster    = "com.typesafe.akka" %% "akka-cluster"       % akkaVersion
  lazy val akkaHttp       = "com.typesafe.akka" %% "akka-http"          % akkaHttpVersion
  lazy val akkaCirce      = "de.heikoseeberger" %% "akka-http-circe"    % "1.12.0"
  lazy val scalajHttp     = "org.scalaj"        %% "scalaj-http"        % "2.3.0"
  lazy val circeGenerics  = "io.circe"          %% "circe-generic"      % circeVersion
  lazy val circeParcer    = "io.circe"          %% "circe-parser"       % circeVersion
  // test
  lazy val akkaTest       = "com.typesafe.akka" %% "akka-testkit"                 % akkaVersion     % Test
  lazy val akkaHttpTest   = "com.typesafe.akka" %% "akka-http-testkit"            % akkaHttpVersion % Test
  lazy val scalaTest      = "org.scalatest"     %% "scalatest"                    % "3.0.1"         % Test
  lazy val scalaMock      = "org.scalamock"     %% "scalamock-scalatest-support"  % "3.6.0"         % Test
}
