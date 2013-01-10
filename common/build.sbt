resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "com.github.oetzi" %% "echo" % "1.1.0",
  "commons-io" % "commons-io" % "2.4",
  "org.slf4j" % "slf4j-api" % "1.6.6",
  "org.slf4j" % "slf4j-simple" % "1.6.6",
  "org.scalaz" %% "scalaz-core" % "6.0.4",
  "org.specs2" %% "specs2" % "1.12.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.apache.commons" % "commons-lang3" % "3.1" % "test",
  "net.databinder" %% "unfiltered-netty-websockets" % "0.6.5"
  )
