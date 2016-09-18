lazy val marvel = project
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)

val circeVersion = "0.5.1"

libraryDependencies ++= Vector(
  Library.scalaTest % "test,it",
  "org.scalacheck" %% "scalacheck" % "1.13.2" % "test",
  "com.typesafe" % "config" % "1.3.0",
  "io.circe" %% "circe-parser" % circeVersion
)

initialCommands := """|import org.behaghel.marvel._
                      |""".stripMargin
