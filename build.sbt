lazy val marvel = project
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)

libraryDependencies ++= Vector(
  Library.scalaTest % "test,it",
  "com.typesafe" % "config" % "1.3.0"
)

initialCommands := """|import org.behaghel.marvel._
                      |""".stripMargin
