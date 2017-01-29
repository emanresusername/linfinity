lazy val commonSettings = Seq(
  scalaVersion := "2.12.1",
  organization := "my.will.be.done.linfinity",
  licenses += ("GPL", url("https://www.gnu.org/licenses/gpl.txt")),
  version := "0.2.0",
  scalacOptions ++= Seq("-deprecation", "-feature"),
  javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked")
)

val quicklensOrg     = "com.softwaremill.quicklens"
val quicklensVersion = "1.4.8"
lazy val core = crossProject
  .crossType(CrossType.Pure)
  .settings(commonSettings: _*)
  .jsSettings(
    libraryDependencies ++= {
      Seq(
        quicklensOrg %%% "quicklens" % quicklensVersion
      )
    }
  )
  .jvmSettings(
    libraryDependencies ++= {
      Seq(
        quicklensOrg %% "quicklens" % quicklensVersion
      )
    }
  )

lazy val coreJvm = core.jvm
lazy val coreJs  = core.js

lazy val cli = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= {

      Seq(
        "com.github.scopt" %% "scopt" % "3.5.0",
        "com.lihaoyi"      %% "fansi" % "0.2.3"
      )
    },
    buildInfoKeys := Seq[BuildInfoKey](name, version, organization),
    buildInfoPackage := s"${organization.value}.${name.value}"
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(coreJvm)

lazy val www = project
  .settings(commonSettings: _*)
  .settings(
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= {
      val bindingVersion = "10.0.2"

      Seq(
        "com.github.japgolly.scalacss" %%% "core" % "0.5.1",
        "com.thoughtworks.binding"     %%% "dom"  % bindingVersion
      )
    }
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(coreJs)

lazy val jsapp = project
  .settings(commonSettings: _*)
  .settings(
    persistLauncher := true,
    persistLauncher in Test := false
  )
  // TODO: conflicts with chrome plugin
  // .enablePlugins(WorkbenchPlugin)
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(www)

import chrome._
import chrome.permissions.Permission
import chrome.permissions.Permission.API
import net.lullabyte.{Chrome, ChromeSbtPlugin}

lazy val webext = project
  .settings(commonSettings: _*)
  .settings(
    persistLauncher := true,
    persistLauncher in Test := false,
    relativeSourceMaps := true,
    skip in packageJSDependencies := false,
    libraryDependencies ++= {
      Seq(
        "net.lullabyte" %%% "scala-js-chrome" % "0.4.0"
      )
    },
    chromeManifest := new AppManifest {
      val name    = "Linfinity"
      val version = Keys.version.value
      val app = App(
        background = Background(
          scripts = Chrome.defaultScripts
        )
      )
      override val defaultLocale = Some("en")
      override val icons = Chrome.icons(
        "icons",
        "linfinity.png",
        Set(32, 64, 96, 128)
      )
      override val permissions = Set[Permission](
        // TODO: persist settings
        // API.Storage
      )
    }
  )
  .enablePlugins(ChromeSbtPlugin)
  .dependsOn(www)
