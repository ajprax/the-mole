import sbt._
import sbt.Keys._

object TheMoleBuild extends Build {

  lazy val theMole = Project(
    id = "the-mole",
    base = file(".")
  ) aggregate(common, server, cli, web)

  lazy val common = Project(
    id = "the-mole-common",
    base = file("common")
  )

  lazy val server = Project(
    id = "the-mole-server",
    base = file("server")
  ) dependsOn(common)

  lazy val cli = Project(
    id = "the-mole-cli",
    base = file("cli")
  ) dependsOn(common)

  lazy val web = Project(
    id = "the-mole-web",
    base = file("web")
  ) dependsOn(common)
}
