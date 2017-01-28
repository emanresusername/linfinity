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
lazy val core = (crossProject.crossType(CrossType.Pure) in file("core"))
  .settings(commonSettings: _*)
  .settings(name := "core")
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

lazy val cli = (project in file("cli"))
  .settings(commonSettings: _*)
  .settings(
    name := "cli",
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

lazy val www = (project in file("www"))
  .settings(commonSettings: _*)
  .settings(
    name := "js",
    persistLauncher := true,
    persistLauncher in Test := false,
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= {
      val bindingVersion = "10.0.2"

      Seq(
        "com.github.japgolly.scalacss" %%% "core" % "0.5.1",
        "com.thoughtworks.binding"     %%% "dom"  % bindingVersion
      )
    }
  )
  .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
  .dependsOn(coreJs)
