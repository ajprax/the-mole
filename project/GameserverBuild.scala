import sbt._
import sbt.Keys._

object GameserverBuild extends Build {

  lazy val gameserver = Project(
    id = "gameserver",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "gameserver",
      organization := "com.goldblastgames",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.2",
      libraryDependencies ++= Seq(
        "com.goldblastgames" % "reactive-sockets_2.9.2" % "0.1-SNAPSHOT",
        "cc.co.scala-reactive" %% "reactive-core" % "0.2-SNAPSHOT",
        "commons-io" % "commons-io" % "2.4"
      ),
      resolvers ++= Seq(
        "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
      )
    )
  )
}
