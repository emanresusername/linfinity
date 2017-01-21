lazy val commonSettings = Seq(
  scalaVersion := "2.12.1",
  organization := "my.will.be.done.linfinity",
  licenses += ("GPL", url("https://www.gnu.org/licenses/gpl.txt")),
  version := "0.2.0"
)

lazy val core = (crossProject.crossType(CrossType.Pure) in file("core"))
  .settings(commonSettings: _*)
  .settings(name := "core")

lazy val coreJvm = core.jvm
lazy val coreJs  = core.js

lazy val cli = (project in file("cli"))
  .settings(commonSettings: _*)
  .settings(
    name := "cli",
    libraryDependencies ++= {
      Seq(
        "com.lihaoyi"       %% "fansi"   % "0.2.3",
        "com.github.kxbmap" %% "configs" % "0.4.4"
      )
    }
  )
  .dependsOn(coreJvm)

lazy val js = (project in file("js"))
  .settings(commonSettings: _*)
  .settings(
    name := "js",
    persistLauncher := true,
    persistLauncher in Test := false,
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= {
      val bindingV = "10.0.2"

      Seq(
        "com.github.japgolly.scalacss" %%% "core" % "0.5.1",
        "com.thoughtworks.binding"     %%% "dom"  % bindingV
      )
    }
  )
  .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
  .dependsOn(coreJs)
