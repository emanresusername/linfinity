lazy val commonSettings = Seq(
  scalaVersion := "2.12.4",
  organization := "my.will.be.done.linfinity",
  licenses += ("GPL", url("https://www.gnu.org/licenses/gpl.txt")),
  version := "0.3.0",
  scalacOptions ++= Seq("-deprecation",
                        "-feature"
                        // TODO: https://github.com/scala/bug/issues/10448#issuecomment-350234124
                        // "-Xlint"
  ),
  scalafmtOnCompile := true
)

val quicklensOrg     = "com.softwaremill.quicklens"
val quicklensVersion = "1.4.11"
lazy val core = crossProject
  .crossType(CrossType.Pure)
  .settings(commonSettings)
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
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= {

      Seq(
        "com.github.scopt" %% "scopt" % "3.7.0",
        "com.lihaoyi"      %% "fansi" % "0.2.5"
      )
    },
    buildInfoKeys := Seq[BuildInfoKey](name, version, organization),
    buildInfoPackage := s"${organization.value}.${name.value}"
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(coreJvm)

lazy val www = project
  .settings(commonSettings)
  .settings(
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= {
      val bindingVersion = "11.0.1"

      Seq(
        "com.github.japgolly.scalacss" %%% "core" % "0.5.5",
        "com.thoughtworks.binding"     %%% "dom"  % bindingVersion
      )
    }
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(coreJs)

lazy val jsapp = project
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSUseMainModuleInitializer in Test := false,
    workbenchStartMode := WorkbenchStartModes.Manual
  )
  .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
  .dependsOn(www)

import chrome._
import chrome.permissions.Permission
import chrome.permissions.Permission.API
import net.lullabyte.{Chrome, ChromeSbtPlugin}

lazy val webext = project
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSUseMainModuleInitializer in Test := false,
    relativeSourceMaps := true,
    skip in packageJSDependencies := false,
    libraryDependencies ++= {
      Seq(
        "net.lullabyte" %%% "scala-js-chrome" % "0.5.0"
      )
    },
    chromeManifest := new ExtensionManifest {
      val name                 = "Linfinity"
      val version              = Keys.version.value
      override val description = Some("To Linfinity! And Beyond!")
      val background = Background(
        scripts = Chrome.defaultScripts
      )
      override val defaultLocale = Some("en")
      override val icons = Chrome.icons(
        "icons",
        "linfinity.png",
        Set(32, 64, 96, 128)
      )
      override val browserAction = Option(
        BrowserAction(
          icon = icons,
          title = Option(name)
        ))
      override val permissions = Set[Permission](
        // TODO: persist settings
        // API.Storage
      )
    }
  )
  .enablePlugins(ChromeSbtPlugin)
  .dependsOn(www)

lazy val root = project
  .in(file("."))
  .aggregate(coreJs, coreJvm, cli, www, jsapp, webext)
