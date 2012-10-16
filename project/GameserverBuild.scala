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
        // Normal dependencies.
        "com.github.oetzi" %% "echo" % "1.1.0",
        "commons-io" % "commons-io" % "2.4",
        "org.slf4j" % "slf4j-api" % "1.6.6",
        "org.slf4j" % "slf4j-simple" % "1.6.6",
        "org.scalaz" %% "scalaz-core" % "6.0.4",

        // Test dependencies.
        "org.specs2" %% "specs2" % "1.12.1" % "test",
        "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
        "org.apache.commons" % "commons-lang3" % "3.1" % "test"
      ),
      resolvers ++= Seq(
        "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
        "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
      )
    )
  )
}
