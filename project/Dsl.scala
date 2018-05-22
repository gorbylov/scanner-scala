import sbt.Keys.{scalaVersion, version, _}
import sbt.{ClasspathDep, Def, ModuleID, Plugins, Project, ProjectReference, file}

object Dsl {

  case class BaseSettings(organization: String, version: String, scalaVersion: String)

  def module(
    name: String,
    baseSettings: BaseSettings,
    dependsOn: Seq[ClasspathDep[ProjectReference]] = Nil,
    compileLibs: Seq[ModuleID] = Nil,
    testLibs: Seq[ModuleID] = Nil,
    plugins: Seq[Plugins] = Nil,
    additionalSettings: Seq[Def.SettingsDefinition] = Nil
  ): Project = {
    val settings = Seq(
      organization := baseSettings.organization,
      version := baseSettings.version,
      scalaVersion := baseSettings.scalaVersion
    )
    Project(name, file(name))
      .dependsOn(dependsOn: _*)
      .settings(settings)
      .settings(libraryDependencies ++= compileLibs ++ testLibs)
      .settings(additionalSettings: _*)
      .enablePlugins(plugins: _*)
  }

}
