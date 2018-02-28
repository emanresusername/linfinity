// https://github.com/lucidd/scala-js-chrome/issues/33
resolvers += Resolver.url("veinhorn plugins-repo", url("https://dl.bintray.com/veinhorn/sbt-plugins"))(Resolver.ivyStylePatterns)

Seq(
  "com.lucidchart" % "sbt-scalafmt" % "1.15",
  "org.scala-js"  % "sbt-scalajs"       % "0.6.22",
  "com.eed3si9n"  % "sbt-buildinfo"     % "0.8.0",
  "net.lullabyte" % "sbt-chrome-plugin" % "0.5.8",
  "com.lihaoyi" % "workbench" % "0.4.0"
).map(addSbtPlugin)
